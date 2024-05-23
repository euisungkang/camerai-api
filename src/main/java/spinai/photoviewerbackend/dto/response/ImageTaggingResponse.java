package spinai.photoviewerbackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImageTaggingResponse {
    private String tags_uuid;
    private List<String> tags;
}
