package com.alivassopoli.security;

import java.util.Set;

import static com.alivassopoli.security.Policy.*;

public enum Role {

    //TODO: Move this hardcode to a database
    ADMIN(Set.of(FIRST_GREETING_SENDER, INSTAGRAM_SAVER, MESSAGE_SENDER, SHOPPING_LIST_CREATOR, SHOPPING_LIST_DELETER, SHOPPING_LIST_PRINTER, SHOPPING_LIST_READER)),
    USER(Set.of(FIRST_GREETING_SENDER, MESSAGE_SENDER, SHOPPING_LIST_CREATOR, SHOPPING_LIST_DELETER, SHOPPING_LIST_PRINTER, SHOPPING_LIST_READER)),
    APTO(Set.of(FIRST_GREETING_SENDER, SHOPPING_LIST_CREATOR, SHOPPING_LIST_DELETER, SHOPPING_LIST_PRINTER, SHOPPING_LIST_READER)),
    UNKNOWN(Set.of(FIRST_GREETING_SENDER, MESSAGE_SENDER));

    private final Set<Policy> policySet;

    Role(final Set<Policy> policySet) {
        this.policySet = policySet;
    }

    public Set<Policy> getPolicySet() {
        return policySet;
    }
}
