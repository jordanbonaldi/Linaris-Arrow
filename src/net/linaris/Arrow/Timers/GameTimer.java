
package net.linaris.Arrow.Timers;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.linaris.Arrow.ArrowPlugin;
import net.linaris.Arrow.Managers.PlayersManager;
import net.linaris.Arrow.Managers.ScoreBoard;
import net.linaris.Arrow.Utils.Data;
import net.linaris.Arrow.Utils.Utils;

public class GameTimer {
    private static GameTimer instance;
    private List<GameTask> m_tasks = new LinkedList<GameTask>();
    private int taskId = -1;
    private long m_time = 9999;

    public static GameTimer getInstance() {
        if (instance == null) {
            instance = new GameTimer();
        }
        return instance;
    }

    private GameTimer() {
    }

    public void start() {
        if (this.taskId != -1) {
            return;
        }
        this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask((Plugin)ArrowPlugin.getInstance(), new Runnable(){

            @Override
			public void run() {
				// TASKS
				for(int i = 0; i < m_tasks.size(); i++) {
					if(m_tasks.get(i).m_time-- <= 0) {
						m_tasks.get(i).m_task.run();
						m_tasks.remove(i);
						i--;
					}
				}
				// -----
                int secondes = (int)(GameTimer.this.m_time % 60);
                if (ArrowPlugin.getGameState() == ArrowPlugin.GameState.LOBBY) {
                    Location lobby = Data.getInstance().getLobby();
                    for (Player player : Utils.m_playersJustConnected) {
                        if (player.getLocation().getWorld().getUID().equals(lobby.getWorld().getUID()) && player.getLocation().distance(lobby) <= 15.0) continue;
                        player.teleport(lobby);
                    }
                    Utils.m_playersJustConnected.clear();
                    if (GameTimer.this.m_time > (long)ArrowPlugin.getInt("lobby-time")) {
                    } else {
                        String motd = Bukkit.getOnlinePlayers().size() >= ArrowPlugin.getPlayerMax() ? "§cFull" : "§a<sec>§fs";
                    }
                    if (GameTimer.this.m_time <= 0) {
                        if (PlayersManager.getInstance().getPlayers().size() < 2) {
                            GameTimer.access$2(GameTimer.this, 9999);
                        } else {
                            ArrowPlugin.getInstance().setGameState(ArrowPlugin.GameState.GAME);
                            net.neferett.LinarisAPI.Game.GameAPI.setGameState(net.neferett.LinarisAPI.Game.GameState.PLAYING);
                            GameTimer.access$2(GameTimer.this, ArrowPlugin.getInt("game-time"));
                        }
                    }
                } else if (ArrowPlugin.getGameState() == ArrowPlugin.GameState.GAME) {
                    for (PlayersManager.TucPlayer tplayer : PlayersManager.getInstance().getPlayers()) {
                        tplayer.updateTime();
                    }
                    if (GameTimer.this.m_time <= 0) {
                        for(Player p : Bukkit.getOnlinePlayers()){
                        		p.sendMessage("&b&l La partie est fini !");

                        }
                        GameTimer.access$2(GameTimer.this, 5);
                        ArrowPlugin.getInstance().setGameState(ArrowPlugin.GameState.END);
                        net.neferett.LinarisAPI.Game.GameAPI.setGameState(net.neferett.LinarisAPI.Game.GameState.RESTARTING);

                    }
                } else if (ArrowPlugin.getGameState() == ArrowPlugin.GameState.END) {
                    if (GameTimer.this.m_time <= 0) {
                        GameTimer.access$2(GameTimer.this, 5);
                        ArrowPlugin.getInstance().setGameState(ArrowPlugin.GameState.RESTART);
                        return;
                    }
                } else if (ArrowPlugin.getGameState() == ArrowPlugin.GameState.RESTART && GameTimer.this.m_time <= 0) {
                    Bukkit.dispatchCommand((CommandSender)Bukkit.getConsoleSender(), (String)"stop");
                }
                        ScoreBoard.getInstance().updateScoreboard(GameTimer.this.m_time);

                
                GameTimer gameTimer = GameTimer.this;
                GameTimer.access$2(gameTimer, gameTimer.m_time - 1);
                if (ArrowPlugin.getBoolean("always-day")) {
                    Data.getInstance().getLobby().getWorld().setTime(5000);
                }
            }
        }, 20, 20);
    }

    public long getTime() {
        return this.m_time;
    }

    public void setTime(long time) {
        this.m_time = time;
    }

    static  void access$2(GameTimer gameTimer, long l) {
        gameTimer.m_time = l;
    }

    public class GameTask {
        private Runnable m_task;
        private long m_time;

        public GameTask(Runnable task, long time) {
            this.m_task = task;
            this.m_time = time;
        }

    }

}

