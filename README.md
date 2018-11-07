# PtiChat App

Cette application a été développée par 3 élèves en 9 demi-journées dans le cadre d'un projet scolaire
(néanmoins un certain nombre d'heures supplémentaires furent nécessaires pour avoir le produit que nous souhaitions).
Il s'agit d'un chat qui permet à ses utilisateurs d'envoyer et recevoir des messages d'autres personnes connectées.
Conçu pour supporter Android 7.0+, elle est entièrement écrite en Java.

## Lancer le projet

Ce projet se divise en deux parties : un dossier pour le Backend en Java 8.0 contenant la BDD et les routes API,
et un autre pour l'application mobile elle-même.

### Mettre en place le Backend

#### En ligne de commande

Si vous êtes dans un environnement mac ou linux, ouvrez votre terminal dans le dossier `BackServer` et exécutez les commandes suivantes :

```terminal
// Pour lancer les tests unitaires et build
$ ./gradlew build

// Pour lancer le serveur
$ ./gradlew run
```

Votre backend devrait maintenant être *up and running*. Notez bien le numéro de port (8059 par défaut) sur lequel il écoute.

### Lancer l'app

#### Lancer l'app via Android Studio

Si vous disposez d'Android Studio sur votre ordinateur, vous pouvez l'utiliser pour lancer l'app sur un émulateur ou sur votre téléphone
(version d'android >= 7.0).

#### Lancer l'app via l'APK

Sinon, vous pouvez télécharger et installer l'application à l'aide de l'APK sur votre téléphone.
Attention, elle apparaîtra comme provenant d'une "source inconnue",
vous devrez donc autoriser celles-ci à être installées sur votre téléphone le temps de l'installation.

#### Relier au serveur

Par défaut, lors de l'ouverture de l'app vous serez connecté au serveur `bebert.cs-campus.fr` qui est notre serveur de test.
Vous pouvez l'utiliser tel quel, ou vous relier au serveur que vous venez de lancer dans l'étape précédente.
Pour cela, vous aurez besoin de votre adresse IP (locale, et votre téléphone devra être connecté au même réseau local
que votre ordinateur, par exemple utiliser le même réseau Wi-Fi) :

Pour trouver l'adresse IP de votre serveur, tapez la ligne de commande suivante dans votre terminal (sous mac) :

```terminal
ifconfig | grep "inet " | grep -v 127.0.0.1 | cut -d\  -f2
```

Elle devrait vous renvoyer votre adresse IP. Si vous n'êtes pas sous mac et/ou que cette commande ne fonctionne pas,
il existe d'autres méthodes pour obtenir votre adresse IP.

Vous devriez avoir noté votre numéro de port lorsque vous avez lancé le serveur.

Entrez ces deux informations au bas de la page de connexion, puis appuyez sur OK.
Si tout s'est bien passé, vous devriez recevoir un message "The back says hello to you".
Vous pouvez maintenant vous connecter avec les identifiants que vous voulez
(l'utilisateur sera créé à la volée s'il n'existe pas, cela évite d'avoir un flow de création de compte fastidieux).

Remarque : Si vous vous connectez sur le serveur backend que vous venez de mettre en place, l'interface semblera assez vide
puisque vous serez le seul utilisateur connecté. Vous pouver vous déconnecter puis vous connecter avec un autre compte
pour pouvoir voir l'interface de chat, créer un nouveau chat,... Si vous avez accès à 2 téléphone vous pourrez vraiment
tester le temps réel. Sinon, si vous contactez les développeurs de l'application en leur indiquant un créneau horaire durant lequel
vous évaluerez notre application, nous pourrons essayer de nous connecter (sur le serveur bebert.cs-campus.fr) et communiquer avec vous :D

## Structure du code

Diagramme UML géré par IntelliJ IDEA, édité pour distinguer les blocs fonctionnels.

### Côté backend

![UML Backend](https://people.via.ecp.fr/~bebert/BackServer_dependency_graph_2.png)

### Côté frontend (application mobile)

![UML Frontend](https://people.via.ecp.fr/~bebert/PtiChatApp_dependency_graph_2.png)

## Principes de Programmation Orientée Objet appliqués / principes étudiés utilisés

### Côté backend

##### Interface

La classe IStorage est une interface décrivant les méthodes utilisables pour accéder et enregistrer des données.
Elle est implémenté par deux classes :
- MemoryStorage stocke les structures de données telle quelles, en mémoire, utilisant des HashMaps.
Ces données sont volatiles, perdues en cas de redémarrage du serveur, et non transférables.
- SqliteStorage stockes les données dans une base de donnée SQLite.
Cette technologie a l'avantage de ne pas nécessiter d'installer un serveur de base de données sur le serveur.

##### Héritage (inheritance)

Notre seul besoin d'héritage est pour la gestions des tests unitaires pour le stockage
(`src/test/java/com/pooa/ptichat/BackServer/Storage`). La classe principale, abstraite, `StorageTests` contient l'essentiel
des **tests unitaires** sur le stockage, testant les fonctions de l'interface `IStorage`. Elle est étendue par deux classes :
- MemoryStorageTests pour tester l'implémentation en mémoire
- SqliteStorageTests pour tester l'implémentation en SQLite

##### Singleton pattern

Une classe singleton est utilisée pour n'avoir qu'une seule instance d'objet de stockage et une seule instance de
`ConnectionManager` pour toutes les threads que l'exécution du code peut produire.

##### Sockets

Le serveur repose sur de la connexion par sockets (et pas HTTP). Son API consiste en la réception et l'envoi de chaînes
de caractères au format JSON (cf. la partie *Spécification API*, qui détaille chacun des messages implémentés de chaque côté).

##### Threading

Chaque traitement de message reçu par le serveur est lancé dans une Thread séparée, afin de n'occuper que très peu
la Thread principale. Ainsi, le serveur ne devrait pas souffrire de latence si une requête génère des calculs ou des accès
aux données lourds, la Thread principale continuera d'écouter les messages entrants.

### Côté frontend (application mobile)

##### Héritage (inheritance)

Beaucoup de classes implémentées sont des classes filles de classes Android. Par exemple `MainActivity` hérite de `AppCompatActivity`.

##### Singleton pattern

Une classe singleton, `SocketSingleton` est utilisée pour n'avoir qu'une seule connexion socket vers le backend.

##### Observer pattern

Lorsque la Thread de réception de messages du serveur reçoit un nouveau message, elle envoie elle-même un *broadcast*
au reste de l'application. Les autres classes (celles ayant besoin d'effectuer certaines actions à la suite de certaines commandes
reçues du serveur) écoutent ces *broadcast* pour effectuer les actions voulues dans leur propre contexte.

##### Proxy pattern

Pour envoyer un message vers le backend, les différentes classes utilisent la fonction statique
`SendMessageTask.sendMessageAsync(context, message);`, sans se soucier de l'implémentation réelle de la connexion socket.

##### Sockets

La connexion au serveur se fait par sockets.

##### Threading

La gestion de la connexion socket utilise des Threads. Une Thread écoute en permanence le serveur (si la connexion est établie).
À chaque envoi de message, une nouvelle Thread est lancée, dont le but est d'envoyer le message vers le serveur.
Cette séparation est cruciale, un retard dans le flux réseau ne doit en aucon cas générer des ralentissements de
l'interface graphique de l'application.

Remarque: étant donné que les requêtes au backend se font par sockets et non par API REST, il n'y a pas de réel lien entre
une "requête" et une "réponse". Le client demande au serveur des informations, puis le serveur ordonne au client d'afficher
certaines informations...

## Liste des fonctionnalités

##### Page de login

- Création de compte utilisateur
- Connexion avec nom d'utilisateur et mot de passe (refusée si utilisateur existant mais mauvais mot de passe)
- Changement de serveur sur lequel se connecter
(philosophie : chaque serveur héberge un groupe de personnes pouvant discuter librement entre elles, pas besoin d'ajout en contact)

##### Page principale

- Déconnexion (bouton Logout)
- Édition du pseudo (l'identifiant de connexion reste bien sûr le même) et du statut, visibles par les autres utilisateurs
(toucher les chmaps de texte correspondant, en haut)
- Liste des utilisateurs inscrits sur le serveur. Les utilisateurs connectés sont en temps réel affichés en vert et placés
en tête de liste. Cliquer sur un utilisateur lance le chat privé avec celui-ci
- Liste des chats dans lesquels l'utilisateur courant participe. Cliquer sur le chat l'ouvre
- Création d'un nouveau chat, avec choix du nom et des utilisateurs y participant (bouton rond avec l'icone de chat)
- Suppression de l'utilisateur courant (bouton de menu en haut à droite)

##### Page de chat

- Renommage du chat (pour les chats de groupe uniquement)
- Suppression du chat
- Liste des personnes présentes dans ce chat
- Liste des messages du chat, les messages envoyés par l'utilisateur courant sont en bleu
- Envoi d'un nouveau message
- Envoi d'un wizz, fonctionnalité **essentielle** de feu *MSN Messenger* que nous avons voulu faire revivre !

##### Autres

- Connexion automatique avec les dernières informations de connexion utilisées lors du redémarrage de l'application
- Notifications à la réception d'un message

## Pourquoi nous devrions avoir une bonne note :)

- Les objectifs initiaux sont largement remplis
- Originalité: Nous sommes le seul groupe ayant développé une application android, et le seul groupe à notre connaissance
à avoir choisi ce projet
- Le fait d'implémenter une application android nous a offert beaucoup de challenge, nous n'avons pas choisi la solution de
facilité. De même, nous utilisont très peu de libraires externes à notre projet, il est presque *from scratch*
(librairies externes: `json` et `sqlite-jdbc` pour le backend, aucune pour l'application mobile !)
- De même, nous avons choisi Java et non Python pour faire de la POO proprement et monter en compétences dans ce langage,
alors que chacun d'entre nous est bien plus à l'aise en Python qu'en Java.
- Ce README est quand même plutôt joli et complet :D

## Spécification API

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
* [Freepik](https://www.flaticon.com/authors/freepik) de [Flaticon](https://www.flaticon.com) pour l'icone des groupes
* [hakule](https://www.istockphoto.com/fr/portfolio/hakule?mediatype=illustration&sort=best) de [istockphoto](https://www.istockphoto.com) pour l'icone de l'app
