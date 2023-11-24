package quarkus.social.rest.dto;

import lombok.Data;
import quarkus.social.domain.model.Post;

import java.sql.Date;

@Data
public class PostResponse {
    private String text;
    private Date date;

    public static PostResponse fromEntity(Post post) {
        PostResponse postResponse = new PostResponse();
        postResponse.setDate(post.getDateTime());
        postResponse.setText(post.getText());

        return postResponse;
    }
}
