package spinai.photoviewerbackend.dto.request;

import lombok.Data;

@Data
public class TokenRefreshRequest {
    private String refresh;
}