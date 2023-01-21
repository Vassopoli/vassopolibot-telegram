package com.alivassopoli;

import com.alivassopoli.configuration.MyURLs;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.*;

@QuarkusTest
class GreetingTest {
    @Test
    void testJaxrs() {
        RestAssured.when().get("/telegram-bot" + MyURLs.TELEGRAM_REGISTER_URL).then()
                .contentType("application/octet-stream")
                .body(startsWith("URL: "))
                .body(endsWith("telegram-bot"));
    }
}
