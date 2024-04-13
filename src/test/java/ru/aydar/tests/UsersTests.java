package ru.aydar.tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.aydar.models.*;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.aydar.specs.Specs.*;
import static ru.aydar.utils.SearchByFieldUtils.getLastNameByFirstName;

@Epic("API сайта Reqres.in")
@Feature("API пользователей (/users)")
@DisplayName("Тесты работы с пользователями (/users)")
public class UsersTests extends TestBase {
    @Test
    @DisplayName("Проверка создания пользователя")
    void createUserTest() {
        UserCreateUpdateRequestModel body = new UserCreateUpdateRequestModel();
        body.setName("Vasya Pupkin");
        body.setJob("Jester");
        UserCreateResponseModel response = step("Отправка запроса на создание пользователя", () ->
                given(basicRequestSpec)
                        .body(body)
                        .when()
                        .post("/users")
                        .then()
                        .spec(created201ResponseSpec)
                        .extract().as(UserCreateResponseModel.class));
        SoftAssertions userInfo = new SoftAssertions();
        userInfo.assertThat(response.getName()).as("Имя пользователя").isEqualTo("Vasya Pupkin");
        userInfo.assertThat(response.getJob()).as("Профессия пользователя").isEqualTo("Jester");
        step("Проверка полей созданного пользователя в ответе", () -> userInfo.assertAll());
    }

    @Test
    @DisplayName("Проверка редактирования пользователя")
    void editUserTest() {
        UserCreateUpdateRequestModel body = new UserCreateUpdateRequestModel();
        body.setName("Emma Wong");
        body.setJob("Associate Jester");
        UserUpdateResponseModel response = step("Отправка запроса на редактирование пользователя", () ->
                given(basicRequestSpec)
                        .body(body)
                        .when()
                        .patch("/users/3")
                        .then()
                        .spec(ok200ResponseSpec)
                        .extract().as(UserUpdateResponseModel.class));
        SoftAssertions userInfo = new SoftAssertions();
        userInfo.assertThat(response.getName()).as("Имя пользователя").isEqualTo("Emma Wong");
        userInfo.assertThat(response.getJob()).as("Профессия пользователя").isEqualTo("Associate Jester");
        step("Проверка отредактированых полей пользователя в ответе", () -> userInfo.assertAll());
    }

    @Test
    @DisplayName("Проверка регистрации пользователя")
    void registerUserTest() {
        UserRegisterRequestModel body = new UserRegisterRequestModel();
        body.setEmail("eve.holt@reqres.in");
        body.setPassword("TotallyNotAJester2007");
        UserRegisterResponseModel response = step("Отправка запроса на регистрацию пользователя", () ->
                given(basicRequestSpec)
                        .body(body)
                        .when()
                        .post("/register")
                        .then()
                        .spec(ok200ResponseSpec)
                        .extract().as(UserRegisterResponseModel.class));
        step("Проверка токена зарегистрированного пользователя в ответе", () ->
                assertThat(response.getToken().length()).as("Количество символов в токене %s", response.getToken()).isEqualTo(17));
    }

    @Story("Список пользователей")
    @DisplayName("Список пользователей: ")
    @ParameterizedTest(name = "Проверка того, что у пользователя по имени {0} фамилия {1}")
    @CsvSource(value = {
            "George , Bluth",
            "Janet , Weaver",
            "Tracey , Ramos"
    })
    void checkUsersLastNameTest(String firstName, String lastName) {
        UserGetListResponseModel response = step("Отправка запроса на получение списка пользователей (первая страница)", () ->
                given(basicRequestSpec)
                        .when()
                        .get("/users")
                        .then()
                        .spec(ok200ResponseSpec)
                        .body(matchesJsonSchemaInClasspath("users_schema.json"))
                        .extract().as(UserGetListResponseModel.class));
        step("Проверка соответствия фамилии пользователя его имени в ответе", () ->
                assertThat(getLastNameByFirstName(response.getData(), firstName)).isEqualTo(lastName));
    }

    @Test
    @DisplayName("Проверка кода ответа 404 для несуществующего id пользователя")
    void checkUser404Test() {
        step("Отправка запроса на получение данных несуществующего пользователя", () ->
                given(basicRequestSpec)
                        .when()
                        .get("/users/13")
                        .then()
                        .spec(notFound404ResponseSpec));
    }

    @Test
    @DisplayName("Проверка кода ответа 204 при удалении пользователя (настоящего удаления на Reqres не предусмотрено)")
    void checkUserDeletion204Test() {
        step("Отправка запроса на удаление пользователя", () ->
                given(basicRequestSpec)
                        .when()
                        .delete("/users/2")
                        .then()
                        .spec(noContent204ResponseSpec));
    }
}
