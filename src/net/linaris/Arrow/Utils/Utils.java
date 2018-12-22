
package net.linaris.Arrow.Utils;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.linaris.Arrow.ArrowPlugin;

public abstract class Utils {
    public static List<Player> m_playersJustConnected = new LinkedList<Player>();

    public static Location toLocation(String string, boolean block) {
        String[] splitted = string.split(";");
        World world = Bukkit.getWorld((String)splitted[0]);
        if (world == null || splitted.length < 4) {
            return null;
        }
        Location location = new Location(world, (double)Integer.parseInt(splitted[1]), (double)Integer.parseInt(splitted[2]), (double)Integer.parseInt(splitted[3]));
        if (!block && splitted.length >= 6) {
            location.setYaw(Float.parseFloat(splitted[4]));
            location.setPitch(Float.parseFloat(splitted[5]));
        }
        return location;
    }

    public static String toString(Location l, boolean block) {
        StringBuilder sb = new StringBuilder();
        sb.append(l.getWorld().getName()).append(";");
        if (block) {
            sb.append(l.getX()).append(";").append(l.getY()).append(";").append(l.getZ());
        } else {
            sb.append(l.getBlockX()).append(";").append(l.getBlockY()).append(";").append(l.getBlockZ());
            sb.append(";").append(l.getYaw()).append(";").append(l.getPitch());
        }
        return sb.toString();
    }

    public static void setInventory(Player player) {
        PlayerInventory inv = player.getInventory();
        if (ArrowPlugin.getGameState() == ArrowPlugin.GameState.LOBBY) {
            inv.clear();
        } else if (ArrowPlugin.getGameState() == ArrowPlugin.GameState.GAME) {
            inv.clear();
            inv.setItem(0, new ItemStack(Material.WOOD_SWORD, 1));
            inv.setItem(1, new ItemStack(Material.BOW, 1));
            inv.setItem(2, new ItemStack(Material.ARROW, 1));
        }
        player.updateInventory();
    }

    public static void resetPlayer(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setMaxHealth(20.0);
        player.setHealth(20.0);
        player.setLevel(0);
        player.setExp(0.0f);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);
        player.setAllowFlight(false);
        player.setFlying(false);
		List<PotionEffect> effects = new LinkedList<PotionEffect>(player.getActivePotionEffects());
		for(PotionEffect effect : effects) { player.removePotionEffect(effect.getType()); }
        
        player.updateInventory();
    }

    public static void tpToLobby(Player player) {
        if (ArrowPlugin.getBoolean("enable-bungeecord")) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(ArrowPlugin.getString("bungeecord-lobby"));
            player.sendPluginMessage((Plugin)ArrowPlugin.getInstance(), "BungeeCord", out.toByteArray());
        } else {
            player.kickPlayer("Kicked by the plugin");
        }
    }

    public static void kick(Player player, String message) {
        if (ArrowPlugin.getBoolean("enable-bungeecord")) {
            player.sendMessage(message);
            Utils.tpToLobby(player);
        } else {
            player.kickPlayer(message);
        }
    }

    public static <T> void sortList(List<T> list, Comparator<T> c) {
        int i = 0;
        while (i < list.size() - 1) {
            T obj2;
            T obj1 = list.get(i);
            if (c.compare(obj1, obj2 = list.get(i + 1)) > 0) {
                list.set(i, obj2);
                list.set(i + 1, obj1);
                if ((i -= 2) <= -2) {
                    ++i;
                }
            }
            ++i;
        }
    }
}

