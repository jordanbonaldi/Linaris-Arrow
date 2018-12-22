
package net.linaris.Arrow.Utils;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.linaris.Arrow.ArrowPlugin;
import net.linaris.Arrow.Managers.PlayersManager;
import net.linaris.Arrow.Utils.Utils;

public class Data {
    private File m_file;
    private FileConfiguration m_yaml;
    private List<Location> m_points = new LinkedList<Location>();
    private Random m_rnd = new Random();
    private static Data instance = null;

    public static Data getInstance() {
        if (instance == null) {
            instance = new Data();
        }
        return instance;
    }

    private Data() {
        try {
            File dirs = new File("plugins/" + ArrowPlugin.getInstance().getName() + "/data");
            dirs.mkdirs();
            this.m_file = new File("plugins/" + ArrowPlugin.getInstance().getName() + "/data/data.yml");
            if (!this.m_file.exists()) {
                this.m_file.createNewFile();
            }
            this.m_yaml = YamlConfiguration.loadConfiguration((File)this.m_file);
            for (String point : this.m_yaml.getStringList("points")) {
                this.m_points.add(Utils.toLocation(point, false));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Location getLobby() {
        if (this.m_yaml.contains("lobby")) {
            return Utils.toLocation(this.m_yaml.getString("lobby"), false);
        }
        return ((World)Bukkit.getWorlds().get(0)).getSpawnLocation();
    }

    public void setLobby(Location lobby) {
        try {
            this.m_yaml.set("lobby", (Object)Utils.toString(lobby, false));
            this.m_yaml.save(this.m_file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPoint(Location loc) {
        try {
            this.m_points.add(loc);
            LinkedList<String> pts_str = new LinkedList<String>();
            for (Location l : this.m_points) {
                pts_str.add(Utils.toString(l, false));
            }
            this.m_yaml.set("points", pts_str);
            this.m_yaml.save(this.m_file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearPoints() {
        try {
            this.m_points.clear();
            this.m_yaml.set("points", (Object)null);
            this.m_yaml.save(this.m_file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tpToPoint() {
        LinkedList<Location> points = new LinkedList<Location>(this.m_points);
        for (PlayersManager.TucPlayer player : PlayersManager.getInstance().getPlayers()) {
            int point_id = this.m_rnd.nextInt(points.size());
            player.getPlayer().teleport(points.get(point_id));
            points.remove(point_id);
        }
    }

    public Location getPoint() {
        return this.m_points.get(this.m_rnd.nextInt(this.m_points.size()));
    }

    public List<Location> getPoints() {
        return this.m_points;
    }
}

