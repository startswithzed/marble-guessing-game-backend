package com.example.marbleguessinggamebackend.service;

import java.util.UUID;

import com.example.marbleguessinggamebackend.exception.GameException;
import com.example.marbleguessinggamebackend.model.Game;
import com.example.marbleguessinggamebackend.model.GameStatus;
import com.example.marbleguessinggamebackend.model.Move;
import com.example.marbleguessinggamebackend.model.Turn;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class GameService {

  private final GameRegistry games;
  private final SimpMessagingTemplate mt;

  // Check if guess is correct
  private Boolean isCorrectGuess(Game game, String guess) {
    if (game.getHidden() % 2 == 0) {
      // Even is correct
      if (guess.equals("EVEN"))
        return true;
      else
        return false;
    } else {
      // Odd is correct
      if (guess.equals("ODD"))
        return true;
      else
        return false;
    }
  }

  // Check if someone won
  private Turn didWin(Game game) {
    if (game.getStake1() <= 0)
      return Turn.PLAYER_2;
    if (game.getStake2() <= 0)
      return Turn.PLAYER_1;
    else
      return null;
  }

  // Create a new game
  public String startGame(String player1) {
    // Generate a random game id
    // Create a new game with player name and game id
    var game = new Game(UUID.randomUUID().toString(), player1);
    log.info("{} started a new game: {}", player1, game.getGameId());
    games.addGame(game);

    return player1 + " started a new game: " + game.getGameId();
  }

  // Join an existing game
  public Game joinGame(String gameId, String player2) {
    // If the game with provided id does not exist the throw exception with message
    if (!games.getGames().containsKey(gameId)) {
      log.error("Game: {} does not exist", gameId);
      throw new GameException("Game: " + gameId + " does not exist");
    }
    // Get game from games registry
    var game = games.getGames().get(gameId);
    // Update game state
    if (game.getStatus().equals(GameStatus.NEW)) {
      game.setPlayer2(player2);
      game.setStatus(GameStatus.IN_PROGRESS);
      game.setStake1(10);
      game.setStake2(10);
      // Update game registry
      games.addGame(game);
      log.info("Player: {} joined game: {}", player2, gameId);

      // Notify listeners
      mt.convertAndSend("/topic/gamestate/" + gameId, game);
      return game;
    } else {
      log.error("Game: {} is already in progress", gameId);
      throw new GameException("Game: " + gameId + " is already in progress");
    }
  }

  // Hide marbles
  public String hide(int hide, String gameId, String player) {
    if (!games.getGames().containsKey(gameId)) {
      log.error("Game: {} does not exist", gameId);
      throw new GameException("Game: " + gameId + " does not exist");
    }
    var game = games.getGames().get(gameId);

    // If game is not in progress throw error with message
    if (!game.getStatus().equals(GameStatus.IN_PROGRESS)) {

      log.error("Game: {} is not in progress", game.getGameId());
      throw new GameException("Game: " + gameId + " is not in progress");
    }

    // If current move is not hide throw error
    var move = game.getMove();
    if (!move.equals(Move.HIDE)) {
      log.error("Invalid move. Current move is: {}", move);
      throw new GameException("Invalid move. Current move is: " + move);
    }

    // Check turn and hide
    var turn = game.getTurn();
    // Player name for current turn
    var expectedPlayer = turn == Turn.PLAYER_1 ? game.getPlayer1() : game.getPlayer2();
    // Check if correct player is playing the move else throw error
    if ((turn.equals(Turn.PLAYER_1) && !game.getPlayer1().equals(player))
        || (turn.equals(Turn.PLAYER_2) && !game.getPlayer2().equals(player))) {
      // Invalid turn
      log.error("Invalid turn. Waiting for player: {} to play", expectedPlayer);
      throw new GameException("Invalid turn. Waiting for " + expectedPlayer + " to play");
    } else {
      // If correct turn then hide
      // Get the stake for current player
      var stake = turn.equals(Turn.PLAYER_1) ? game.getStake1() : game.getStake2();
      if (hide > stake || hide <= 0) {
        log.error("Can only hide marbles >0 and <= {}", stake);
        throw new GameException("Invalid count. Can only hide marbles >0 and <=" + stake);
      } else {
        // Update state
        game.setHidden(hide);
        game.setTurn(turn.equals(Turn.PLAYER_1) ? Turn.PLAYER_2 : Turn.PLAYER_1);
        game.setMove(Move.BET);
        // Update game registry
        games.addGame(game);
        log.info("Player: {} hid {} marbles", player, hide);

        // Notify listeners
        mt.convertAndSend("/topic/gamestate/" + gameId, game);
        return player + " hid: " + hide + " marbles";
      }
    }
  }

  // Bet marbles
  public String bet(int bet, String gameId, String player) {
    if (!games.getGames().containsKey(gameId)) {
      log.error("Game: {} does not exist", gameId);
      throw new GameException("Game: " + gameId + " does not exist");
    }
    var game = games.getGames().get(gameId);

    if (!game.getStatus().equals(GameStatus.IN_PROGRESS)) {

      log.error("Game: {} is not in progress", game.getGameId());
      throw new GameException("Game " + gameId + " is not in progress");
    }

    var move = game.getMove();
    if (!move.equals(Move.BET)) {
      log.error("Invalid move. Current move is: {}", move);
      throw new GameException("Invalid move. Current move is: " + move);
    }

    // Check turn and bet
    var turn = game.getTurn();
    var expectedPlayer = turn == Turn.PLAYER_1 ? game.getPlayer1() : game.getPlayer2();
    if ((turn.equals(Turn.PLAYER_1) && !game.getPlayer1().equals(player))
        || (turn.equals(Turn.PLAYER_2) && !game.getPlayer2().equals(player))) {
      // Invalid turn
      log.error("Invalid turn. Waiting for player: {} to play", expectedPlayer);
      throw new GameException("Invalid turn. Waiting for " + expectedPlayer + " to play");
    } else {
      // Get stake for current player and bet
      var stake = turn.equals(Turn.PLAYER_1) ? game.getStake1() : game.getStake2();
      if (bet > stake || bet <= 0) {
        log.error("Can only bet marbles >0 and <= {}", stake);
        throw new GameException("Invalid count. Can only bet marbles >0 and <=" + stake);
      } else {
        // Update game state
        game.setBet(bet);
        game.setMove(Move.GUESS);
        // Update game registry
        games.addGame(game);
        log.info("Player: {} bet {} marbles", player, bet);

        // Notify listeners
        mt.convertAndSend("/topic/gamestate/" + gameId, game);
        return player + " bet: " + bet + " marbles";
      }
    }
  }

  // Guess marbles
  public String guess(String gameId, String player, String guess) {
    if (!games.getGames().containsKey(gameId)) {
      log.error("Game: {} does not exist", gameId);
      throw new GameException("Game: " + gameId + " does not exist");
    }
    var game = games.getGames().get(gameId);

    if (!game.getStatus().equals(GameStatus.IN_PROGRESS)) {

      log.error("Game: {} is not in progress", game.getGameId());
      throw new GameException("Game: " + gameId + " is not in progress");
    }

    var move = game.getMove();
    if (!move.equals(Move.GUESS)) {
      log.error("Invalid move. Current move is: {}", move);
      throw new GameException("Invalid move. Current move is: " + move);
    }

    // Check turn and guess
    var turn = game.getTurn();
    var expectedPlayer = turn == Turn.PLAYER_1 ? game.getPlayer1() : game.getPlayer2();
    if ((turn.equals(Turn.PLAYER_1) && !game.getPlayer1().equals(player))
        || (turn.equals(Turn.PLAYER_2) && !game.getPlayer2().equals(player))) {
      // Invalid turn
      log.error("Invalid turn. Waiting for player: {} to play", expectedPlayer);
      throw new GameException("Invalid turn. Waiting for " + expectedPlayer + " to play");
    } else {
      // Check guess
      var isCorrect = isCorrectGuess(game, guess);
      if (isCorrect) {
        // Correct guess
        if (turn.equals(Turn.PLAYER_1)) {
          // Player 1 guessed correctly
          game.setStake1(game.getStake1() + game.getBet());
          game.setStake2(game.getStake2() - game.getBet());
          log.info("Player: {} guessed correctly", player);
        } else {
          // Player 2 guessed correctly
          game.setStake2(game.getStake2() + game.getBet());
          game.setStake1(game.getStake1() - game.getBet());
          log.info("Player: {} guessed correctly", player);
        }
      } else {
        // Wrong guess
        if (turn.equals(Turn.PLAYER_1)) {
          // Player 1 guessed incorrectly
          game.setStake1(game.getStake1() - game.getHidden());
          game.setStake2(game.getStake2() + game.getHidden());
          log.info("Player: {} guessed incorrectly", player);
        } else {
          // Player 2 guessed incorrectly
          game.setStake2(game.getStake2() - game.getHidden());
          game.setStake1(game.getStake1() + game.getHidden());
          log.info("Player: {} guessed incorrectly", player);
        }
      }

      // Check if somebody won
      var winner = didWin(game);
      if (winner != null) {
        // Someone won
        // End game
        // Set winner
        game.setStatus(GameStatus.ENDED);
        if (winner.equals(Turn.PLAYER_1)) {
          // Player 1 won the game
          game.setWinner(game.getPlayer1());
          game.setStake1(20);
          game.setStake2(0);
          // Update game registry
          games.addGame(game);
          log.info("Player: {} won the game", game.getPlayer1());

          // Notify listeners
          mt.convertAndSend("/topic/gamestate/" + gameId, game);
          return winner + " won the game";
        } else {
          // Player 2 won the game
          game.setWinner(game.getPlayer2());
          game.setStake2(20);
          game.setStake1(0);
          // Update game registry
          games.addGame(game);
          log.info("Player: {} won the game", game.getPlayer2());

          // Notify listeners
          mt.convertAndSend("/topic/gamestate/" + gameId, game);
          return winner + " won the game";
        }
      } else {
        // No one won so proceed to next round
        game.setHidden(0);
        game.setBet(0);
        game.setMove(Move.HIDE);
        // Update game registry
        games.addGame(game);

        // Notify listeners
        mt.convertAndSend("/topic/gamestate/" + gameId, game);
        var result = isCorrect ? "correctly" : "incorrectly";
        return player + " guessed " + result;
      }
    }
  }

  // Restart game
  public String restartGame(String gameId, String player) {
    if (!games.getGames().containsKey(gameId)) {
      log.error("Game: {} does not exist", gameId);
      throw new GameException("Game: " + gameId + " does not exist");
    }
    var game = games.getGames().get(gameId);
    // Check if restart request is from player playing the game
    if (!player.equals(game.getPlayer1()) && !player.equals(game.getPlayer2())) {
      log.error("Can't restart game: {} as player: {} is not playing", gameId, player);

      throw new GameException("Can't restart as " + player + " is not playing the game: " + gameId);
    }
    // Update game state
    game.setStatus(GameStatus.IN_PROGRESS);
    game.setStake1(10);
    game.setStake2(10);
    game.setTurn(Turn.PLAYER_1);
    game.setMove(Move.HIDE);
    game.setHidden(0);
    game.setBet(0);
    game.setWinner(null);
    // Update game registry
    games.addGame(game);
    log.info("Restarting game: {}", gameId);

    // Notify listeners
    mt.convertAndSend("/topic/gamestate/" + gameId, game);
    return player + " restarted game: " + gameId;
  }

  // Quit game
  public String quitGame(String gameId, String player) {
    if (!games.getGames().containsKey(gameId)) {
      log.error("Game: {} does not exist", gameId);
      throw new GameException("Game: " + gameId + " does not exist");
    }
    var game = games.getGames().get(gameId);
    // Check if player is playing the game
    if (player.equals(game.getPlayer1()) || player.equals(game.getPlayer2())) {
      log.info("Player: {} quit the game", player);
      game.setStatus(GameStatus.ENDED);
      // Set winner null if someone has already won
      // This is to differentiate between quit game state and game state when somebody
      // won
      game.setWinner(null);
      // Update game registry
      games.removeGame(game);

      // Notify listeners
      mt.convertAndSend("/topic/gamestate/" + gameId, game);
      return player + " quit game: " + gameId;
    } else {
      // Return error is player is not part of the game
      log.error("Player: {} is not playing game: {}", player, gameId);
      throw new GameException(player + " is not playing the game: " + gameId);
    }
  }
}
