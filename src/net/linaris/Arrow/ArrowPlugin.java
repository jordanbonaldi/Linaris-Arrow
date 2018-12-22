package net.linaris.Arrow;

import java.util.ArrayList;
import java.util.List;

import net.linaris.Arrow.Commands.CommandArrow;
import net.linaris.Arrow.Listeners.BasicPlayerListener;
import net.linaris.Arrow.Listeners.GameListener;
import net.linaris.Arrow.Listeners.LobbyListener;
import net.linaris.Arrow.Listeners.WorldListener;
import net.linaris.Arrow.Managers.PlayersManager;
import net.linaris.Arrow.Managers.ScoreBoard;
import net.linaris.Arrow.Timers.GameTimer;
import net.linaris.Arrow.Utils.Data;
import net.linaris.Arrow.Utils.Utils;
import net.neferett.LinarisAPI.API;
import net.neferett.LinarisAPI.Fireworks.FireworksAPI;
import net.neferett.LinarisAPI.Wallet.Gain;
import net.neferett.LinarisAPI.Wallet.GainItem;
import net.neferett.LinarisAPI.Wallet.GainRecap;
import net.neferett.LinarisAPI.Wallet.GainRecapManager;
import net.neferett.LinarisAPI.Wallet.WalletAPI;
import net.neferett.LinarisAPI.Wallet.WalletCurrency;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class ArrowPlugin
extends JavaPlugin {
    private GameState m_gameState;
    private boolean m_winnerFind = false;
    private static ArrowPlugin instance;

    public static ArrowPlugin getInstance() {
        return instance;
    }
    
    public WalletAPI walletAPI;

    private Gain mainGain;
    private GainItem gainParticipation;
    private GainItem gainWin1;
    private GainItem gainWin2;
    private GainItem gainWin3;
    
    public void onEnable() {
        instance = this;
        
        
        net.neferett.LinarisAPI.Game.GameAPI.setGameState(net.neferett.LinarisAPI.Game.GameState.LOADING);
        net.neferett.LinarisAPI.Game.GameAPI.setGameStateMotd(true);
        net.neferett.LinarisAPI.Login.LoginAPI.setVipPlayers(2);
        mainGain = new Gain();
        gainParticipation = new GainItem("Participation", 2, WalletCurrency.Coins);
        gainWin1 = new GainItem("1er place", 5, WalletCurrency.Coins);
        gainWin2 = new GainItem("2eme place", 4, WalletCurrency.Coins);
        gainWin3 = new GainItem("3eme place", 3, WalletCurrency.Coins);


        mainGain.add(gainParticipation, gainWin1,gainWin2,gainWin3);
        
        final net.neferett.LinarisAPI.Main BetaAPI = API.getInstance();
        BetaAPI.getMotdAPI().setEnabled(true);
        BetaAPI.getMotdAPI().setMotdValue("sign_info", (Object)(ChatColor.WHITE + "" + ChatColor.BOLD + "Solo"));
        
        
        walletAPI = new WalletAPI();
        
        
        this.saveDefaultConfig();
        this.reloadConfig();
        if (ArrowPlugin.getBoolean("enable-bungeecord")) {
            this.getServer().getMessenger().registerOutgoingPluginChannel((Plugin)this, "BungeeCord");
        }
        Data.getInstance();
        ScoreBoard.getInstance();
        for (org.bukkit.Location loc : Data.getInstance().getPoints()) {
            loc.getWorld().getChunkAt(loc).load();
        }
        this.m_gameState = ArrowPlugin.getBoolean("config-mode") ? GameState.CONFIG : GameState.LOBBY;
        for (World world : Bukkit.getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity) || entity.getType() == EntityType.PLAYER) continue;
                entity.remove();
            }
        }
        PluginManager pm = this.getServer().getPluginManager();
        if (!ArrowPlugin.getBoolean("config-mode")) {
            pm.registerEvents((Listener)new BasicPlayerListener(), (Plugin)this);
            pm.registerEvents((Listener)new GameListener(), (Plugin)this);
            pm.registerEvents((Listener)new LobbyListener(), (Plugin)this);
        }
        pm.registerEvents((Listener)new WorldListener(), (Plugin)this);
        GameTimer.getInstance().start();
        ((World)Bukkit.getWorlds().get(0)).setStorm(false);
        this.getCommand("arrow").setExecutor((CommandExecutor)new CommandArrow());
        net.neferett.LinarisAPI.Game.GameAPI.setGameState(net.neferett.LinarisAPI.Game.GameState.WAITING);
    }

    public static GameState getGameState() {
        return ArrowPlugin.instance.m_gameState;
    }

    public void setGameState(GameState gameState) {
        this.m_gameState = gameState;
        if (this.m_gameState == GameState.GAME) {
        	for(Player player : Bukkit.getOnlinePlayers()){
                Utils.resetPlayer(player);
                Utils.setInventory(player);
            }
            Data.getInstance().tpToPoint();
            for(Player p : Bukkit.getOnlinePlayers()){
            		p.sendMessage("§bLa partie commence ! §eVous devez faire 25 kills pour gagner !");

            }
        } else if (this.m_gameState != GameState.END && this.m_gameState == GameState.RESTART) {
        	for(Player player : Bukkit.getOnlinePlayers()){
                Utils.tpToLobby(player);
                
            }
        }
    }

    public void showWinner() {
        net.neferett.LinarisAPI.Game.GameAPI.setGameState(net.neferett.LinarisAPI.Game.GameState.RESTARTING);
        List<GainItem> gains1 = new ArrayList<>();
        List<GainItem> gains2 = new ArrayList<>();
        List<GainItem> gains3 = new ArrayList<>();
        List<GainItem> gains4 = new ArrayList<>();

        if (this.m_winnerFind) {
            return;
        }
        Bukkit.broadcastMessage((String)"§f§m----------------");
        List<PlayersManager.TucPlayer> ranking = PlayersManager.getInstance().getRanking();
            for(Player p : Bukkit.getOnlinePlayers()){
            		p.sendMessage("");
            		p.sendMessage("");
            		p.sendMessage(("§e<rank>§ae §f: §6<player> §bavec §6<kills> §bkills !").replace("<rank>", Integer.toString(1)).replaceAll("<player>", ranking.get(0).getPlayer().getName()).replaceAll("<kills>", Integer.toString(ranking.get(0).getKills())));
            		p.sendMessage(("§e<rank>§ae §f: §6<player> §bavec §6<kills> §bkills !").replace("<rank>", Integer.toString(2)).replaceAll("<player>", ranking.get(1).getPlayer().getName()).replaceAll("<kills>", Integer.toString(ranking.get(1).getKills())));
            		p.sendMessage(("§e<rank>§ae §f: §6<player> §bavec §6<kills> §bkills !").replace("<rank>", Integer.toString(3)).replaceAll("<player>", ranking.get(2).getPlayer().getName()).replaceAll("<kills>", Integer.toString(ranking.get(2).getKills())));
            		p.sendMessage("");
            		p.sendMessage("");

            		if(ranking.get(0).getPlayer().getName().equals(p.getName())){
                               gains1.add(ArrowPlugin.getInstance().getGainWin1());
                        FireworksAPI.setFireWork(ranking.get(0).getPlayer());
                        ranking.get(0).getPlayer().setAllowFlight(true);
                        ranking.get(0).getPlayer().setFlying(true);
                        ranking.get(0).getPlayer().setGameMode(GameMode.CREATIVE);
                        ranking.get(0).getPlayer().setVelocity(new Vector(0, 0.25, 0));
                        ranking.get(0).getPlayer().playSound(ranking.get(0).getPlayer().getLocation(), Sound.LEVEL_UP, 1, 1);
                        
                        getMainGain().addTo(ranking.get(0).getPlayer(), false, true, gains1.toArray(new GainItem[gains1.size()]));
                        
            		}else if(ranking.get(1).getPlayer().getName().equals(p.getName())){
                        gains2.add(ArrowPlugin.getInstance().getGainWin2());
                        FireworksAPI.setFireWork(ranking.get(1).getPlayer());
                        ranking.get(1).getPlayer().setAllowFlight(true);
                        ranking.get(1).getPlayer().setFlying(true);
                        ranking.get(1).getPlayer().setGameMode(GameMode.CREATIVE);
                        ranking.get(1).getPlayer().setVelocity(new Vector(0, 0.25, 0));
                        ranking.get(1).getPlayer().playSound(ranking.get(1).getPlayer().getLocation(), Sound.LEVEL_UP, 1, 1);
                        getMainGain().addTo(ranking.get(1).getPlayer(), false, true, gains2.toArray(new GainItem[gains2.size()]));
            		}else if(ranking.get(2).getPlayer().getName().equals(p.getName())){
                        gains3.add(ArrowPlugin.getInstance().getGainWin3());
                        FireworksAPI.setFireWork(ranking.get(2).getPlayer());
                        ranking.get(2).getPlayer().setAllowFlight(true);
                        ranking.get(2).getPlayer().setFlying(true);
                        ranking.get(2).getPlayer().setGameMode(GameMode.CREATIVE);
                        ranking.get(2).getPlayer().setVelocity(new Vector(0, 0.25, 0));
                        ranking.get(2).getPlayer().playSound(ranking.get(2).getPlayer().getLocation(), Sound.LEVEL_UP, 1, 1);
                        getMainGain().addTo(ranking.get(2).getPlayer(), false, true, gains3.toArray(new GainItem[gains3.size()]));

            		}else{
                        gains4.add(getGainParticipation());
                        getMainGain().addTo(p.getPlayer(), false, true, gains4.toArray(new GainItem[gains4.size()]));
            		}
            		
                    GainRecap gr = GainRecapManager.getInstance().getRecap(p.getPlayer().getUniqueId());
                    gr.display();
            

        }
        Bukkit.broadcastMessage((String)"§f§m---------------");
        this.setGameState(GameState.END);
        GameTimer.getInstance().setTime(5);
        this.m_winnerFind = true;
    }

    public static int getInt(String key) {
        return instance.getConfig().getInt(key);
    }

    public static boolean getBoolean(String key) {
        return instance.getConfig().getBoolean(key);
    }

    public static String getString(String key) {
        return instance.getConfig().getString(key);
    }

    public static int getPlayerMax() {
        int pts = Data.getInstance().getPoints().size();
        return Math.min(ArrowPlugin.getInt("player-max"), pts);
    }

    public enum GameState {
        CONFIG,
        LOBBY,
        GAME,
        END,
        RESTART;
        

    }
    public Gain getMainGain() {
        return this.mainGain;
    }
    
    public GainItem getGainParticipation() {
        return this.gainParticipation;
    }
    
    public GainItem getGainWin1() {
        return this.gainWin1;
    }
    public GainItem getGainWin2() {
        return this.gainWin2;
    }
    public GainItem getGainWin3() {
        return this.gainWin3;
    }
    


}

