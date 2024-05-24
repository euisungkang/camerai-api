package spinai.camerai.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageAnalysisRequest {
    private String image_uuid;
    private String image_url;
}
