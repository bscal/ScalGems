package me.bscal.mcgems;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.mojang.brigadier.CommandDispatcher;
import io.github.rysefoxx.inventory.plugin.pagination.InventoryManager;
import io.papermc.paper.brigadier.PaperBrigadier;
import net.minecraft.commands.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class MCGems extends JavaPlugin implements Listener
{

    // TODO list
    // Permissions
    // Adding sockets? crafting, item?
    // GemStats, and GemPlayer stats

    public static MCGems Instance;
    public static InventoryManager InventoryManager;

    @Override
    public void onLoad()
    {
        Instance = this;
    }

    @Override
    public void onEnable()
    {
        InventoryManager = new InventoryManager(this);
        InventoryManager.invoke();

        Bukkit.getPluginManager().registerEvents(this, this);
        GemKt.InitGemColors();

        CommandsKt.InitCommands(getServer());
    }

    @Override
    public void onDisable()
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
