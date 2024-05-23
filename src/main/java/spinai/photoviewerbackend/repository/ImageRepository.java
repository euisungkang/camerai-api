package spinai.photoviewerbackend.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import spinai.photoviewerbackend.model.UploadedImage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ImageRepository {

    @Qualifier("imagesNamedParameterJdbcTemplate")
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public int[] saveImages(List<UploadedImage> images) {
        final String query = "INSERT INTO `images` " +
                "(image_uuid, user_uuid, image_url, deleted, uploaded) " +
                "VALUES (:image_uuid, :user_uuid, :image_url, :deleted, :uploaded)";

        SqlParameterSource[] params = SqlParameterSourceUtils.createBatch(images);

        return jdbcTemplate.batchUpdate(query, params);
    }

    public int updateImageDescription(String image_uuid, String description) {
        final String query = "UPDATE `images` " +
                "SET description=:description " +
                "WHERE image_uuid=:image_uuid";

        MapSqlParameterSource params = new MapSqlParameterSource(Map.of(
                "description", description,
                "image_uuid", image_uuid
        ));

        return jdbcTemplate.update(query, params);
    }

    public int updateImageTagsUUID(String image_uuid, String tags_uuid) {
        final String query = "UPDATE `images` " +
                "SET tags_uuid=:tags_uuid " +
                "WHERE image_uuid=:image_uuid";

        MapSqlParameterSource params = new MapSqlParameterSource(Map.of(
                "tags_uuid", tags_uuid,
                "image_uuid", image_uuid
        ));

        return jdbcTemplate.update(query, params);
    }

    public int[] saveImageTagsArray(String tags_uuid, List<String> tags) {
        final String query = "INSERT INTO `images_tags` " +
                "(tags_uuid, tag) " +
                "VALUES (:tags_uuid, :tag)";

        List<Map<String, String>> entries = new ArrayList<>();
        for (String tag : tags) {
            entries.add(Map.of(
                    "tags_uuid", tags_uuid,
                    "tag", tag
            ));
        }

        SqlParameterSource[] params = SqlParameterSourceUtils.createBatch(entries);

        return jdbcTemplate.batchUpdate(query, params);

    }
}
