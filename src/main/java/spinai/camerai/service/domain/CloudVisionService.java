package spinai.camerai.service.domain;

import java.util.List;

public interface CloudVisionService {

    public List<String> analyzeImageTags(String image_url);
}
