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

##### new user
```json
{
  "type": "createNewUser",
  "userId": "user id",
  "password": "user password"
}
```

##### new chat
```json
{
  "type": "createNewChat",
  "chatName": "the chat name",
  "users": ["userId01", "userId02", "userId03"]
}
```

##### new message
```json
{
  "type": "createNewMessage",
  "content": "Message content",
  "senderId": "sender id",
  "chatId": "chat id"
}
```

##### get list of users
```json
{
  "type": "getListOfUsers"
}
```

##### get list of chats
```json
{
  "type": "getListOfUsers",
  "userId": "user id of the requester"
}
```

##### get list of messages in one chat
```json
{
  "type": "getListOfChats",
  "userId": "user id of the requester"
}
```


### Back-end -> Front-end messages

##### send "you are connected"

##### send list of users
```json
{
  "type": "listOfUsers",
  "users": [{"userId": "user id", "pseudo": "pseudo", "profilePicture": "...", "status": "...", "isConnected": true},
            {"userId": "user id", "pseudo": "pseudo", "profilePicture": "...", "status": "...", "isConnected": false}]
}
```

##### send list of chats
```json
{
  "type": "listOfChats",
  "userId": "user id",
  "chats": [{"chatId": "chat id", "chatName": "chat display name"},
            {"chatId": "chat id", "chatName": "chat display name"}]
}
```

##### send list of messages in one chat
```json
{
  "type": "listMessagesChat",
  "chatId": "chat id",
  "messages": [{"messageId": "message id", "content": "...", "date": "2018-10-16 14:45:09", "senderId": "senderId", "chatId": "chatId", "read": true},
               {"messageId": "message id", "content": "...", "date": "2018-10-16 21:47:00", "senderId": "senderId", "chatId": "chatId", "read": false}]
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
