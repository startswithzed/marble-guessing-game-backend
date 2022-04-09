package com.example.marbleguessinggamebackend.controller;

import javax.servlet.http.HttpServletRequest;

import com.example.marbleguessinggamebackend.dto.Guess;
import com.example.marbleguessinggamebackend.dto.MarbleCount;
import com.example.marbleguessinggamebackend.model.Game;
import com.example.marbleguessinggamebackend.service.GameRegistry;
import com.example.marbleguessinggamebackend.service.GameService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class GameAPI {

  private final GameService gs;
  private final GameRegistry gr;

  // Get all games endpoint
  @Operation(summary = "Get all games")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Fetched all games", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = Game.class)) })
  })
  @GetMapping()
  public ResponseEntity<Object[]> getAllGames() {
    // Get all games from the game registry
    var games = gr.getGames();

    return ResponseEntity.ok(games.values().toArray());
  }

  // Create game endpoint
  @Operation(summary = "Start a new game")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Started a new game", content = {
          @Content(mediaType = "text/plain") }),
      @ApiResponse(responseCode = "400", description = "Player header is missing", content = {
          @Content(mediaType = "text/plain") })
  })
  @Parameter(in = ParameterIn.HEADER, required = true, name = "player", description = "Player name header")
  @PostMapping("/create")
  public ResponseEntity<String> createGame(HttpServletRequest request) {
    // Get player name from request header
    var player = request.getHeader("player");
    // Return a bad request response if header is missing
    if (player == null || player.isBlank())
      return ResponseEntity.badRequest().body("player header is missing");
    // Call service to create a new game
    var response = gs.startGame(player);

    return ResponseEntity.ok(response);
  }

  // Join game endpoint
  @Operation(summary = "Join an existing game")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Joined game", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = Game.class)) })
  })
  @Parameter(in = ParameterIn.HEADER, required = true, name = "player", description = "Player name header")
  @PostMapping("/join/{gameId}")
  public ResponseEntity<Object> joinGame(HttpServletRequest request, @PathVariable String gameId) {
    var player = request.getHeader("player");
    if (player == null || player.isBlank())
      return ResponseEntity.badRequest().body("player header is missing.");
    // Call service to join game by passing game id from path variable and player
    // name from header
    var response = gs.joinGame(gameId, player);

    return ResponseEntity.ok(response);
  }

  // Hide marbles endpoint
  @Operation(summary = "Hide marbles")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Hid marbles", content = {
          @Content(mediaType = "text/plain") })
  })
  @Parameter(in = ParameterIn.HEADER, required = true, name = "player", description = "Player name header")
  @PostMapping("/{gameId}/hide")
  public ResponseEntity<String> hide(HttpServletRequest request, @PathVariable String gameId,
      @RequestBody MarbleCount mc) {
    var player = request.getHeader("player");
    if (player == null || player.isBlank())
      return ResponseEntity.badRequest().body("player header is missing.");
    // Get marble count from request body
    int hide = mc.getCount();
    var response = gs.hide(hide, gameId, player);

    return ResponseEntity.ok(response);
  }

  // Bet marbles endpoint
  @Operation(summary = "Bet marbles")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Bet marbles", content = {
          @Content(mediaType = "text/plain") })
  })
  @Parameter(in = ParameterIn.HEADER, required = true, name = "player", description = "Player name header")
  @PostMapping("/{gameId}/bet")
  public ResponseEntity<String> bet(HttpServletRequest request, @PathVariable String gameId,
      @RequestBody MarbleCount mc) {
    var player = request.getHeader("player");
    if (player == null || player.isBlank())
      return ResponseEntity.badRequest().body("player header is missing.");
    // Get marble count from request body
    int bet = mc.getCount();
    var response = gs.bet(bet, gameId, player);

    return ResponseEntity.ok(response);
  }

  // Guess marbles endpoint
  @Operation(summary = "Guess marbles")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Guessed marbles", content = {
          @Content(mediaType = "text/plain") })
  })
  @Parameter(in = ParameterIn.HEADER, required = true, name = "player", description = "Player name header")
  @PostMapping("/{gameId}/guess")
  public ResponseEntity<String> guess(HttpServletRequest request, @PathVariable String gameId, @RequestBody Guess g) {
    var player = request.getHeader("player");
    if (player == null || player.isBlank())
      return ResponseEntity.badRequest().body("player header is missing.");
    // Get guess from request body
    var guess = g.getGuess();
    var response = gs.guess(gameId, player, guess);

    return ResponseEntity.ok(response);
  }

   // Restart game endpoint
  @Operation(summary = "Restart game")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Restarted game", content = {
          @Content(mediaType = "text/plain") })
  })
  @Parameter(in = ParameterIn.HEADER, required = true, name = "player", description = "Player name header")
  @PostMapping("/{gameId}/restart")
  public ResponseEntity<String> restart(HttpServletRequest request, @PathVariable String gameId) {
    var player = request.getHeader("player");
    if (player == null || player.isBlank())
      return ResponseEntity.badRequest().body("player header is missing.");
    var response = gs.restartGame(gameId, player);

    return ResponseEntity.ok(response);
  }

  // Quit game endpoint
  @Operation(summary = "Quit game")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Quit game", content = {
          @Content(mediaType = "text/plain") })
  })
  @Parameter(in = ParameterIn.HEADER, required = true, name = "player", description = "Player name header")
  @PostMapping("/{gameId}/quit")
  public ResponseEntity<String> quit(HttpServletRequest request, @PathVariable String gameId) {
    var player = request.getHeader("player");
    if (player == null || player.isBlank())
      return ResponseEntity.badRequest().body("player header is missing.");
    var response = gs.quitGame(gameId, player);

    return ResponseEntity.ok(response);
  }
}
