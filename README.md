# Marble Guessing Game Backend
Spring boot backend for marble guessing game inspired by Squid Game TV series.

## Rules
- It is a two-player game with each player having ten marbles in their stake in the beginning.
- Each round, a player acts as the hider while the other acts as the guesser.
-  Hider hides a non-zero number of marbles from their stake.
-  Guesser secretly bets a non-zero number of marbles from their stake and guesses whether the hider's marbles are odd or even.
-  If the guesser guessed correctly, the hider must give the guesser marbles equal to the number of marbles the guesser bet.
-  If the guesser guessed incorrectly, they must give the hider marbles equal to the number of marbles they hid.
-  Roles switch every round.
- Objective is to win all the marbles of the opponent.

## Running the server
**NOTE: Java must be installed on the system**
1. Clone the repository.
2. Open a terminal in the root directory of the project (contains the `.gradle`  folder).
3. Run `./gradlew bootRun`  command.
##### or For VS Code
 1. Clone the repository and open the project.
 2. Install the spring boot extension pack from the extensions marketplace.
 3. Open any source code file and press `Ctrl + F5` to run the project.

## Docs
1. Run the server.
2. Launch `http://localhost:8080/swagger-ui/index.html#/` in a browser of your choice.

## Websocket endpoints
- Clients must connect to `http://localhost:8080/game` via a Stomp client using SockJS.
- Once connected, the clients must subscribe to `/topic/gamestate/{gameId}` to listen to notifications sent by the server.

## Postman collection
1. Fork the collection from [this link](https://www.postman.com/telecoms-operator-5792800/workspace/marble-guessing-game/collection/17279060-e3b66001-f620-49b9-bc30-434c35fd324f?action=share&creator=17279060).
2. Get `gameId` from the response of create game request and update the current value of the `gameId` variable of the collection to use it in other requests.