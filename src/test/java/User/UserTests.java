package User;

import Entities.User;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import javafx.beans.binding.Bindings;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;


import static com.sun.javafx.fxml.expression.Expression.equalTo;
import static com.sun.javafx.fxml.expression.Expression.lessThan;
import static io.restassured.RestAssured.given;
import static io.restassured.config.LogConfig.logConfig;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import static javafx.beans.binding.Bindings.when;
import static org.codehaus.groovy.ast.tools.GeneralUtils.param;
import static org.hamcrest.Matcher.*;
import static org.hamcrest.Matchers.isA;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTests {
private static User user;
    public static Faker faker;
    public static RequestSpecification request;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://petstore.swagger.io/v2";

        faker = new Faker();

        user = new User(faker.name().username(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().safeEmailAddress(),
                faker.internet().password(8, 10),
                faker.phoneNumber().toString());

    }

    @BeforeEach
    void setRequest() {
        request = given().config(RestAssuredConfig.config().logConfig(logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                //. config validação de falha gera um report de log
                .header("api-key", "special-key")
                .contentType(ContentType.JSON);
    }

    @Test
    @Order(1)
    public void CreatNewUser_WhithValidData_ReturnOK() {
        request
                .body(user)
                .when()
                .post("/user")
                .then()
                .assertThat().statusCode(200).and()
                .body("code", Matchers.equalTo(200))
                .body("type", Matchers.equalTo("unknown"))
                .body("message", isA(String.class))
                .body("size()", Matchers.equalTo(3));


    }

    @Test
    @Order(2)
    public void GetLogin_ValidUser_ReturnOK() {
        request
                .param("username", user.getUsername())
                .param("password", user.getPassword())
                .when()
                .get("/user/login")
                .then()
                .assertThat()
                .statusCode(200)
                .and().time(Matchers.lessThan(2000l))
                .and().body(matchesJsonSchemaInClasspath("LoginResponseSchema.json"));


    }

    @Test
    @Order(3)
    public void GetUserByUsername_userIsValid_ReturnOK() {
        request
                .when()
                .get("/user/" + user.getUsername())
                .then()
                .assertThat().statusCode(200).and().time(Matchers.lessThan(2000L))
                .and().body("firstName", Matchers.equalTo(user.getFirstname()));


    }

    @Test
    @Order(4)
    public void DeleteUserByUsername_UserExists_ReturnOK() {
        request
                .when()
                .get("/user/" + user.getUsername())
                .then()
                .assertThat().statusCode(200).and().time(Matchers.lessThan(2000l))
                .log();
    }

    @Test

    public void CreateNewUser_WhithInvalidBody_ReturnBadRequest() {

        Response response = request
                .body("teste")
                .when()
                .post("/user")
                .then().extract().response();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(400, response.statusCode());
        Assertions.assertEquals(true, response.getBody().asPrettyString().contains("unknown"));
        Assertions.assertEquals(3, response.body().jsonPath().getMap("$").size());
    }
}




