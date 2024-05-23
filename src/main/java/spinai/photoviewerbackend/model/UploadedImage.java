package spinai.photoviewerbackend.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import spinai.photoviewerbackend.util.LocalDateTimeAttributeConverter;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Table(name = "images")
public class UploadedImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "image_uuid", nullable = false)
    private String image_uuid;

    @Column(name = "user_uuid", nullable = false)
    private String user_uuid;

    @Column(name = "image_url", nullable = false)
    private String image_url;

    @Column(name = "tags_uuid", nullable = true)
    private String tags_uuid;

    @Column(name = "tags", nullable = false)
    private List<String> tags;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "uploaded", nullable = false)
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime uploaded;
}
