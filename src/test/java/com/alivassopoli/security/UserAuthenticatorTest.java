package com.alivassopoli.security;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
@TestProfile(UserAuthenticatorTestProfile.class)
public class UserAuthenticatorTest {

    @Inject
    UserAuthenticator userAuthenticator;

    @Test
    public void testGetChatRole_admin() {
        Assertions.assertEquals(Role.ADMIN, userAuthenticator.getChatRole(123L));
    }

    @Test
    public void testGetChatRole_user() {
        Assertions.assertEquals(Role.USER, userAuthenticator.getChatRole(456L));
        Assertions.assertEquals(Role.USER, userAuthenticator.getChatRole(789L));
    }

    @Test
    public void testGetChatRole_unknown() {
        Assertions.assertEquals(Role.UNKNOWN, userAuthenticator.getChatRole(999L));
    }
}
