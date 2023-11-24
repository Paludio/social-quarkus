package quarkus.social.rest.dto;

import lombok.Data;

@Data
public class CreatePostRequest {
    private String text;
}
