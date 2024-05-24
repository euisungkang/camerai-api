package spinai.camerai.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ImageDetailResponse {
    private String image_uuid;
    private String image_url;
    private String tags_uuid;
    private List<String> tags;
    private String description;
    private LocalDateTime uploaded;
}
