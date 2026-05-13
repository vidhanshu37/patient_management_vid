import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class PatientIntegrationTest {
    @BeforeAll
    static void stepUp() {
        RestAssured.baseURI = "http://localhost:4004";
    }

//    eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlckB0ZXN0LmNvbSIsInJvbGUiOiJBRE1JTiIsImlhdCI6MTc3ODY5OTIxNywiZXhwIjoxNzc4NzM1MjE3fQ.GKegN-zciUzM2SNZ13zD_uWzwEjN3Sr2JOYxu3QspAU
    @Test
    public void shouldReturnPatientsWithValidToken() {
        String loginPayload = """
                {
                    "email": "testuser@test.com",
                    "password": "password123"
                }
                """;


        // build HTTP request
        String token = given()
                .contentType("application/json")
                .body(loginPayload)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .get("token");

        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/patients")
                .then()
                .statusCode(200)
                .body("patients", notNullValue());

    }
}
