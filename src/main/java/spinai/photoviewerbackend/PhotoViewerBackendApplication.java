package spinai.photoviewerbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class PhotoViewerBackendApplication {

	private static final String TIME_ZONE_ID = "UTC";

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone(TIME_ZONE_ID));
		System.out.println("Application time zone: " + TimeZone.getDefault().getID());
		SpringApplication.run(PhotoViewerBackendApplication.class, args);
	}

}
