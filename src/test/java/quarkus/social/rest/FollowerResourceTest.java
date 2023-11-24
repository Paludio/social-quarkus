package quarkus.social.rest;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.json.bind.JsonbBuilder;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import quarkus.social.domain.model.Follower;
import quarkus.social.domain.model.User;
import quarkus.social.domain.repository.FollowerRepository;
import quarkus.social.domain.repository.UserRepository;
import quarkus.social.exception.ErrorMessages;
import quarkus.social.rest.dto.FollowerRequest;

import java.sql.Date;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FollowerResourceTest {
    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;
    Long userId;
    Long followerId;

    @BeforeEach
    @Transactional
    void setUp() {
        // Usuário a ser seguido
        User user = new User();
        user.setAge(Date.valueOf("2023-11-22"));
        user.setName("Fulano");
        userRepository.persist(user);
        userId = user.getId();

        // Usuário seguidor
        User follower = new User();
        follower.setAge(Date.valueOf("2023-11-22"));
        follower.setName("Cicrano");
        userRepository.persist(follower);
        followerId = follower.getId();

        // Cria um seguidor
        Follower followerEntity = new Follower();
        followerEntity.setFollower(follower);
        followerEntity.setUser(user);
        followerRepository.persist(followerEntity);
    }

    @Test
    @DisplayName("Should return status 409 for the same id")
    @Order(1)
    public void sameUserAsFollowerTest() {
        FollowerRequest followerRequest = new FollowerRequest();
        followerRequest.setFollowerId(userId);

        Response response = given()
                .contentType(ContentType.JSON)
                .pathParams("userId", userId)
                .body(JsonbBuilder.create().toJson(followerRequest))
                .when()
                .put()
                .then()
                .extract().response();

        assertEquals(409, response.getStatusCode());
        assertEquals(ErrorMessages.CONFLICT.toString(), response.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("Should return status 404 when user id does't exist")
    @Order(2)
    public void inexistentUserIdFollowerPutTest() {
        Long inexistentId = 99L;
        FollowerRequest followerRequest = new FollowerRequest();
        followerRequest.setFollowerId(userId);

        Response response = given()
                .contentType(ContentType.JSON)
                .pathParams("userId", inexistentId)
                .body(JsonbBuilder.create().toJson(followerRequest))
                .when()
                .put()
                .then()
                .extract().response();

        assertEquals(404, response.getStatusCode());
        assertEquals(ErrorMessages.USER_NOT_FOUND.toString(), response.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("Should follow a user")
    @Order(3)
    public void followUserTest() {
        FollowerRequest followerRequest = new FollowerRequest();
        followerRequest.setFollowerId(followerId);

        Response response = given()
                .contentType(ContentType.JSON)
                .pathParams("userId", userId)
                .body(JsonbBuilder.create().toJson(followerRequest))
                .when()
                .put()
                .then()
                .extract().response();

        assertEquals(204, response.getStatusCode());
    }

    @Test
    @DisplayName("Should return status 404 when user id does't exist")
    @Order(4)
    public void inexistentUserIdFollowerGetTest() {
        Long inexistentId = 99L;

        Response response = given()
                .contentType(ContentType.JSON)
                .pathParams("userId", inexistentId)
                .when()
                .get()
                .then()
                .extract().response();

        assertEquals(404, response.getStatusCode());
        assertEquals(ErrorMessages.USER_NOT_FOUND.toString(), response.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("Should list a users followers")
    @Order(5)
    public void listUsersFollowersTest() {
        Response response = given()
                .contentType(ContentType.JSON)
                .pathParams("userId", userId)
                .when()
                .get()
                .then()
                .extract().response();

        Integer followersCount = response.jsonPath().getInt("followersCount");
        List<Object> followersContent = response.jsonPath().getList("content");

        assertEquals(200, response.getStatusCode());
        assertEquals(1, followersCount);
        assertEquals(1, followersContent.size());
    }

    @Test
    @DisplayName("Should return status 404 when user id does't exist")
    @Order(6)
    public void deleteAFollowerTest() {
        Long inexistentId = 99L;
        Response response = given()
                .contentType(ContentType.JSON)
                .pathParams("userId", inexistentId)
                .when()
                .delete()
                .then()
                .extract().response();

        assertEquals(404, response.getStatusCode());
        assertEquals(ErrorMessages.USER_NOT_FOUND.toString(), response.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("Should delete a user followers")
    @Order(7)
    public void deleteUsersFollowersTest() {
        Response response = given()
                .contentType(ContentType.JSON)
                .pathParams("userId", userId)
                .queryParams("followerId", followerId)
                .when()
                .delete()
                .then()
                .extract().response();

        assertEquals(204, response.getStatusCode());
    }
}