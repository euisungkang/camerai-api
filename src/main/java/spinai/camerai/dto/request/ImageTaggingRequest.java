package spinai.camerai.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageTaggingRequest {
    private String image_uuid;
    private MultipartFile image;
}
