package spinai.photoviewerbackend.controller;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spinai.photoviewerbackend.dto.request.ImageAnalysisRequest;
import spinai.photoviewerbackend.dto.response.ImageTaggingResponse;
import spinai.photoviewerbackend.dto.response.ImageUploadResponse;
import spinai.photoviewerbackend.exception.InvalidInputException;
import spinai.photoviewerbackend.dto.response.ImageAnalysisResponse;
import spinai.photoviewerbackend.service.application.ImageService;
import spinai.photoviewerbackend.service.application.TokenAuthService;

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
    public ResponseEntity<ImageTaggingResponse> postImageTagging(@RequestBody ImageAnalysisRequest request) {
        ImageTaggingResponse tagging = imageService.tagImage(request.getImage_uuid(), request.getImage_url());

        return new ResponseEntity<>(tagging, HttpStatus.OK);
    }

    private boolean valid(MultipartFile multipartFile) {
        if (Objects.isNull(multipartFile.getOriginalFilename())) {
            return false;
        }
        return !multipartFile.getOriginalFilename().trim().isEmpty() && EXTENSIONS.contains(FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
    }
}
