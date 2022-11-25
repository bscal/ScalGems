package me.bscal.mcgems;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class MCGems extends JavaPlugin
{

    public static MCGems Instance;

    @Override
    public void onEnable()
    {
        Instance = this;
    }

    @Override
    public void onDisable()
    {
        Instance = null;
    }

    public static class PlayerListener implements Listener
    {
        @EventHandler
        public void OnJoin(PlayerJoinEvent evt)
        {
            PlayerMgrKt.CreateGemPlayer(evt.getPlayer());
        }

        @EventHandler
        public void OnQuit(PlayerQuitEvent evt)
        {
            PlayerMgrKt.RemoveGemPlayer(evt.getPlayer().getUniqueId());
        }

        @EventHandler
        public void OnDeath(PlayerDeathEvent evt)
        {
            PlayerMgrKt.RemoveAllStatsFromPlayer(evt.getPlayer().getUniqueId());
        }

        @EventHandler
        public void OnEquipmentChange(PlayerArmorChangeEvent evt)
        {
            GemPlayer gemPlayer = PlayerMgrKt.getGemPlayerMap().get(evt.getPlayer().getUniqueId());
            if (gemPlayer == null) return;

            ItemStack oldItem = evt.getOldItem();
            if (oldItem != null)
            {
                GemGearSet gemGearSet = GemKt.DeserializeItemStack(oldItem);
                if (gemGearSet != null)
                {
                    gemGearSet.Unequip(gemPlayer);
                }
            }

            ItemStack newItem = evt.getNewItem();
            if (newItem != null)
            {
                GemGearSet gemGearSet = GemKt.DeserializeItemStack(newItem);
                if (gemGearSet != null)
                {
                    gemGearSet.Equip(gemPlayer);
                }
            }
        }
    }

}
