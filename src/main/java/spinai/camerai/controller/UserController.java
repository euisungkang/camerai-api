package spinai.camerai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spinai.camerai.dto.request.UserRequest;
import spinai.camerai.exception.InvalidInputException;
import spinai.camerai.service.application.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("")
    public ResponseEntity<Object> postUser(@RequestBody UserRequest request) {
        userService.saveUser(request);
        return new ResponseEntity<>("", HttpStatus.CREATED);
    }

    @GetMapping("/validation/email")
    public ResponseEntity<Object> validateEmail(@RequestParam String email) {
        if (email.isEmpty() || email.isBlank()) {
            throw new InvalidInputException("Invalid or empty email");
        }

        boolean valid = !userService.emailTaken(email);
        return new ResponseEntity<>(valid, HttpStatus.OK);
    }
}
