package spinai.camerai.repository;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import spinai.camerai.model.UploadedImage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ImageRepository {

    @Qualifier("imagesNamedParameterJdbcTemplate")
    private final NamedParameterJdbcTemplate jdbcTemplate;

    private final static Integer OFFSET_BASE = 20;

    public List<UploadedImage> getImages(String user_uuid, int page) {
        int offset = page * OFFSET_BASE;

        final String query = "SELECT image_uuid, image_url, tags_uuid, uploaded " +
                "FROM `images` " +
                "WHERE deleted=0 AND user_uuid=:user_uuid " +
                "ORDER BY uploaded DESC " +
                "LIMIT :limit OFFSET :offset";

        MapSqlParameterSource params = new MapSqlParameterSource(Map.of(
                "user_uuid", user_uuid,
                "limit", OFFSET_BASE,
                "offset", offset
        ));

        return jdbcTemplate.query(query, params, this::mapRowToUploadedImage);
    }

    public UploadedImage getImageDetails(String image_uuid) {
        final String query = "SELECT image_uuid, image_url, tags_uuid, description, uploaded " +
                "FROM `images` " +
                "WHERE image_uuid=:image_uuid AND deleted=0";

        MapSqlParameterSource params = new MapSqlParameterSource("image_uuid", image_uuid);

        return jdbcTemplate.queryForObject(query, params, this::mapRowToImageDetail);
    }

    public List<String> getIImageTags(String tags_uuid) {
        final String query = "SELECT JSON_ARRAYAGG(tag) AS tags " +
                "FROM `images_tags` " +
                "WHERE tags_uuid=:tags_uuid";

        MapSqlParameterSource params = new MapSqlParameterSource("tags_uuid", tags_uuid);

        return jdbcTemplate.queryForObject(query, params, this::mapRowToTags);
    }

//    public Optional<Product> getProductById(String user_uuid, String product_uuid) {
//        final String query = "SELECT product_uuid, user_uuid, name, price, months_used, description, location, category, sold, likes, views, chats, deleted, " +
//                "(SELECT EXISTS (SELECT 1 FROM `users_liked` WHERE users_liked.user_uuid=:user_uuid AND users_liked.product_uuid=products_normal.product_uuid)) AS user_liked, " +
//                "(SELECT JSON_ARRAYAGG(url) FROM `products_images` WHERE product_uuid=:product_uuid) AS images " +
//                "FROM `products_normal` " +
//                "WHERE product_uuid=:product_uuid; " +
//                "UPDATE `products_normal` SET views = views + 1 WHERE product_uuid=:product_uuid;";
//
//        MapSqlParameterSource params = new MapSqlParameterSource(Map.of(
//                "user_uuid", user_uuid,
//                "product_uuid", product_uuid
//        ));
//
//        return Optional.ofNullable(jdbcTemplate.queryForObject(query, params, this::mapRowToProductWithImages));
//    }

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

    private UploadedImage mapRowToUploadedImage(ResultSet rs, Integer rowNum) throws SQLException {
        return UploadedImage.builder()
                .image_uuid(rs.getString("image_uuid"))
                .image_url(rs.getString("image_url"))
                .tags_uuid(rs.getString("tags_uuid"))
                .uploaded(rs.getTimestamp("uploaded").toLocalDateTime())
                .build();
    }

    private UploadedImage mapRowToImageDetail(ResultSet rs, Integer rowNum) throws SQLException {
        return UploadedImage.builder()
                .image_uuid(rs.getString("image_uuid"))
                .image_url(rs.getString("image_url"))
                .tags_uuid(rs.getString("tags_uuid"))
                .description(rs.getString("description"))
                .uploaded(rs.getTimestamp("uploaded").toLocalDateTime())
                .build();
    }

    private List<String> mapRowToTags(ResultSet rs, Integer rowNum) throws SQLException {
        JSONArray tagsJSON = new JSONArray(rs.getString("tags"));
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < tagsJSON.length(); i++) {
            tags.add(tagsJSON.get(i).toString());
        }
        return tags;
    }
}
