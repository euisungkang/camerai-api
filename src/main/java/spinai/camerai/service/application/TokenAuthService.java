package spinai.camerai.service.application;

import spinai.camerai.dto.request.TokenRefreshRequest;
import spinai.camerai.dto.request.TokenRequest;
import spinai.camerai.dto.response.TokenRefreshResponse;
import spinai.camerai.dto.response.TokenResponse;

public interface TokenAuthService {
    public TokenResponse createToken(TokenRequest request);
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request);
    public String getUUID(String token);
}
