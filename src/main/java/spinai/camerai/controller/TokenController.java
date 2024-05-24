package spinai.camerai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spinai.camerai.dto.request.TokenRefreshRequest;
import spinai.camerai.dto.request.TokenRequest;
import spinai.camerai.dto.response.TokenRefreshResponse;
import spinai.camerai.dto.response.TokenResponse;
import spinai.camerai.exception.InvalidInputException;
import spinai.camerai.service.application.TokenAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/token")
public class TokenController {

    private final TokenAuthService tokenAuthService;

    @PostMapping("")
    public ResponseEntity<TokenResponse> postToken(@RequestBody TokenRequest request) {
        if (request.getUser_email().isEmpty() || request.getUser_email().isBlank() ||
            request.getPassword().isEmpty() || request.getPassword().isBlank()) {
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
