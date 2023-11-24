package quarkus.social.rest;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jakarta.json.bind.JsonbBuilder;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import quarkus.social.rest.dto.CreateUserRequest;
import quarkus.social.rest.dto.ResponseError;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {
    @TestHTTPResource("/users")
    URL apiURL;

    @Test
    @DisplayName("Should create an user successfully")
    @Order(1)
    public void createUserTest() {
        CreateUserRequest user = new CreateUserRequest();
        user.setName("teste");
        user.setAge("2002-03-07");

        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .body(JsonbBuilder.create().toJson(user))
                        .when()
                        .post(apiURL)
                        .then()
                        .extract().response();

        assertEquals(201, response.statusCode());
        assertNotNull(response.jsonPath().getString("id"));
    }

    @Test
    @DisplayName("Should return error when json is not valid")
    @Order(2)
    public void createUserValidationErrorTest() {
        CreateUserRequest user = new CreateUserRequest();
        user.setName(null);
        user.setAge(null);

        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .body(JsonbBuilder.create().toJson(user))
                        .when()
                        .post(apiURL)
                        .then()
                        .extract().response();

        assertEquals(ResponseError.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Validation error", response.jsonPath().getString("message"));

        List<Map<String, String>> errors = response.jsonPath().getList("errors");
        assertNotNull(errors.get(0).get("message"));
        assertNotNull(errors.get(1).get("message"));
    }

    @Test
    @DisplayName("Should list all users")
    @Order(3)
    public void listAllUsersTest() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .get(apiURL)
                .then()
                .statusCode(200)
                .body("size()", Matchers.is(1));
    }

    @Test
    @DisplayName("Should throw error when is send a wrong id")
    @Order(4)
    public void updateUserErrorTest() {
        CreateUserRequest user = new CreateUserRequest();
        user.setName("Test");
        user.setAge("2023-11-22");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(JsonbBuilder.create().toJson(user))
                .put(String.format("%s/2", apiURL))
                .then()
                .extract().response();

        assertEquals(404, response.getStatusCode());
        assertEquals("User not found", response.jsonPath().getString("message"));
    }

    @Test
    @DisplayName("Should update user by id")
    @Order(5)
    public void updateUserTest() {
        CreateUserRequest user = new CreateUserRequest();
        user.setName("Test");
        user.setAge("2023-11-22");

        Response response = given()
                .contentType(ContentType.JSON)
                .body(JsonbBuilder.create().toJson(user))
                .put(String.format("%s/1", apiURL))
                .then()
                .extract().response();

        assertEquals(201, response.getStatusCode());
        assertEquals(1, response.jsonPath().getLong("id"));
        assertEquals("Test", response.jsonPath().getString("name"));
    }

    @Test
    @DisplayName("Should delete user by id")
    @Order(6)
    public void deleteUserTest() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .delete(String.format("%s/1", apiURL))
                .then()
                .statusCode(204);
    }
}