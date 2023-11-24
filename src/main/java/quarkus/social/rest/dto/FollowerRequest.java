package quarkus.social.rest.dto;

import lombok.Data;

@Data
public class FollowerRequest {
    private Long followerId;
}
