package spinai.photoviewerbackend.dto.request;

import lombok.Data;

@Data
public class ImageAnalysisRequest {
    private String image_uuid;
    private String image_url;
}
