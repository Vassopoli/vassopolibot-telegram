package com.alivassopoli.adapter.dynamodb;

import io.quarkus.runtime.annotations.RegisterForReflection;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.Objects;

@RegisterForReflection
public class ShoppingListItem {

    private String item;
    private String category;
    private String ownerTelegramChatId;
    private String ownerTelegramChatName;
    private String createdAt;

    public ShoppingListItem() {

    }

    public ShoppingListItem(String item, String category, String ownerTelegramChatId, String ownerTelegramChatName, String createdAt) {
        this.item = item;
        this.category = category;
        this.ownerTelegramChatId = ownerTelegramChatId;
        this.ownerTelegramChatName = ownerTelegramChatName;
        this.createdAt = createdAt;
    }

    public static ShoppingListItem from(Map<String, AttributeValue> item) {
        final ShoppingListItem fruit = new ShoppingListItem();
        if (item != null && !item.isEmpty()) {
            fruit.setItem(item.get(AbstractShoppingListRepository.SHOPPING_LIST_ITEM_COL).s());
            fruit.setCategory(item.get(AbstractShoppingListRepository.SHOPPING_LIST_CATEGORY_COL).s());
            fruit.setOwnerTelegramChatId(item.get(AbstractShoppingListRepository.SHOPPING_LIST_OWNER_TELEGRAM_CHAT_ID_COL).s());

            //NULLABLE
            final AttributeValue ownerTelegramChatName = item.get(AbstractShoppingListRepository.SHOPPING_LIST_OWNER_TELEGRAM_CHAT_NAME_COL);
            if (Objects.nonNull(ownerTelegramChatName)) {
                fruit.setOwnerTelegramChatName(ownerTelegramChatName.s());
            }

            //NULLABLE
            final AttributeValue createdAt = item.get(AbstractShoppingListRepository.SHOPPING_LIST_CREATEDAT_COL);
            if (Objects.nonNull(createdAt)) {
                fruit.setCreatedAt(createdAt.s());
            }
        }
        return fruit;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOwnerTelegramChatId() {
        return ownerTelegramChatId;
    }

    public void setOwnerTelegramChatId(String ownerTelegramChatId) {
        this.ownerTelegramChatId = ownerTelegramChatId;
    }

    public String getOwnerTelegramChatName() {
        return ownerTelegramChatName;
    }

    public void setOwnerTelegramChatName(String ownerTelegramChatName) {
        this.ownerTelegramChatName = ownerTelegramChatName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
