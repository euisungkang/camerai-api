package spinai.camerai.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spinai.camerai.dto.request.ImageAnalysisRequest;
import spinai.camerai.dto.request.ImageTaggingRequest;
import spinai.camerai.dto.response.*;
import spinai.camerai.exception.InvalidInputException;
import spinai.camerai.model.UploadedImage;
import spinai.camerai.service.application.ImageService;
import spinai.camerai.service.application.TokenAuthService;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class ImageController {

    private final TokenAuthService tokenAuthService;
    private final ImageService imageService;

    private static final List<String> EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");

    @GetMapping("")
    public ResponseEntity<ImageListResponse> getImages(@RequestHeader("Authorization") String token, @RequestParam("page") int page) {
        if (page < 0) {
            throw new InvalidInputException("page cannot be negative");
        }

        String user_uuid = tokenAuthService.getUUID(token);

        ImageListResponse response = imageService.getImages(user_uuid, page);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{image_uuid}")
    public ResponseEntity<ImageDetailResponse> getImageDetail(@PathVariable("image_uuid") String image_uuid) {
        if (invalidUUID(image_uuid)) {
            throw new InvalidInputException("Invalid or empty uuid");
        }

        ImageDetailResponse response = imageService.getImageDetail(image_uuid);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<ImageUploadResponse> postImage(@RequestHeader("Authorization") String token, @RequestParam("images") MultipartFile[] images) {
        boolean valid = Arrays.stream(images).allMatch(this::valid);

        if (!valid) {
            throw new InvalidInputException("Invalid File. File extension or name is not supported");
        }

        String uuid = tokenAuthService.getUUID(token);

        ImageUploadResponse response = imageService.uploadImages(images, uuid);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/analyze")
    public ResponseEntity<ImageAnalysisResponse> postImageAnalysis(@RequestBody ImageAnalysisRequest request) {
        ImageAnalysisResponse analysis = imageService.analyzeImage(request.getImage_uuid(), request.getImage_url());

        return new ResponseEntity<>(analysis, HttpStatus.OK);
    }

    @PostMapping("/tags")
    public ResponseEntity<ImageTaggingResponse> postImageTagging(@RequestBody ImageTaggingRequest request) {
        ImageTaggingResponse tagging = imageService.tagImage(request.getImage_uuid(), request.getImage());

        return new ResponseEntity<>(tagging, HttpStatus.OK);
    }

    private boolean invalidUUID(String uuid) {
        return uuid == null || uuid.isEmpty() || uuid.isBlank();
    }

    private boolean valid(MultipartFile multipartFile) {
        if (Objects.isNull(multipartFile.getOriginalFilename())) {
            return false;
        }
        return !multipartFile.getOriginalFilename().trim().isEmpty() && EXTENSIONS.contains(FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
    }
}
