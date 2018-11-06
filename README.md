# PtiChat App

Cette application a été développée en 9 demi-journées dans le cadre d'un projet scalaire. Il s'agit d'un chat qui permet à ses utilisateurs d'envoyer et recevoir des messages d'autres personnes connectées. Conçu pour supporter Android 7.0+, elle est entièrement écrite en Java.

## Getting Started

Ce projet se divise en deux parties : un fichier pour le Back en Java 8.0 contenant la BDD et les routes API, et un autre pour l'app elle-même.

### Set up the Back

// TODO (work in progress)

### Launch the App

// TODO (work in progress)

## API specification

### Front-end -> Back-end messages

#### Just send a message to be displayed on the Server

```json
{
  "type": "justText",
  "content": "Hello there"
}
```

#### New user

```json
{
  "type": "createNewUser",
  "userId": "user id",
  "password": "user password"
}
```

#### Edit user

```json
{
  "type": "editUser",
  "userId": "user id",
  "pseudo": "user (new?) pseudo",
  "status": "user (new?) status"
}
```

#### Edit chat

```json
{
  "type": "editChat",
  "chatId": "chat id",
  "chatName": "chat (new?) name"
}
```

#### Delete user

```json
{
  "type": "deleteUser",
  "userId": "user id"
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

#### Delete chat

```json
{
  "type": "deleteChat",
  "chatId": "chat id"
}
```

#### Get list of users

```json
{
  "type": "getListOfUsers"
}
```

#### Get list of users in a chat

```json
{
  "type": "getGroupMembers",
  "chatId": "chat id"
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
  "connection": true,
  "userId": "user id"
}
```

(`connection` is `true` to announce a connection with the id `userId`, `false` for a disconnection)

### Back-end -> Front-end messages

#### Just send a message to be displayed on the App

```json
{
  "type": "justText",
  "content": "Hello there"
}
```

#### Send "you are connected"

```json
{
  "type": "loginAcceptance",
  "user": {"userId": "user id", "pseudo": "pseudo", "profilePicture": "...", "status": "...", "isConnected": true},
  "value": true,
  "message": ""
}
```

#### Validate profile edition

```json
{
  "type": "userEditAcceptance",
  "user": {"userId": "user id", "pseudo": "pseudo", "profilePicture": "...", "status": "...", "isConnected": true},
  "value": true,
  "message": ""
}
```

#### Validate chat edition

```json
{
  "type": "chatEditAcceptance",
  "chat": {"chatId": "chat id", "chatName": "chat display name", "isPrivate": false},
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
  "chats": [{"chatId": "chat id", "chatName": "chat display name", "isPrivate": false},
            {"chatId": "chat id", "chatName": "chat display name", "isPrivate": false}]
}
```

#### Send list of users in a given chat

```json
{
  "type": "listOfChatMembers",
  "chatId": "chat id",
  "users": [{"userId": "user id", "pseudo": "pseudo", "profilePicture": "...", "status": "...", "isConnected": true},
            {"userId": "user id", "pseudo": "pseudo", "profilePicture": "...", "status": "...", "isConnected": false}]
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
  "chat": {"chatId": "chat id", "chatName": "chat display name", "isPrivate": false},
  "message": {"messageId": "message id", "content": "...", "date": "2018-10-16 14:45:09", "senderId": "senderId", "chatId": "chatId", "read": true}
}
```

#### Notify the user that someone is now online or offline

```json
{
  "type": "announceConnection",
  "connection": true,
  "userId": "user id"
}
```

## Built With

* [Android](https://developer.android.com) - The app runs on it

## Authors

* [Aymeric Bernard](https://github.com/AymericBebert)
* [Come de Cerval](https://github.com/CarbonC)
* [Solen Le Roux--Couloigner](https://github.com/Tishwa)

## Licence

Ce project est sous licence MIT.

## Remerciements

* MSN Messenger pour l'inspirations
* Android Studio pour l'avatar des utilisateurs
* [Freepik](https://www.flaticon.com/authors/freepik) de [Flaticon](/www.flaticon.com) pour l'icone des groupes
* [hakule](https://www.istockphoto.com/fr/portfolio/hakule?mediatype=illustration&sort=best) de [istockphoto](www.istockphoto.com) pour l'icone de l'app
