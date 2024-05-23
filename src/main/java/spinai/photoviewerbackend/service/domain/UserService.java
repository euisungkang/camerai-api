package spinai.photoviewerbackend.service.domain;

import spinai.photoviewerbackend.dto.request.UserRequest;

public interface UserService {

    public boolean saveUser(UserRequest request);

    public String getUUIDByEmail(String email);
    public boolean emailTaken(String email);
}
