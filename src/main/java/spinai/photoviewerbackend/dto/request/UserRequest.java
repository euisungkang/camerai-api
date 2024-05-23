package spinai.photoviewerbackend.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
public class UserRequest {
    private String email;
    private String password;
}
