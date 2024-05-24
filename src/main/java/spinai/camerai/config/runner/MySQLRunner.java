package spinai.camerai.config.runner;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class MySQLRunner implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(MySQLRunner.class);

    @Qualifier("imagesDataSource")
    private final DataSource imagesDataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = imagesDataSource.getConnection()) {
            String URL = connection.getMetaData().getURL();
            String User = connection.getMetaData().getUserName();
            logger.info(connection + " images Connection");
        } catch (SQLException ex) {
            throw new RuntimeException("Error connecting to MySQL database: images", ex);
        }
    }
}