package spinai.camerai.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import spinai.camerai.model.User;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    @Qualifier("imagesNamedParameterJdbcTemplate")
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public int saveUser(User user) {
        final String query = "INSERT INTO `users` " +
                "(user_uuid, email, password, created_on) " +
                "VALUES (:user_uuid, :email, :password, :created_on)";

        final MapSqlParameterSource params = mapUserToSqlParameters(user);

        return jdbcTemplate.update(query, params);
    }

    public String getUUIDByEmail(String email) {
        final String query = "SELECT user_uuid FROM `users` WHERE email=:email";

        MapSqlParameterSource params = new MapSqlParameterSource("email", email);

        return jdbcTemplate.queryForObject(query, params, String.class);
    }

    public Boolean emailExists(String email) {
        final String query = "SELECT EXISTS (SELECT 1 FROM `users` WHERE email=:email)";

        MapSqlParameterSource params = new MapSqlParameterSource("email", email);

        return jdbcTemplate.queryForObject(query, params, Boolean.class);
    }

    public Boolean emailAndPasswordExists(String email, String password) {
        final String query = "SELECT EXISTS (SELECT 1 FROM `users` WHERE email=:email AND password=:password)";

        MapSqlParameterSource params = new MapSqlParameterSource(Map.of(
                "email", email,
                "password", password
        ));

        return jdbcTemplate.queryForObject(query, params, Boolean.class);
    }

    private MapSqlParameterSource mapUserToSqlParameters(User user) {
        return new MapSqlParameterSource(Map.of(
                "user_uuid", user.getUser_uuid(),
                "email", user.getEmail(),
                "password", user.getPassword(),
                "created_on", user.getCreated_on()
        ));
    }
}
