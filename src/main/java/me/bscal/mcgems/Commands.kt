package me.bscal.mcgems

import com.mojang.brigadier.context.CommandContext
import net.axay.kspigot.commands.command
import net.axay.kspigot.commands.literal
import net.minecraft.commands.CommandSourceStack
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.persistence.PersistentDataType

fun SocketCommand(ctx: CommandContext<CommandSourceStack>): Int
{
    val player = ctx.source.player ?: return 0;
    val bukkitPlayer = player.bukkitEntity;
    val handStack = bukkitPlayer.equipment.itemInMainHand;
    if (handStack.type == Material.AIR) return 0;
    val handGemGearSet = DeserializeGemItemStack(handStack) ?: return 0;
    // TODO implement gems
    //val gem: Gem = GemFromItemStack(handBukkitStack) ?: return 0;
    val gem = TEST_GEM;
    handGemGearSet.Socket(bukkitPlayer, 0, gem, handStack);

    player.bukkitEntity.sendMessage("Successfully socketed a gem!");

    return 1;
}

fun AddSocketCommand(ctx: CommandContext<CommandSourceStack>): Int
{
    val player = ctx.source.player ?: return 0;
    val bukkitPlayer = player.bukkitEntity;

    val handStack = bukkitPlayer.equipment.itemInMainHand;
    if (handStack.type == Material.AIR) return 0;

    val gemItemStack = CreateGemItemStack(Array(1) {
        Color.GRAY
    }, null);
    gemItemStack.Serialize(handStack);

    val gemGearSetJson = handStack.itemMeta.persistentDataContainer
            .get(GemItemStackKey, PersistentDataType.STRING);
    println(gemGearSetJson)

    player.bukkitEntity.sendMessage("Successfully added a socket to your item!");
    return 1;
}

fun DebugItemCommand(ctx: CommandContext<CommandSourceStack>): Int
{
    val player = ctx.source.player ?: return 0;
    val bukkitPlayer = player.bukkitEntity;
    val handStack = bukkitPlayer.equipment.itemInMainHand;

    if (handStack.type == Material.AIR)
    {
        bukkitPlayer.sendMessage("[MCGems] No item in hand!");
        return 0;
    }

    val getItemStack: GemItemStack? = DeserializeGemItemStack(handStack)
    if (getItemStack == null)
    {
        bukkitPlayer.sendMessage("[MCGems] ItemStack does not contain any gem data!");
        return 0;
    }

    bukkitPlayer.sendMessage("[[MCGems] Printing GemItemStack Data]");
    bukkitPlayer.sendMessage("Colors:");

    var colorStr = "  ["
    for (color in getItemStack.ColorArray)
    {
        colorStr += color;
        colorStr += ", ";
    }
    colorStr += "]";
    bukkitPlayer.sendMessage(colorStr);

    bukkitPlayer.sendMessage("Gems:");
    var gemStr = "  ["
    for (gem in getItemStack.GemArray)
    {
        gemStr += gem;
        gemStr += ", ";
    }
    gemStr += "]";
    bukkitPlayer.sendMessage(gemStr);

    bukkitPlayer.sendMessage("Socket Bonus: " + getItemStack.SocketBonus);
    bukkitPlayer.sendMessage("Has Bonus: " + getItemStack.IsBonusActive);

    val gemGearSetJson = handStack.itemMeta.persistentDataContainer
            .get(GemItemStackKey, PersistentDataType.STRING);
    println(gemGearSetJson)

    return 1;
}

fun InitCommands()
{
    command("MCGems") {
        literal("socket") {
            executes {
                SocketCommand(it);
            }
        }
    }

    command("MCGems") {
        literal("addsocket") {
            executes {
                AddSocketCommand(it);
            }
        }
    }

    command("MCGems") {
        literal("debug") {
            executes {
                DebugItemCommand(it);
            }
        }
    }
}