package spinai.photoviewerbackend.service.domain.impl;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import spinai.photoviewerbackend.exception.ResponseProcessingException;
import spinai.photoviewerbackend.service.domain.CloudVisionService;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CloudVisionServiceImpl implements CloudVisionService {

    private final Logger logger = LoggerFactory.getLogger(CloudVisionService.class);

    private static final Integer TAGS_UPPER_LIMIT = 4;

    @Override
    public List<String> analyzeImageTags(String image_url) {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

            AnnotateImageRequest request = buildImageRequest(image_url);
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

    private AnnotateImageRequest buildImageRequest(String image_url) throws IOException {
        URL url = new URL(image_url);
        InputStream in = new BufferedInputStream(url.openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;
        while (-1 != (n = in.read(buf))) {
            out.write(buf, 0, n);
        }
        out.close();
        in.close();

        ByteString imgBytes = ByteString.copyFrom(out.toByteArray());
        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.LABEL_DETECTION).build();
        return AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    }
}
