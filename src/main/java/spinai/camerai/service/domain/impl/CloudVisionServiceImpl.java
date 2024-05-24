package spinai.camerai.service.domain.impl;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spinai.camerai.exception.ResponseProcessingException;
import spinai.camerai.service.domain.CloudVisionService;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CloudVisionServiceImpl implements CloudVisionService {

    private final Logger logger = LoggerFactory.getLogger(CloudVisionService.class);

    private static final Integer TAGS_UPPER_LIMIT = 4;

    @Override
    public List<String> analyzeImageTags(MultipartFile image) {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

            AnnotateImageRequest request = buildImageRequest(image);
            BatchAnnotateImagesResponse response = vision.batchAnnotateImages(List.of(request));
            AnnotateImageResponse res= response.getResponsesList().getFirst();

            if (res.hasError()) {
                logger.error("error processing image annotation" + res.getErrorOrBuilder().getMessage());
            }

            List<String> annotations = new ArrayList<>();
            final int tagsLimit;

            if (res.getLabelAnnotationsList().size() > TAGS_UPPER_LIMIT) {
                tagsLimit = TAGS_UPPER_LIMIT;
            } else {
                tagsLimit = res.getLabelAnnotationsList().size();
            }

            for (int j = 0; j < tagsLimit; j++) {
                annotations.add(res.getLabelAnnotationsList().get(j).getDescription());
            }

            return annotations;
        } catch (IOException ex) {
            throw new ResponseProcessingException("Error while processing image labelling", ex);
        }
    }

    private AnnotateImageRequest buildImageRequest(MultipartFile image) throws IOException {
        byte[] data = image.getBytes();
        ByteString imgBytes = ByteString.copyFrom(data);
        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
        return AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    }
}
