package spinai.photoviewerbackend.service.application.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
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
import spinai.photoviewerbackend.dto.response.ImageTaggingResponse;
import spinai.photoviewerbackend.dto.response.ImageUploadResponse;
import spinai.photoviewerbackend.exception.FileProcessingException;
import spinai.photoviewerbackend.exception.InvalidInputException;
import spinai.photoviewerbackend.dto.response.ImageAnalysisResponse;
import spinai.photoviewerbackend.model.UploadedImage;
import spinai.photoviewerbackend.repository.ImageRepository;
import spinai.photoviewerbackend.service.application.ImageService;
import spinai.photoviewerbackend.service.domain.CloudVisionService;
import spinai.photoviewerbackend.service.domain.GPTService;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public ImageTaggingResponse tagImage(String image_uuid, String image_url) {
        validateImageUrl(image_url);

        List<String> tags = cloudVisionService.analyzeImageTags(image_url);
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
