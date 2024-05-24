package spinai.camerai.service.application;

import spinai.camerai.dto.request.UserRequest;

public interface UserService {

    public boolean saveUser(UserRequest request);
    public String getUUIDByEmail(String email);
    public boolean emailTaken(String email);
    public boolean credentialsValid(String email, String password);
}
