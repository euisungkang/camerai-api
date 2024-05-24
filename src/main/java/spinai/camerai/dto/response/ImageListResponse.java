package spinai.camerai.dto.response;

import lombok.Builder;
import lombok.Data;
import spinai.camerai.model.UploadedImage;

import java.util.List;

@Data
@Builder
public class ImageListResponse {
    private Integer display;
    private Integer page;
    private List<UploadedImage> images;
}
