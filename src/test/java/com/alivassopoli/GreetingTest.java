package com.alivassopoli;

import com.alivassopoli.configuration.MyURLs;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

@QuarkusTest
class GreetingTest {

//    @Test
//    void testPositive() {
//        //TODO: Mock TelegramBotsApi.registerBot method
//        RestAssured.when().get("/telegram-bot" + MyURLs.TELEGRAM_REGISTER_WEBHOOK_URL).then()
//                .contentType("application/json")
//                .statusCode(200)
//                .body(contains("webhookUrl"))
//                .body(contains("telegram-bot"));
//    }

    @Test
    void testNegative() {
        RestAssured.when().get("/telegram-bot" + MyURLs.TELEGRAM_REGISTER_WEBHOOK_URL).then()
                .contentType("application/json")
                .statusCode(500)
                .body("isEmpty()", Matchers.is(true));
    }
}
