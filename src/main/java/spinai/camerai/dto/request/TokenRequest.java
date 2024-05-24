package spinai.camerai.dto.request;

import lombok.Data;

@Data
public class TokenRequest {
    private String user_email;
    private String password;
}
