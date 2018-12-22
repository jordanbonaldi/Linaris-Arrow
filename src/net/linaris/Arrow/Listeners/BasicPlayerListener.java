
package net.linaris.Arrow.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.linaris.Arrow.ArrowPlugin;
import net.linaris.Arrow.Managers.PlayersManager;
import net.linaris.Arrow.Timers.GameTimer;
import net.linaris.Arrow.Utils.Data;
import net.linaris.Arrow.Utils.Utils;

public class BasicPlayerListener
implements Listener {
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (ArrowPlugin.getGameState() != ArrowPlugin.GameState.CONFIG && ArrowPlugin.getGameState() != ArrowPlugin.GameState.LOBBY) {
        	if(!event.getPlayer().hasPermission("game.staff")){
                event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "&cLa partie est en cours !");

        	}else{
        		event.getPlayer().setFlying(true);
        		event.getPlayer().setFlySpeed(2);
				PotionEffect inv = PotionEffectType.INVISIBILITY.createEffect(10000000, 1000000000);
		        event.getPlayer().addPotionEffect(inv, true);
        	}
        } 
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
        PlayersManager.getInstance().removePlayer(event.getPlayer());
        if (ArrowPlugin.getGameState() == ArrowPlugin.GameState.LOBBY) {
        } else {
            event.setQuitMessage(null);
        }
        if (Bukkit.getOnlinePlayers().size() < ArrowPlugin.getInt("player-min") && ArrowPlugin.getGameState() == ArrowPlugin.GameState.LOBBY) {
            GameTimer.getInstance().setTime(9999);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (ArrowPlugin.getGameState() == ArrowPlugin.GameState.LOBBY) {
            event.setRespawnLocation(Data.getInstance().getLobby());
            event.getPlayer().setGameMode(GameMode.SURVIVAL);
        }
        Utils.setInventory(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.getDrops().clear();
        event.setDeathMessage(null);
        event.setDroppedExp(0);
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled=true)
    public void onPlayerPickupItemEvent(PlayerPickupItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled=true)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getFoodLevel() < 20) {
            event.setFoodLevel(20);
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (ArrowPlugin.getGameState() != ArrowPlugin.GameState.CONFIG) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (ArrowPlugin.getGameState() != ArrowPlugin.GameState.CONFIG) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        event.setCancelled(true);
        Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)ArrowPlugin.getInstance(), new Runnable(){

            @Override
            public void run() {
                event.getWhoClicked().closeInventory();
            }
        });
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        event.setCancelled(true);
    }
    
    
	
		


}

