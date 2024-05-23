package spinai.photoviewerbackend.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class JDBCConfig {

    @Bean
    public NamedParameterJdbcTemplate imagesNamedParameterJdbcTemplate(@Qualifier("imagesDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}