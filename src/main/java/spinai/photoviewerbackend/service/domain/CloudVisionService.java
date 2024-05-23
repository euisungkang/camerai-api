package spinai.photoviewerbackend.service.domain;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface CloudVisionService {

    public List<String> analyzeImageTags(String image_url);
}
