package spinai.camerai.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageAnalysisResponse {
    private String description;
}
