package spinai.camerai.service.application.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.transfer.Upload;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import spinai.camerai.dto.response.*;
import spinai.camerai.exception.FileProcessingException;
import spinai.camerai.exception.InvalidInputException;
import spinai.camerai.model.UploadedImage;
import spinai.camerai.repository.ImageRepository;
import spinai.camerai.service.application.ImageService;
import spinai.camerai.service.domain.CloudVisionService;
import spinai.camerai.service.domain.GPTService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final AmazonS3 s3;

    @Value("${aws.bucket.name}")
    private String bucketName;

    @Value("${aws.cdn.domain}")
    private String domain;

    private final GPTService gptService;
    private final CloudVisionService cloudVisionService;
    private final ImageRepository imageRepository;

    @Override
    @Transactional(readOnly = true)
    public ImageListResponse getImages(String user_uuid, int page) {
        List<UploadedImage> images = imageRepository.getImages(user_uuid, page);
        return buildImageListResponse(images, page);
    }

    @Override
    @Transactional(readOnly = true)
    public ImageDetailResponse getImageDetail(String image_uuid) {
        UploadedImage image = imageRepository.getImageDetails(image_uuid);

        if (image.getTags_uuid() != null) {
            image.setTags(imageRepository.getIImageTags(image.getTags_uuid()));
        }

        return buildImageDetailResponse(image);
    }

    @Override
    @Transactional(readOnly = false)
    public ImageUploadResponse uploadImages(MultipartFile[] images, String user_uuid) {
        List<String> urls = new ArrayList<>();

        for (MultipartFile image : images) {
            File file = new File(Objects.requireNonNull(image.getOriginalFilename()));
            try (FileOutputStream stream = new FileOutputStream(file)) {
                stream.write(image.getBytes());
            } catch (IOException ex) {
                throw new FileProcessingException("Invalid file of extension " + FilenameUtils.getExtension(image.getOriginalFilename()) + ". Data may be corrupted");
            }

            String fileName = generateFileName(image);

            PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/" + FilenameUtils.getExtension(image.getOriginalFilename()));
            metadata.setContentLength(file.length());
            request.setMetadata(metadata);
            PutObjectResult result = s3.putObject(request);

            file.delete();

            urls.add(domain + s3.getUrl(bucketName, fileName).getPath());
        }

        List<UploadedImage> uploadedImages = buildUploadedImages(user_uuid, urls);
        imageRepository.saveImages(uploadedImages);

        return buildImageUploadResponse(uploadedImages);
    }

    @Override
    @Transactional(readOnly = false)
    public ImageAnalysisResponse analyzeImage(String image_uuid, String image_url) {
        String description = gptService.analyzeImageDescription(image_url);

        imageRepository.updateImageDescription(image_uuid, description);

        return ImageAnalysisResponse.builder()
                .description(description)
                .build();
    }

    @Override
    @Transactional(readOnly = false)
    public ImageTaggingResponse tagImage(String image_uuid, MultipartFile image) {
        List<String> tags = cloudVisionService.analyzeImageTags(image);
        String tags_uuid = UUID.randomUUID().toString();

        imageRepository.updateImageTagsUUID(image_uuid, tags_uuid);
        imageRepository.saveImageTagsArray(tags_uuid, tags);

        return ImageTaggingResponse.builder()
                .tags_uuid(tags_uuid)
                .tags(tags)
                .build();
    }

    private List<UploadedImage> buildUploadedImages(String user_uuid, List<String> urls) {
        List<UploadedImage> images = new ArrayList<>();
        LocalDateTime uploaded = LocalDateTime.now();

        for (String url : urls) {
            images.add(UploadedImage.builder()
                    .image_uuid(UUID.randomUUID().toString())
                    .user_uuid(user_uuid)
                    .image_url(url)
                    .deleted(false)
                    .uploaded(uploaded)
                    .build());
        }

        return images;
    }

    private ImageUploadResponse buildImageUploadResponse(List<UploadedImage> images) {
        return ImageUploadResponse.builder()
                .images(images.stream().map((i) ->
                        ImageUploadResponse.Image.builder()
                                .image_uuid(i.getImage_uuid())
                                .image_url(i.getImage_url())
                                .build())
                        .toList())
                .build();
    }

    private ImageListResponse buildImageListResponse(List<UploadedImage> images, int page) {
        return ImageListResponse.builder()
                .display(images.size())
                .page(page)
                .images(images)
                .build();
    }

    private ImageDetailResponse buildImageDetailResponse(UploadedImage image) {
        return ImageDetailResponse.builder()
                .image_uuid(image.getImage_uuid())
                .image_url(image.getImage_url())
                .tags_uuid(image.getTags_uuid())
                .tags(image.getTags())
                .description(image.getDescription())
                .uploaded(image.getUploaded())
                .build();
    }

    private void validateImageUrl(String image_url) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(image_url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet);
                 InputStream stream = response.getEntity().getContent()) {
                Image image = ImageIO.read(stream);
                if (image == null) {
                    throw new InvalidInputException("invalid or empty image url " + image_url);
                }
            }
        } catch (IOException ex) {
            throw new InvalidInputException("malformed image url " + image_url);
        }
    }

    private String generateFileName(MultipartFile multipartFile) {
        return new Date().getTime() + "-" + Objects.requireNonNull(multipartFile.getOriginalFilename()).replaceAll(" ", "_");
    }
}
