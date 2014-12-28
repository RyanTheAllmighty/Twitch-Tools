package me.ryandowling;

import java.util.List;

public class SteamOwnedGamesResponse {
    private SteamOwnedGamesResponseData response;

    public SteamOwnedGamesResponseData getResponse() {
        return this.response;
    }

    public List<SteamOwnedGame> getGames() {
        return this.response.getGames();
    }

    private class SteamOwnedGamesResponseData {
        private int game_count;
        private List<SteamOwnedGame> games;

        public int getGameCount() {
            return this.game_count;
        }

        public List<SteamOwnedGame> getGames() {
            return this.games;
        }
    }
}
