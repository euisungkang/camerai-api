package spinai.camerai.service.application;

import org.springframework.web.multipart.MultipartFile;
import spinai.camerai.dto.response.*;
import spinai.camerai.model.UploadedImage;

public interface ImageService {
    public ImageListResponse getImages(String user_uuid, int page);
    public ImageDetailResponse getImageDetail(String image_uuid);
    public ImageUploadResponse uploadImages(MultipartFile[] images, String user_uuid);
    public ImageAnalysisResponse analyzeImage(String image_uuid, String image_url);
    public ImageTaggingResponse tagImage(String image_uuid, MultipartFile image);
}
