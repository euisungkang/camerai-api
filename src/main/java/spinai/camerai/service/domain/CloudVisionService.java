package spinai.camerai.service.domain;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CloudVisionService {

    public List<String> analyzeImageTags(MultipartFile image);
}
