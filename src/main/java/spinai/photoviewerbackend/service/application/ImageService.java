package spinai.photoviewerbackend.service.application;

import org.springframework.web.multipart.MultipartFile;
import spinai.photoviewerbackend.dto.response.ImageAnalysisResponse;
import spinai.photoviewerbackend.dto.response.ImageTaggingResponse;
import spinai.photoviewerbackend.dto.response.ImageUploadResponse;

import java.util.List;
import java.util.Map;

public interface ImageService {
    public ImageUploadResponse uploadImages(MultipartFile[] images, String user_uuid);
    public ImageAnalysisResponse analyzeImage(String image_uuid, String image_url);
    public ImageTaggingResponse tagImage(String image_uuid, String image_url);
}
