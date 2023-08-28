package com.alivassopoli.adapter.dynamodb;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ShoppingListRepository extends AbstractShoppingListRepository {

    private final DynamoDbClient dynamoDB;

    public ShoppingListRepository(final DynamoDbClient dynamoDB) {
        this.dynamoDB = dynamoDB;
    }

    public List<ShoppingListItem> findAllByOwner(final String ownerTelegramChatId) {

        //TODO: QueryRequest instead of ScanRequest
//        QueryRequest x = QueryRequest.builder()
//                .build();
//        dynamoDB.query(x);

        return dynamoDB.scanPaginator(scanRequest(ownerTelegramChatId)).items().stream()
                .map(ShoppingListItem::from)
                .collect(Collectors.toList());
    }

    public PutItemResponse add(ShoppingListItem fruit) {
        return dynamoDB.putItem(putRequest(fruit));
    }

    public ShoppingListItem get(String name, String ownerTelegramChatId) {
        return ShoppingListItem.from(dynamoDB.getItem(getRequest(name, ownerTelegramChatId)).item());
    }

    public DeleteItemResponse delete(String name, String ownerTelegramChatId) {
        return dynamoDB.deleteItem(deleteRequest(name, ownerTelegramChatId));
    }
}
