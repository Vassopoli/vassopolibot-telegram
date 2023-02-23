# vassopoli-telegram-webhook

This project is the source code of the [VassopoliBot](https://t.me/VassopoliBot). It communicates to the Telegram API using webhook.

The bot receives a message, try to identify the intention of this message, and delegates the message to its respective service.

If no intention is recognized, it assumes that the message should be sent to the bot admin. In this case, the bot only acts like a proxy, forwarding the messages.

## Services

- Shopping List
- Message Sender
