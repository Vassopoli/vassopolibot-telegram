package com.alivassopoli.adapter.dynamodb;

import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Map;

public abstract class AbstractShoppingListRepository {

    private static final String SHOPPING_LIST_TABLE_NAME = "shopping-list";
    protected static final String SHOPPING_LIST_ITEM_COL = "item";
    protected static final String SHOPPING_LIST_CATEGORY_COL = "category";
    protected static final String SHOPPING_LIST_CATEGORY_MARKET_VAL = "market";
    protected static final String SHOPPING_LIST_CREATEDAT_COL = "createdAt";

    //TODO: Change scan to normal query
    protected ScanRequest scanRequest() {
        final Map<String, AttributeValue> expressionAttributeValues =
                Map.of(":category", AttributeValue.builder().s(SHOPPING_LIST_CATEGORY_MARKET_VAL).build());

        return ScanRequest.builder()
                .tableName(SHOPPING_LIST_TABLE_NAME)
                .filterExpression("category = :category")
                .expressionAttributeValues(expressionAttributeValues)
                .build();
    }

    protected PutItemRequest putRequest(final ShoppingListItem shoppingListItem) {
        final Map<String, AttributeValue> item = Map.of(
                SHOPPING_LIST_ITEM_COL, AttributeValue.builder().s(shoppingListItem.getItem()).build(),
                SHOPPING_LIST_CATEGORY_COL, AttributeValue.builder().s(shoppingListItem.getCategory()).build(),
                SHOPPING_LIST_CREATEDAT_COL, AttributeValue.builder().s(shoppingListItem.getCreatedAt()).build()
        );

        return PutItemRequest.builder()
                .tableName(SHOPPING_LIST_TABLE_NAME)
                .item(item)
                .build();
    }

    protected GetItemRequest getRequest(final String name) {
        final Map<String, AttributeValue> key = Map.of(
                SHOPPING_LIST_ITEM_COL, AttributeValue.builder().s(name).build(),
                SHOPPING_LIST_CATEGORY_COL, AttributeValue.builder().s(SHOPPING_LIST_CATEGORY_MARKET_VAL).build());

        return GetItemRequest.builder()
                .tableName(SHOPPING_LIST_TABLE_NAME)
                .key(key)
                .attributesToGet(SHOPPING_LIST_ITEM_COL, SHOPPING_LIST_CATEGORY_COL, SHOPPING_LIST_CREATEDAT_COL)
                .build();
    }

    protected DeleteItemRequest deleteRequest(final String name) {
        final Map<String, AttributeValue> key = Map.of(
                SHOPPING_LIST_ITEM_COL, AttributeValue.builder().s(name).build(),
                SHOPPING_LIST_CATEGORY_COL, AttributeValue.builder().s(SHOPPING_LIST_CATEGORY_MARKET_VAL).build());

        return DeleteItemRequest.builder()
                .tableName(SHOPPING_LIST_TABLE_NAME)
                .key(key)
                .returnValues(ReturnValue.ALL_OLD)
                .build();
    }
}
