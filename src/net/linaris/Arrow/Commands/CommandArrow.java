
package net.linaris.Arrow.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import net.linaris.Arrow.ArrowPlugin;
import net.linaris.Arrow.Timers.GameTimer;
import net.linaris.Arrow.Utils.Data;

public class CommandArrow
implements CommandExecutor,
Listener {
    public CommandArrow() {
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)ArrowPlugin.getInstance());
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage((Object)ChatColor.RED + "You must be a player !");
            return true;
        }
        if (!sender.isOp()) {
            sender.sendMessage((Object)ChatColor.RED + "You must be an admin !");
            return true;
        }
        Player player = (Player)sender;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("setlobby")) {
                Location l = player.getLocation();
                Data.getInstance().setLobby(l);
                l.getWorld().setSpawnLocation(l.getBlockX(), l.getBlockY(), l.getBlockZ());
                player.sendMessage((Object)ChatColor.GREEN + "Lobby define !");
                return true;
            }
            if (args[0].equalsIgnoreCase("lobby")) {
                player.teleport(Data.getInstance().getLobby());
                return true;
            }
            if (args[0].equalsIgnoreCase("start")) {
                if (ArrowPlugin.getGameState() == ArrowPlugin.GameState.CONFIG) {
                    ArrowPlugin.getInstance().getConfig().set("config-mode", (Object)false);
                    ArrowPlugin.getInstance().saveConfig();
                    player.sendMessage((Object)ChatColor.GREEN + "Config mode disable, please restart the server");
                    return true;
                }
                if (ArrowPlugin.getGameState() == ArrowPlugin.GameState.LOBBY) {
                    GameTimer.getInstance().setTime(2);
                    return true;
                }
            } else {
                if (args[0].equalsIgnoreCase("AddPoint")) {
                    Data.getInstance().addPoint(player.getLocation());
                    player.sendMessage((Object)ChatColor.GREEN + "Point set !");
                    return true;
                }
                if (args[0].equalsIgnoreCase("ClearPoints")) {
                    Data.getInstance().clearPoints();
                    player.sendMessage((Object)ChatColor.GREEN + "Point set !");
                    return true;
                }
            }
        }
        return false;
    }
}

