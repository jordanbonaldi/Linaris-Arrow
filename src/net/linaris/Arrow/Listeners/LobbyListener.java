
package net.linaris.Arrow.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import net.linaris.Arrow.ArrowPlugin;
import net.linaris.Arrow.Managers.PlayersManager;
import net.linaris.Arrow.Managers.ScoreBoard;
import net.linaris.Arrow.Timers.GameTimer;
import net.linaris.Arrow.Utils.Data;
import net.linaris.Arrow.Utils.Utils;

public class LobbyListener
implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayersManager.getInstance().getPlayer(player);
        Utils.resetPlayer(player);
        if (ArrowPlugin.getGameState() == ArrowPlugin.GameState.LOBBY) {
            for(Player p : Bukkit.getOnlinePlayers()){
                    p.setScoreboard(ScoreBoard.getInstance().getScoreboard());

            }
            PlayersManager.getInstance().getPlayer(player);
            player.teleport(Data.getInstance().getLobby());
            Utils.m_playersJustConnected.add(player);
            if (Bukkit.getOnlinePlayers().size() >= ArrowPlugin.getPlayerMax()) {
                if (GameTimer.getInstance().getTime() > 10) {
                    GameTimer.getInstance().setTime(10);
                }
            } else if (Bukkit.getOnlinePlayers().size() >= ArrowPlugin.getInt("player-min")) {
                if (GameTimer.getInstance().getTime() > (long)ArrowPlugin.getInt("lobby-time")) {
                    GameTimer.getInstance().setTime(ArrowPlugin.getInt("lobby-time"));
                }
            } else {
                GameTimer.getInstance().setTime(9999);
            }
            Utils.setInventory(player);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player;
        if (ArrowPlugin.getGameState() == ArrowPlugin.GameState.LOBBY && (player = event.getPlayer()).getLocation().getY() <= Data.getInstance().getLobby().getY() - 8.0) {
            player.teleport(Data.getInstance().getLobby());
        }
    }
}

