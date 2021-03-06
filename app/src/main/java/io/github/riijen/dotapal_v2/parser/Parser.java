package io.github.riijen.dotapal_v2.parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.riijen.dotapal_v2.model.Match;
import io.github.riijen.dotapal_v2.model.Player;

public class Parser {

    /**
     * Returns the 5 most recently played games from a given account_id
     * @param rawJson - Raw JSON text buffered from dota 2 api
     *                using account_id - for Jandrix
     */
    public static List<Match> parseByAccountId(String rawJson) {

        List<Match> allMatches = null;
        JSONTokener convertedJson = new JSONTokener(rawJson);

        try {
            JSONObject jObject = new JSONObject(convertedJson);
            JSONObject result = jObject.getJSONObject("result");

            int status = result.getInt("status");

            if (status == 15) {
                return allMatches;
            }

            allMatches = new ArrayList<Match>();

            JSONArray matches = result.getJSONArray("matches");

            for (int i = 0 ; i < 5 ; i++) {

                List<Player> radiant = new ArrayList<Player>();
                List<Player> dire = new ArrayList<Player>();

                JSONObject jmatch = matches.getJSONObject(i);

                String matchId = jmatch.getString("match_id");
                int lobbyType = jmatch.getInt("lobby_type");
                String lobby_Type;

                if (lobbyType == 0) {
                    lobby_Type = "public matchmaking";
                } else if (lobbyType == 1) {
                    lobby_Type = "practice";
                } else if (lobbyType == 2) {
                    lobby_Type = "tournament";
                } else if (lobbyType == 3) {
                    lobby_Type = "tutorial";
                } else if (lobbyType == 4) {
                    lobby_Type = "coop with bots";
                } else if (lobbyType == 5) {
                    lobby_Type = "team match";
                } else if (lobbyType == 6) {
                    lobby_Type = "solo queue";
                } else if (lobbyType == 7) {
                    lobby_Type = "ranked";
                } else {
                    lobby_Type = "solo mid 1v1";
                }

                JSONArray players = jmatch.getJSONArray("players");

                for (int j = 0 ; j < players.length() ; j++) {
                    JSONObject player = players.getJSONObject(i);
                    String account_id = player.getString("account_id");
                    String hero_id = player.getString("hero_id");
                    int player_slot = player.getInt("player_slot");

                    if (player_slot < 128) {
                        radiant.add(new Player(account_id, hero_id));
                    } else {
                        dire.add(new Player(account_id, hero_id));
                    }
                }

                Match match = new Match(matchId, radiant, dire, lobby_Type);
                allMatches.add(match);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return allMatches;
    }

    /**
     * Parse the details of a specific match
     * @param rawJson
     */

    public static void parseMatchDetails(String rawJson, Match match) {

        JSONTokener jsonTokener = new JSONTokener(rawJson);
        List<Player> radiant = new ArrayList<Player>();
        List<Player> dire = new ArrayList<Player>();

        try {
            JSONObject jObject = new JSONObject(jsonTokener);
            JSONObject result = jObject.getJSONObject("result");

            boolean radiant_win = result.getBoolean("radiant_win");
            int duration = result.getInt("duration");

            JSONArray players = result.getJSONArray("players");

            for (int i = 0 ; i < players.length() ; i++) {

                JSONObject player = players.getJSONObject(i);

                String account_id = player.getString("account_id");
                String hero_id = player.getString("hero_id");

                String item1 = player.getString("item_0");
                String item2 = player.getString("item_1");
                String item3 = player.getString("item_2");
                String item4 = player.getString("item_3");
                String item5 = player.getString("item_4");
                String item6 = player.getString("item_5");

                List<String> items = new ArrayList<>();
                items.add(item1);
                items.add(item2);
                items.add(item3);
                items.add(item4);
                items.add(item5);
                items.add(item6);

                int player_slot = player.getInt("player_slot");
                int kills = player.getInt("kills");
                int deaths = player.getInt("deaths");
                int assists = player.getInt("assists");
                int gpm = player.getInt("gold_per_min");
                int xpm = player.getInt("xp_per_min");
                int totalGold = player.getInt("gold");
                int denies = player.getInt("denies");
                int last_hits = player.getInt("last_hits");
                int level = player.getInt("level");

                Player newPlayer = new Player(account_id, hero_id);
                newPlayer.setGameStats(items, gpm, xpm, totalGold, denies, last_hits, level, kills,
                        deaths, assists);

                if (player_slot < 128) {
                    radiant.add(newPlayer);
                } else {
                    dire.add(newPlayer);
                }

            }

            match.setRadiantPlayers(radiant);
            match.setDirePlayers(dire);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
}
