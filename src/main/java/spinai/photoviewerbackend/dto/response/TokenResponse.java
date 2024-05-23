package spinai.photoviewerbackend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String refresh;
    private String access;
    private String user_email;
}
