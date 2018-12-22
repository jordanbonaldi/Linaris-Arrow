package net.linaris.Arrow.Listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.linaris.Arrow.ArrowPlugin;
import net.linaris.Arrow.Managers.PlayersManager;
import net.linaris.Arrow.Utils.Constantes;
import net.linaris.Arrow.Utils.Data;

public class GameListener
implements Listener {
    @EventHandler(priority=EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        PlayersManager.TucPlayer damager;
        if (ArrowPlugin.getGameState() != ArrowPlugin.GameState.GAME) {
            return;
        }
        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }
        PlayersManager.TucPlayer damaged = PlayersManager.getInstance().getPlayer((Player)event.getEntity());
        if (event.getDamager().getType() == EntityType.ARROW) {
            Arrow arrow = (Arrow)event.getDamager();
            if (arrow.getShooter() instanceof Player) {
                PlayersManager.TucPlayer killer = PlayersManager.getInstance().getPlayer((Player)arrow.getShooter());
                killer.addKill(PlayersManager.KillType.ARROW);
                damaged.killEffect();
            }
        } else if (event.getDamager().getType() == EntityType.PLAYER && (damager = PlayersManager.getInstance().getPlayer((Player)event.getDamager())).getPlayer().getItemInHand().getType() == Constantes.SWORD_ITEM) {
            if (damaged.getPlayer().getHealth() <= 7.0) {
                damager.addKill(PlayersManager.KillType.SWORD);
                damaged.killEffect();
            } else {
                event.setDamage(7.0);
                event.setCancelled(false);
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (ArrowPlugin.getGameState() != ArrowPlugin.GameState.GAME) {
            return;
        }
        if (event.getItem() == null) {
            event.setCancelled(true);
            return;
        }
        Player player = event.getPlayer();
        if (event.getItem().getType() == Material.POTION) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 2, false));
            player.setItemInHand(null);
            event.setCancelled(true);
        } else if (event.getItem().getType() == Material.BOW) {
            if (event.getItem().getEnchantmentLevel(Constantes.BOW_5_IN_1_ENCH) > 0) {
                Arrow base = (Arrow)player.launchProjectile((Class)Arrow.class);
                base.setBounce(false);
                Vector v = base.getVelocity();
                double xz = -0.1;
                while (xz <= 0.1) {
                    double y = -0.1;
                    while (y <= 0.1) {
                        Arrow arrow = (Arrow)player.launchProjectile((Class)Arrow.class);
                        arrow.setVelocity(v.clone().add(new Vector(xz, y, xz)));
                        arrow.setBounce(false);
                        y += 0.2;
                    }
                    xz += 0.2;
                }
                player.setItemInHand(null);
                event.setCancelled(true);
            } else if (event.getItem().getEnchantmentLevel(Constantes.BOW_EXPLODE_ENCH) > 0) {
                Arrow arrow = (Arrow)player.launchProjectile((Class)Arrow.class);
                arrow.setBounce(false);
                arrow.setMetadata("explode", (MetadataValue)new FixedMetadataValue((Plugin)ArrowPlugin.getInstance(), (Object)true));
                player.setItemInHand(null);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity().getType() == EntityType.ARROW) {
            Arrow arrow = (Arrow)event.getEntity();
            arrow.remove();
            if (!(arrow.getShooter() instanceof Player)) {
                return;
            }
            PlayersManager.TucPlayer shooter = PlayersManager.getInstance().getPlayer((Player)arrow.getShooter());
            if (arrow.hasMetadata("explode")) {
                Location l = arrow.getLocation();
                for (Entity entity : arrow.getNearbyEntities(4.0, 4.0, 4.0)) {
                    if (entity.getType() != EntityType.PLAYER || entity.getUniqueId().equals(shooter.getUUID())) continue;
                    PlayersManager.TucPlayer dead = PlayersManager.getInstance().getPlayer((Player)entity);
                    shooter.addKill(PlayersManager.KillType.OTHER);
                    dead.killEffect();
                }
                arrow.getLocation().getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 4.0f, false, false);
            }
        }
    }

    @EventHandler
    public void onPlayerFly(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        PlayersManager.TucPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
        event.setCancelled(true);
        player.setAllowFlight(false);
        player.setFlying(false);
        if (tplayer.canDoubleJump()) {
            player.setVelocity(player.getLocation().getDirection().multiply(1.2).setY(0.9));
            player.playSound(player.getLocation(), Sound.GHAST_SCREAM, 1.0f, 1.0f);
            tplayer.doubleJump();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (ArrowPlugin.getGameState() == ArrowPlugin.GameState.GAME) {
            Player player = event.getPlayer();
            if (player.getLocation().getY() < 0.0) {
                player.teleport(Data.getInstance().getPoint());
                return;
            }
            PlayersManager.TucPlayer tplayer = PlayersManager.getInstance().getPlayer(player);
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            if (tplayer.canDoubleJump()) {
                if (player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
                    player.setAllowFlight(true);
                }
            } else {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
        }
    }
}

