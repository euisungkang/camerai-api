package spinai.camerai.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImageUploadResponse {
    private List<Image> images;

    @Data
    @Builder
    public static class Image {
        private String image_uuid;
        private String image_url;
    }
}
