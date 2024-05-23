package spinai.photoviewerbackend.service.domain.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spinai.photoviewerbackend.dto.request.UserRequest;
import spinai.photoviewerbackend.model.User;
import spinai.photoviewerbackend.repository.UserRepository;
import spinai.photoviewerbackend.service.domain.UserService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = false)
    public boolean saveUser(UserRequest request) {
        User user = buildUser(request);

        int rowsAffected = userRepository.saveUser(user);

        return rowsAffected != 0;
    }

    @Override
    @Transactional(readOnly = true)
    public String getUUIDByEmail(String email) {
        return userRepository.getUUIDByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailTaken(String email) {
        return userRepository.emailExists(email);
    }

    private User buildUser(UserRequest request) {
        LocalDateTime created_on = LocalDateTime.now();

        return User.builder()
                .user_uuid(UUID.randomUUID().toString())
                .email(request.getEmail())
                .password(request.getPassword())
                .created_on(created_on)
                .build();
    }
}
