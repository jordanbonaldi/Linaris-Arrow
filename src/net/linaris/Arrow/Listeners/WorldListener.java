
package net.linaris.Arrow.Listeners;

import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class WorldListener
implements Listener {
    private boolean m_ignoreNextWeatherChange = false;

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (this.m_ignoreNextWeatherChange) {
            this.m_ignoreNextWeatherChange = false;
            return;
        }
        if (!event.getWorld().hasStorm()) {
            event.setCancelled(true);
        } else {
            this.m_ignoreNextWeatherChange = true;
            event.getWorld().setStorm(false);
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getEntityType() != EntityType.WITHER) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        event.setCancelled(true);
    }
}

