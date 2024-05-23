package spinai.photoviewerbackend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JWTToken {

    @JsonProperty("token_type")
    private String token_type;

    @JsonProperty("exp")
    private String exp;

    @JsonProperty("iat")
    private String iat;

    @JsonProperty("user_email")
    private String user_email;

    @JsonProperty("user_uuid")
    private String user_uuid = "";
}

