import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class AuthIntegrationTest {
    @BeforeAll
    static void stepUp() {
        RestAssured.baseURI = "http://localhost:4004";
    }

    @Test
    public void shouldReturnOkWithValidToken() {
        // 1. Arrange
        // 2. Act
        // 3. Assert

        String loginPayload = """
                {
                    "email": "testuser@test.com",
                    "password": "password123"
                }
                """;


        // build HTTP request
        Response response = given() // 1. Arrange (import from - RestAssured.given)
                .contentType("application/json")
                .body(loginPayload)
                .when() // 2. Act
                .post("/auth/login")
                .then() // 3. Assert
                .statusCode(200)
                .body("token", notNullValue())  // import from hamcrest.Matchers.notNullValue
                .extract().response();

        System.out.println("Response: " + response.jsonPath().getString("token"));
    }
}