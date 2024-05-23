package spinai.photoviewerbackend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spinai.photoviewerbackend.dto.request.TokenRefreshRequest;
import spinai.photoviewerbackend.dto.request.TokenRequest;
import spinai.photoviewerbackend.dto.response.TokenRefreshResponse;
import spinai.photoviewerbackend.dto.response.TokenResponse;
import spinai.photoviewerbackend.exception.InvalidInputException;
import spinai.photoviewerbackend.service.application.TokenAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/token")
public class TokenController {

    private final TokenAuthService tokenAuthService;

    @PostMapping("")
    public ResponseEntity<TokenResponse> postToken(@RequestBody TokenRequest request) {
        if (request.getUser_email().isEmpty() || request.getUser_email().isBlank()) {
            throw new InvalidInputException("Invalid Input: email cannot be empty");
        }

        TokenResponse tokens = tokenAuthService.createToken(request);

        return new ResponseEntity<>(tokens, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Object> postRefreshToken(@RequestBody TokenRefreshRequest request) {
        if (request.getRefresh().isEmpty() || request.getRefresh().isBlank()) {
            throw new InvalidInputException("Invalid Input: refresh token cannot be empty");
        }

        TokenRefreshResponse tokens = tokenAuthService.refreshToken(request);

        return new ResponseEntity<>(tokens, HttpStatus.OK);
    }
}
