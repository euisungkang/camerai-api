package spinai.photoviewerbackend.service.application;

import spinai.photoviewerbackend.dto.request.TokenRefreshRequest;
import spinai.photoviewerbackend.dto.request.TokenRequest;
import spinai.photoviewerbackend.dto.response.TokenRefreshResponse;
import spinai.photoviewerbackend.dto.response.TokenResponse;

public interface TokenAuthService {
    public TokenResponse createToken(TokenRequest request);
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request);
    public String getUUID(String token);
}
