# PtiChat App

As part of a school project, we developed a MSN-like app that allows users to send and receive messages (and has a few nice features). It runs on Android 7.0+ and is written in Java.

## Getting Started

This project has two folders: one for the back in Java 8.0 and one for the Android app.

### Back

// TODO (work in progress)

### App

// TODO (work in progress)

## API specification

### Front-end -> Back-end messages

#### New user

```json
{
  "type": "createNewUser",
  "userId": "user id",
  "password": "user password"
}
```

#### New chat

```json
{
  "type": "createNewChat",
  "chatName": "the chat name",
  "users": ["userId01", "userId02", "userId03"]
}
```

#### New message

```json
{
  "type": "createNewMessage",
  "content": "Message content",
  "senderId": "sender id",
  "chatId": "chat id"
}
```

#### Get list of users

```json
{
  "type": "getListOfUsers"
}
```

#### Get list of chats

```json
{
  "type": "getListOfChats",
  "userId": "user id of the requester"
}
```

#### Get list of messages in one chat

```json
{
  "type": "getListOfMessages",
  "chatId": "chat id from which to get the messages"
}
```

##### Get list of messages in a private chat

```json
{
  "type": "getPrivateMessages",
  "userId1": "user id of the first user",
  "userId2": "user id of the second user"
}
```

#### Send a new message in a chat

```json
{
  "type": "sendNewMessage",
  "message": {"messageId": "message id", "content": "...", "date": "2018-10-16 14:45:09", "senderId": "senderId", "chatId": "chatId", "read": true}
}
```

#### Announce who you are when you reconnect to the server without going through the login page

```json
{
  "type": "announceConnection",
  "userId": "user id"
}
```

#### Just send a message to be displayed on the other side

```json
{
  "type": "justText",
  "content": "Hello there"
}
```

### Back-end -> Front-end messages

#### Send "you are connected"

```json
{
  "type": "loginAcceptance",
  "user": {"userId": "user id", "pseudo": "pseudo", "profilePicture": "...", "status": "...", "isConnected": true},
  "value": true,
  "message": ""
}
```

#### Send list of users

```json
{
  "type": "listOfUsers",
  "users": [{"userId": "user id", "pseudo": "pseudo", "profilePicture": "...", "status": "...", "isConnected": true},
            {"userId": "user id", "pseudo": "pseudo", "profilePicture": "...", "status": "...", "isConnected": false}]
}
```

#### Send list of chats

```json
{
  "type": "listOfChats",
  "userId": "user id",
  "chats": [{"chatId": "chat id", "chatName": "chat display name"},
            {"chatId": "chat id", "chatName": "chat display name"}]
}
```

#### Send list of messages in one chat

```json
{
  "type": "listMessagesChat",
  "chatId": "chat id",
  "messages": [{"messageId": "message id", "content": "...", "date": "2018-10-16 14:45:09", "senderId": "senderId", "chatId": "chatId", "read": true},
               {"messageId": "message id", "content": "...", "date": "2018-10-16 21:47:00", "senderId": "senderId", "chatId": "chatId", "read": false}]
}
```

#### New message appeared in a chat

```json
{
  "type": "newMessageInChat",
  "chatId": "chat id",
  "message": {"messageId": "message id", "content": "...", "date": "2018-10-16 14:45:09", "senderId": "senderId", "chatId": "chatId", "read": true}
}
```

#### Just send a message to be displayed on the other side

```json
{
  "type": "justText",
  "content": "Hello there"
}
```

## Built With

* [Android](https://developer.android.com) - The app runs on it

## Authors

* [Aymeric Bernard](https://github.com/AymericBebert)
* [Come de Cerval](https://github.com/CarbonC)
* [Solen Le Roux--Couloigner](https://github.com/Tishwa)

## License

This project is licensed under the MIT License

## Acknowledgments

* MSN for the inspiration
