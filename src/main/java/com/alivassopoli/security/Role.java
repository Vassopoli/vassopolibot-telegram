package com.alivassopoli.security;

public enum Role {

    //TODO: Change this numerical logic to a logic of many roles per user
    // Admin ->
    // - All
    // User ->
    // Unknown ->
    // - None
    ADMIN(3), USER(2), UNKNOWN(1);

    private final int code;

    Role(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
