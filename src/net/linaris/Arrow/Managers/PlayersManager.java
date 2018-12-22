
package net.linaris.Arrow.Managers;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import net.linaris.Arrow.ArrowPlugin;
import net.linaris.Arrow.Utils.Constantes;
import net.linaris.Arrow.Utils.Data;
import net.linaris.Arrow.Utils.Utils;

public class PlayersManager {
    private Map<UUID, TucPlayer> m_players = new HashMap<UUID, TucPlayer>();
    private static PlayersManager instance;

    public static PlayersManager getInstance() {
        if (instance == null) {
            instance = new PlayersManager();
        }
        return instance;
    }

    private PlayersManager() {
    }

    public void removePlayer(Player player) {
        this.m_players.remove(player.getUniqueId());
    }

    public TucPlayer getPlayer(Player player) {
        if (this.m_players.containsKey(player.getUniqueId())) {
            return this.m_players.get(player.getUniqueId());
        }
        TucPlayer tplayer = new TucPlayer(this, player);
        this.m_players.put(player.getUniqueId(), tplayer);
        for(Player p : Bukkit.getOnlinePlayers()){
                p.setScoreboard(ScoreBoard.getInstance().getScoreboard());

        }

        return tplayer;
    }

    public Collection<TucPlayer> getPlayers() {
        return this.m_players.values();
    }

    public List<TucPlayer> getRanking() {
        LinkedList<TucPlayer> players = new LinkedList<TucPlayer>(this.getPlayers());
        Utils.sortList(players, new Comparator<TucPlayer>(){

            @Override
            public int compare(TucPlayer p1, TucPlayer p2) {
                return Integer.compare(p2.getKills(), p1.getKills());
            }
        });
        return players;
    }

    public enum KillType {
        ARROW,
        SWORD,
        OTHER;
    }

    public class TucPlayer {
        private Player m_player;
        private int m_kill;
        private int m_killStreak;
        private int m_timeToArrow;
        private int m_timeToJump;
        final  PlayersManager this$0;

        private TucPlayer(PlayersManager playersManager, Player player) {
            this.this$0 = playersManager;
            this.m_kill = 0;
            this.m_killStreak = 0;
            this.m_timeToArrow = 5;
            this.m_timeToJump = 0;
            this.m_player = player;
        }

        public void addKill(KillType killType) {
            ++this.m_kill;
            ++this.m_killStreak;
            ScoreBoard.getInstance().updateScore(this);
            if (this.m_kill >= Constantes.KILL_TO_WIN) {
                ArrowPlugin.getInstance().showWinner();
            }
            if (this.m_killStreak > 8) {
                this.m_killStreak -= 8;
            }
            PlayerInventory inv = this.m_player.getInventory();
            if (this.m_killStreak == 3) {
                Potion potion = new Potion(PotionType.SPEED, 1);
                inv.setItem(this.getNextSlot(), potion.toItemStack(1));
            } else if (this.m_killStreak == 5) {
                ItemStack arc5 = new ItemStack(Material.BOW);
                arc5.addEnchantment(Constantes.BOW_5_IN_1_ENCH, 1);
                inv.setItem(this.getNextSlot(), arc5);
            } else if (this.m_killStreak == 8) {
                ItemStack arcExplode = new ItemStack(Material.BOW);
                arcExplode.addEnchantment(Constantes.BOW_EXPLODE_ENCH, 1);
                inv.setItem(this.getNextSlot(), arcExplode);
            }
            if (killType == KillType.ARROW) {
                this.addArrow(2);
            } else if (killType == KillType.SWORD) {
                this.addArrow(1);
            }
            this.m_player.updateInventory();
        }

        public void killEffect() {
            Location l = this.m_player.getLocation();
            this.m_player.teleport(Data.getInstance().getPoint());
            this.m_player.getInventory().setItem(2, new ItemStack(Material.ARROW, 1));
            this.m_player.setHealth(20.0);
            this.m_player.updateInventory();
            final Firework f = (Firework)l.getWorld().spawn(l, (Class)Firework.class);
            FireworkMeta fm = f.getFireworkMeta();
            fm.addEffect(FireworkEffect.builder().flicker(true).trail(true).with(FireworkEffect.Type.BALL).withColor(Color.WHITE).withFade(Color.WHITE).build());
            fm.setPower(0);
            f.setFireworkMeta(fm);
            Bukkit.getScheduler().scheduleSyncDelayedTask((Plugin)ArrowPlugin.getInstance(), new Runnable(){

                @Override
                public void run() {
                    f.detonate();
                }
            }, 4);
        }

        public Player getPlayer() {
            return this.m_player;
        }

        public UUID getUUID() {
            return this.m_player.getUniqueId();
        }

        public int getKills() {
            return this.m_kill;
        }

        public boolean canDoubleJump() {
            if (this.m_timeToJump <= 0) {
                return true;
            }
            return false;
        }

        public void doubleJump() {
            this.m_timeToJump = 5;
        }

        public void updateTime() {
            --this.m_timeToJump;
            if (this.m_player.getInventory().getItem(2) != null) {
                this.m_player.setLevel(0);
                this.m_timeToArrow = 5;
            } else {
                this.m_player.setLevel(this.m_timeToArrow);
                if (this.m_timeToArrow <= 0) {
                    this.addArrow(1);
                    this.m_timeToArrow = 5;
                    return;
                }
                --this.m_timeToArrow;
            }
        }

        private void addArrow(int amount) {
            PlayerInventory inv = this.m_player.getInventory();
            ItemStack arrow = inv.getItem(2);
            if (arrow == null) {
                inv.setItem(2, new ItemStack(Material.ARROW, amount));
            } else {
                arrow.setAmount(arrow.getAmount() + amount);
            }
        }

        private int getNextSlot() {
            PlayerInventory inv = this.m_player.getInventory();
            int i = 3;
            while (i < 9) {
                if (inv.getItem(i) == null) {
                    return i;
                }
                ++i;
            }
            return 8;
        }



    }

}

