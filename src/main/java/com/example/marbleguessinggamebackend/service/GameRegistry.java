package com.example.marbleguessinggamebackend.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.marbleguessinggamebackend.model.Game;

import org.springframework.stereotype.Component;

@Component
public class GameRegistry {
  
  private static ConcurrentHashMap<String, Game> games;

  private GameRegistry() {
    GameRegistry.games = new ConcurrentHashMap<>();
  }

  public Map<String, Game> getGames() {
    return GameRegistry.games;
  } 

  public void addGame(Game game) {
    games.put(game.getGameId(), game);
  }

  public void removeGame(Game game) {
    games.values().remove(game);
  }
}

