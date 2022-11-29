package me.bscal.mcgems

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.builder.RequiredArgumentBuilder.argument
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.adventure.AdventureComponent
import net.kyori.adventure.text.Component
import net.minecraft.commands.CommandSourceStack
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType


private const val PLAYER_ARG = "player"
private const val GEM_ARG = "gem_type"

fun SocketCommand(ctx: CommandContext<CommandSourceStack>): Int
{
    // TODO kinda broken!

    val player = ctx.source.player ?: return 0;
    val bukkitPlayer = player.bukkitEntity;
    val handStack = bukkitPlayer.equipment.itemInMainHand;
    if (handStack.type == Material.AIR) return 0;
    val handGemGearSet = DeserializeGemItemStack(handStack) ?: return 0;
    // TODO implement gems
    //val gem: Gem = GemFromItemStack(handBukkitStack) ?: return 0;
    val gem = TEST_GEM;
    //handGemGearSet.Socket(bukkitPlayer, 0, gem, handStack);

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
        Color.WHITE
    }, null);
    gemItemStack.Serialize(handStack);

    val gemGearSetJson = handStack.itemMeta.persistentDataContainer
            .get(GemItemStackKey, PersistentDataType.STRING);
    println(gemGearSetJson)

    player.bukkitEntity.sendMessage("Successfully added a socket to your item!");
    return 1;
}

fun SocketGUICommand(ctx: CommandContext<CommandSourceStack>): Int
{
    val player = ctx.source.player ?: return 0;
    val bukkitPlayer = player.bukkitEntity;
    val gemPlayer = GemPlayerMap[bukkitPlayer.uniqueId] ?: return 0;
    val handStack = bukkitPlayer.equipment.itemInMainHand;
    if (handStack.type == Material.AIR) return 0;
    val handGemGearSet = DeserializeGemItemStack(handStack) ?: return 0;

    try
    {
        CreateGemSmithUi(bukkitPlayer, gemPlayer, handStack, handGemGearSet);
    } catch (e: Exception)
    {
        e.printStackTrace()
    }

    return 1;
}

fun GiveGemCommand(ctx: CommandContext<CommandSourceStack>): Int
{
    val gemArg = ctx.getArgument(GEM_ARG, String::class.java);
    val playerArg = ctx.getArgument(PLAYER_ARG, String::class.java);

    val gemStack = NameToGemStackMap[gemArg]
    if (gemStack == null)
    {
        val component = AdventureComponent(Component.text("Gem name could not be found!"));
        ctx.source.sendFailure(component);
        return 0;
    }

    val player: Player? = Bukkit.getPlayer(playerArg)
    if (player == null)
    {
        val component = AdventureComponent(Component.text("Player could not be found!"));
        ctx.source.sendFailure(component);
        return 0;
    }

    val component = AdventureComponent(
            Component.text("Giving $gemArg to $playerArg!"));
    ctx.source.sendSuccess(component, true);

    player.inventory.addItem(gemStack.DefaultStack)

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

fun InitCommands(server: Server)
{
    val dispatcher: CommandDispatcher<CommandSourceStack> =
            (server as org.bukkit.craftbukkit.v1_19_R1.CraftServer)
            .server.vanillaCommandDispatcher.dispatcher

    dispatcher.register(literal<CommandSourceStack?>("MCGems")
            .then(literal<CommandSourceStack?>("socket")
                    .executes {
                        SocketCommand(it);
                    }))

    dispatcher.register(literal<CommandSourceStack?>("MCGems")
            .then(literal<CommandSourceStack?>("addsocket")
                    .executes {
                        AddSocketCommand(it);
                    }))


    dispatcher.register(literal<CommandSourceStack?>("MCGems")
            .then(literal<CommandSourceStack?>("debug")
                    .executes {
                        DebugItemCommand(it);
                    }))

    dispatcher.register(literal<CommandSourceStack?>("MCGems")
            .then(literal<CommandSourceStack?>("gui")
                    .executes {
                        SocketGUICommand(it);
                    }))

    dispatcher.register(literal<CommandSourceStack?>("MCGems")
            .then(literal<CommandSourceStack?>("givegem")
                    .then(argument<CommandSourceStack?, String?>(GEM_ARG, StringArgumentType.word())
                            .suggests { _, builder ->
                                for (name in NameList)
                                    builder.suggest(name);
                                builder.buildFuture()
                            }.then(argument<CommandSourceStack?, String?>
                            (PLAYER_ARG, StringArgumentType.word())
                                    .suggests { _, builder ->
                                        for (p in Bukkit.getOnlinePlayers())
                                            builder.suggest(p.name)
                                        builder.buildFuture()
                                    }).executes {
                                GiveGemCommand(it)
                            })))



}