package spinai.camerai.service.application.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import spinai.camerai.dto.request.TokenRefreshRequest;
import spinai.camerai.dto.request.TokenRequest;
import spinai.camerai.dto.response.TokenRefreshResponse;
import spinai.camerai.dto.response.TokenResponse;
import spinai.camerai.exception.*;
import spinai.camerai.model.JWTToken;
import spinai.camerai.service.application.TokenAuthService;
import spinai.camerai.service.application.UserService;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenAuthServiceImpl implements TokenAuthService {

    @Value("${jwt.signing.secret}")
    private String jwtSecret;

    private final ObjectMapper mapper;
    private final UserService userService;

    @Override
    public TokenResponse createToken(TokenRequest request) {
        if (!userService.credentialsValid(request.getUser_email(), request.getPassword())) {
            throw new EntityNotFoundException("Invalid credentials");
        }

        String user_uuid = userService.getUUIDByEmail(request.getUser_email());

        return buildTokenResponse(request.getUser_email(), user_uuid);
    }

    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String[] chunks = request.getRefresh().split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();

        String payload = new String(decoder.decode(chunks[1]));

        try {
            JWTToken token = mapper.readValue(payload, JWTToken.class);

            if (!userService.emailTaken(token.getUser_email())) {
                throw new EntityNotFoundException("Entity Not Found: email does not exist");
            } else if (!token.getToken_type().equals("refresh")) {
                throw new InvalidInputException("Invalid token type: must be refresh token");
            } else if (Instant.ofEpochSecond(Long.parseLong(token.getExp())).isBefore(Instant.now())) {
                throw new ExpiredTokenException("Refresh token has expired");
            }

            if (token.getUser_uuid().isEmpty() || token.getUser_uuid().isBlank()) {
                String user_uuid = userService.getUUIDByEmail(token.getUser_email());
                return buildTokenRefreshResponse(token.getUser_email(), user_uuid);
            } else {
                return buildTokenRefreshResponse(token.getUser_email(), token.getUser_uuid());
            }

        } catch (JsonProcessingException ex) {
            throw new ResponseProcessingException("JWT: Error processing refresh token payload");
        }
    }

    @Override
    public String getUUID(String token) {
        String payload = getPayload(token);

        try {
            JWTToken JWTToken = mapper.readValue(payload, JWTToken.class);

            if (!userService.emailTaken(JWTToken.getUser_email())) {
                throw new EntityNotFoundException("Entity Not Found: email does not exist");
            }

            return JWTToken.getUser_uuid();
        } catch (JsonProcessingException ex) {
            throw new ResponseProcessingException("JWT: Error processing access token payload");
        }
    }

    private String getPayload(String token) {

        // Filter "Bearer " from the token
        String[] filter = token.split(" ");
        String rawToken = "";
        if (filter.length > 1) {
            rawToken = filter[filter.length - 1];
        } else {
            rawToken = filter[0];
        }

        String[] chunks = rawToken.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        return new String(decoder.decode(chunks[1]));
    }

    private TokenResponse buildTokenResponse(String email, String user_uuid) {
        try {
            return TokenResponse.builder()
                    .refresh(generateToken(email, user_uuid, "refresh"))
                    .access(generateToken(email, user_uuid, "access"))
                    .user_email(email)
                    .build();
        } catch (Exception ex) {
            throw new TokenGenerationException("Failed to generate new JWT token");
        }
    }

    private TokenRefreshResponse buildTokenRefreshResponse(String email, String user_uuid) {
        try {
            return TokenRefreshResponse.builder()
                    .access(generateToken(email, user_uuid, "access"))
                    .refresh(generateToken(email, user_uuid, "refresh"))
                    .build();
        } catch (Exception ex) {
            throw new TokenGenerationException("Failed to generate refreshed JWT token");
        }
    }

    private String generateToken(String email, String user_uuid, String token_type) throws Exception {

        // JWT Header
        JWSHeader.Builder headerBuilder = new JWSHeader.Builder(JWSAlgorithm.HS256);
        headerBuilder.type(JOSEObjectType.JWT);

        final Date iat = new Date(new Date().getTime());
        final Date exp = new Date(new Date().getTime() + (1 * 24 * 60 * 60 * 1000));

        // JWT Payload
        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder();
        claimsBuilder.claim("token_type", token_type);
        claimsBuilder.issueTime(iat);
        claimsBuilder.expirationTime(exp);
        claimsBuilder.claim("user_email", email);
        claimsBuilder.claim("user_uuid", user_uuid);

        // JWT Secret Key
        byte[] key = jwtSecret.getBytes();
        SecretKey originalKey = new SecretKeySpec(key, 0, key.length, "HmacSHA256");

        // JWT Signature
        JWSSigner jwsSigner = new MACSigner(originalKey);
        SignedJWT jwt = new SignedJWT(headerBuilder.build(), claimsBuilder.build());
        jwt.sign(jwsSigner);

        return jwt.serialize();
    }

    private boolean invalidUUID(String uuid) {
        return uuid == null || uuid.isEmpty() || uuid.isBlank();
    }
}
