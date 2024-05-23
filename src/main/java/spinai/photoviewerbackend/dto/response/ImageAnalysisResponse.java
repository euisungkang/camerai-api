package spinai.photoviewerbackend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageAnalysisResponse {
    private String description;
}
