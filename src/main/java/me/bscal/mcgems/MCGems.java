package me.bscal.mcgems;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import net.axay.kspigot.main.KSpigot;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public final class MCGems extends KSpigot implements Listener
{

    // TODO list
    // Permissions
    // Gem items
    // Socket ui
    // Adding sockets? crafting, item?
    // GemStats, and GemPlayer stats

    public static MCGems Instance;

    @Override
    public void load()
    {
        Instance = this;
    }

    @Override
    public void startup()
    {
        Bukkit.getPluginManager().registerEvents(this, this);
        CommandsKt.InitCommands();
        GemKt.InitGemColors();
    }

    @Override
    public void shutdown()
    {
        Instance = null;
    }

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
            GemItemStack gemGearSet = GemItemStackKt.DeserializeGemItemStack(oldItem);
            if (gemGearSet != null)
            {
                gemGearSet.Unequip(gemPlayer);
            }
        }

        ItemStack newItem = evt.getNewItem();
        if (newItem != null)
        {
            GemItemStack gemGearSet = GemItemStackKt.DeserializeGemItemStack(newItem);
            if (gemGearSet != null)
            {
                gemGearSet.Equip(gemPlayer);
            }
        }
    }

}
