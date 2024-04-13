package ru.aydar.tests;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.aydar.models.ColorGetListResponseModel;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static ru.aydar.specs.Specs.*;
import static ru.aydar.utils.SearchByFieldUtils.getPantoneCodeByColorName;

@Epic("API сайта Reqres.in")
@Feature("API цветов (/unknown)")
@DisplayName("Тесты работы со списком цветов (/unknown)")
public class ColorsTests extends TestBase {
    @Story("Список цветов")
    @DisplayName("Список цветов: ")
    @ParameterizedTest(name = "Проверка того, что у цвета {0} номер Pantone равен {1}")
    @CsvSource(value = {
            "fuchsia rose , 17-2031",
            "aqua sky , 14-4811",
            "blue turquoise , 15-5217"
    })
    void checkPantoneCodeTest(String name, String code) {
        ColorGetListResponseModel response = step("Отправка запроса на получение списка цветов (первая страница)", () ->
                given(basicRequestSpec)
                        .when()
                        .get("/unknown")
                        .then()
                        .spec(ok200ResponseSpec)
                        .extract().as(ColorGetListResponseModel.class));
        step("Проверка соответствия Pantone-кода цвета его названию в ответе", () ->
                assertThat(getPantoneCodeByColorName(response.getData(), name)).as("Pantone-код").isEqualTo(code));
    }

    @Test
    @DisplayName("Проверка кода ответа 404 для несуществующего id цвета")
    void checkUnknown404Test() {
        step("Отправка запроса на получение данных несуществующего цвета", () ->
                given(basicRequestSpec)
                        .when()
                        .get("/unknown/13")
                        .then()
                        .spec(notFound404ResponseSpec));
    }
}
