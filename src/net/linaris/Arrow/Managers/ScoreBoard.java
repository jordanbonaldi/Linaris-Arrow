
package net.linaris.Arrow.Managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import net.linaris.Arrow.ArrowPlugin;

public class ScoreBoard {
    private Objective m_obj;
    private ArrowPlugin.GameState m_boardType;
    private Map<String, String> m_uniqueText = new HashMap<String, String>();
    private int m_wait = 0;
    private static ScoreBoard instance;

    public static ScoreBoard getInstance() {
        if (instance == null) {
            instance = new ScoreBoard();
        }
        return instance;
    }

    private ScoreBoard() {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        this.m_obj = board.registerNewObjective("arrow", "dummy");
        this.m_obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.m_boardType = ArrowPlugin.GameState.CONFIG;
    }

    public Scoreboard getScoreboard() {
        return this.m_obj.getScoreboard();
    }

    public void updateScoreboard(long time) {
        ArrowPlugin.GameState gameState = ArrowPlugin.getGameState();
        if (this.m_boardType != gameState) {
            this.m_boardType = gameState;
            this.clear();
            if (gameState == ArrowPlugin.GameState.LOBBY) {
                this.m_obj.setDisplayName("§6☬ §b§lArrow §6☬");
            }
        }
        if (gameState == ArrowPlugin.GameState.LOBBY) {
            int nbPlayers = Bukkit.getOnlinePlayers().size();
            this.setText("lobby_player", ("Joueurs §a<x>/<max>").replaceAll("<x>", Integer.toString(nbPlayers)).replaceAll("<max>", Integer.toString(ArrowPlugin.getPlayerMax())), 2);
            if (nbPlayers >= ArrowPlugin.getInt("player-min")) {
                this.setText("lobby_time", ("Début dans §6<sec>s").replaceAll("<sec>", Long.toString(time)).replaceAll("<SECOND>", time > 1 ? "seconde" : "secondes"), 1);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("§aAttente");
                int i = 0;
                while (i < this.m_wait) {
                    sb.append(".");
                    ++i;
                }
                this.m_wait = this.m_wait >= 3 ? 0 : this.m_wait + 1;
                this.setText("lobby_time", sb.toString(), 1);
            }
        } else if (gameState == ArrowPlugin.GameState.GAME) {
            int min = (int)(time / 60);
            int sec = (int)(time % 60);
            this.m_obj.setDisplayName(("§6☬ §e<min>:<sec> §b§lArrow §6☬").replaceAll("<min>", ScoreBoard.getVarWithZero(min)).replaceAll("<sec>", ScoreBoard.getVarWithZero(sec)).replaceAll("<MINUTE>", min > 1 ? "minutes" : "minute").replaceAll("<SECONDE>", sec > 1 ? "seconde" : "secondes"));
        }
    }

    private void setText(String id, String text, int pos) {
        if (this.m_uniqueText.containsKey(id)) {
            String last_text = this.m_uniqueText.get(id);
            if (last_text.equalsIgnoreCase(text)) {
                int last_score = this.m_obj.getScore(Bukkit.getOfflinePlayer((String)last_text)).getScore();
                if (pos != last_score) {
                    this.m_obj.getScore(Bukkit.getOfflinePlayer((String)last_text)).setScore(pos);
                }
                return;
            }
            this.m_obj.getScoreboard().resetScores(Bukkit.getOfflinePlayer((String)last_text));
        }
        if (text.length() > 16) {
            text = text.substring(0, 16);
        }
        this.m_uniqueText.put(id, text);
        this.m_obj.getScore(Bukkit.getOfflinePlayer((String)text)).setScore(pos);
    }

    public static String getVarWithZero(int var) {
        return var > 9 ? Integer.toString(var) : "0" + var;
    }

    public void updateScore(PlayersManager.TucPlayer tplayer) {
        this.m_obj.getScore((OfflinePlayer)tplayer.getPlayer()).setScore(tplayer.getKills());
    }

    private void clear() {
        for (String entry : this.m_uniqueText.values()) {
            this.m_obj.getScoreboard().resetScores(Bukkit.getOfflinePlayer((String)entry));
        }
        this.m_uniqueText.clear();
    }
}

