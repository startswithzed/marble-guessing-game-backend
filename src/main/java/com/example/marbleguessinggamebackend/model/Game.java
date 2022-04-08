package com.example.marbleguessinggamebackend.model;

import lombok.Data;

@Data
public class Game {
  
  private String gameId;
  private GameStatus status;
  private String player1;
  private String player2;
  private int stake1;
  private int stake2;
  private Turn turn;
  private Move move;
  private int hidden;
  private int bet;
  private String winner;

  public Game(String gameId, String player1) {
    this.gameId = gameId;
    this.player1 = player1;
    status = GameStatus.NEW;
    move = Move.HIDE;
    turn = Turn.PLAYER_1;
  }
}
