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
import quarkus.social.domain.model.Post;
import quarkus.social.domain.model.User;
import quarkus.social.domain.repository.FollowerRepository;
import quarkus.social.domain.repository.PostRepository;
import quarkus.social.domain.repository.UserRepository;
import quarkus.social.exception.ErrorMessages;
import quarkus.social.rest.dto.CreatePostRequest;

import java.sql.Date;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(PostsResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostsResourceTest {
    @Inject
    UserRepository userRepository;
    @Inject
    FollowerRepository followerRepository;
    @Inject
    PostRepository postRepository;
    Long userId;
    Long userNotFollowerId;
    Long userFollowerId;

    @BeforeEach
    @Transactional
    public void setUp() {
        //usuario padrão dos testes
        User user = new User();
        user.setAge(Date.valueOf("2023-11-22"));
        user.setName("Fulano");
        userRepository.persist(user);
        userId = user.getId();

        //criada a postagem para o usuario
        Post post = new Post();
        post.setText("Hello");
        post.setUser(user);
        postRepository.persist(post);

        //usuario que não segue ninguém
        User userNotFollower = new User();
        userNotFollower.setAge(Date.valueOf("2023-11-22"));
        userNotFollower.setName("Cicrano");
        userRepository.persist(userNotFollower);
        userNotFollowerId = userNotFollower.getId();

        //usuário seguidor
        User userFollower = new User();
        userFollower.setAge(Date.valueOf("2023-11-22"));
        userFollower.setName("Terceiro");
        userRepository.persist(userFollower);
        userFollowerId = userFollower.getId();

        Follower follower = new Follower();
        follower.setUser(user);
        follower.setFollower(userFollower);
        followerRepository.persist(follower);
    }

    @Test
    @DisplayName("Should create a post for a user")
    @Order(1)
    public void createPostTest() {
        CreatePostRequest postRequest = new CreatePostRequest();
        postRequest.setText("Some text");

        given()
                .contentType(ContentType.JSON)
                .body(JsonbBuilder.create().toJson(postRequest))
                .pathParams("userId", userId)
                .when()
                .post()
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("Should return status 404 when the user not exist")
    @Order(2)
    public void createPostTestError() {
        Integer nonExistentUser = 99;

        Response response = given()
                .contentType(ContentType.JSON)
                .pathParams("userId", nonExistentUser)
                .when()
                .post()
                .then()
                .extract().response();

        assertEquals(404, response.getStatusCode());
        assertEquals(ErrorMessages.USER_NOT_FOUND.toString(), response.jsonPath().getString("message"));
    }


    @Test
    @DisplayName("Should return 400 when followerId header is not present")
    @Order(3)
    public void listPostFollowerHeaderNotSendTest() {
        Response response = given()
                .pathParams("userId", userId)
                .when()
                .get()
                .then()
                .extract().response();

        assertEquals(400, response.getStatusCode());
        assertEquals(ErrorMessages.HEADER_ERROR.toString(), response.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("Should return 404 when the user not exist")
    @Order(4)
    public void listPostUserNotFoundTest() {
        Integer nonExistentUser = 99;

        Response response = given()
                .pathParams("userId", nonExistentUser)
                .headers("followerId", userFollowerId)
                .when()
                .get()
                .then()
                .extract().response();
        assertEquals(404, response.getStatusCode());
        assertEquals(ErrorMessages.USER_NOT_FOUND.toString(), response.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("Should return 404 when follower does't exist")
    @Order(5)
    public void listPostFollowerDoesNotExistsTest() {
        Integer nonExistentFollower = 99;

        Response response = given()
                .pathParams("userId", userId)
                .headers("followerId", nonExistentFollower)
                .when()
                .get()
                .then()
                .extract().response();
        assertEquals(404, response.getStatusCode());
        assertEquals(ErrorMessages.USER_NOT_FOUND.toString(), response.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("Should return 403 when follower does't follow")
    @Order(6)
    public void listPostNotAFollowerTest() {
        Response response = given()
                .pathParams("userId", userId)
                .headers("followerId", userNotFollowerId)
                .when()
                .get()
                .then()
                .extract().response();
        assertEquals(403, response.getStatusCode());
        assertEquals(ErrorMessages.FORBIDDEN.toString(), response.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("Should return posts")
    @Order(7)
    public void listPostsTest() {
        Response response = given()
                .pathParams("userId", userId)
                .headers("followerId", userFollowerId)
                .when()
                .get()
                .then()
                .extract().response();
        List<Map<String, String>> list = response.jsonPath().getList("");
        System.out.println(list);
        assertEquals(200, response.getStatusCode());
        assertEquals("Hello", list.get(0).get("text"));
    }
}