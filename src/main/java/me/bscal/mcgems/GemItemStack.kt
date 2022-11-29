@file:UseSerializers(BukkitColorToMapSerializer::class, GemStatToString::class)

package me.bscal.mcgems

import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

val GemItemStackKey = NamespacedKey(MCGems.Instance, "GemItemStackKey");

@Serializable
class GemItemStack
{
    lateinit var ColorArray: Array<Color>;
    lateinit var GemArray: Array<Gem?>;
    var SocketBonus: GemStat? = null;
    var IsBonusActive: Boolean = false;

    fun Socket(player: Player, gemPlayer: GemPlayer, slot: Int, gem: Gem, gemItemStack: ItemStack): Boolean
    {
        if (slot >= GemArray.size) return false;

        if (gem.Color != ColorArray[slot])
        {
            val style = Style.style(TextColor.color(1.0f, 0.0f, 0.0f));
            val text = Component.text("GemSlot and Gem color do not match!", style);
            player.sendMessage(text);
            return false;
        }

        GemArray[slot] = gem;

        var allSocketsFilled = true;
        for (i in GemArray.indices)
        {
            if (GemArray[i] == null || GemArray[i]?.Color != ColorArray[i])
            {
                allSocketsFilled = false;
                break;
            }
        }

        if (allSocketsFilled)
        {
            IsBonusActive = true;
        }

        Serialize(gemItemStack);

        Unequip(gemPlayer);
        Equip(gemPlayer);

        return true;
    }

    fun Equip(gemPlayer: GemPlayer)
    {
        for (gem in GemArray)
        {
            gem?.Stat?.ApplyStats?.invoke(gemPlayer);
        }
        if (IsBonusActive)
        {
            SocketBonus?.ApplyStats?.invoke(gemPlayer);
        }
    }

    fun Unequip(gemPlayer: GemPlayer)
    {
        for (gem in GemArray)
        {
            gem?.Stat?.ApplyStats?.invoke(gemPlayer);
        }
        if (IsBonusActive)
        {
            SocketBonus?.RemoveStats?.invoke(gemPlayer);
        }
    }

    fun Serialize(itemStack: ItemStack)
    {
        val im = itemStack.itemMeta;
        val gemGearSetJson = Json.encodeToString(this);
        im.persistentDataContainer.set(GemItemStackKey, PersistentDataType.STRING, gemGearSetJson);
        itemStack.itemMeta = im;
    }
}

fun CreateGemItemStack(colors: Array<Color>, socketBonus: GemStat?): GemItemStack
{
    val result = GemItemStack();
    result.ColorArray = colors;
    result.GemArray = arrayOfNulls(colors.size);
    result.SocketBonus = socketBonus;
    return result;
}

fun DeserializeGemItemStack(itemStack: ItemStack): GemItemStack?
{
    if (!itemStack.hasItemMeta()) return null;
    val im = itemStack.itemMeta;
    val gemGearSetJson = im.persistentDataContainer
            .get(GemItemStackKey, PersistentDataType.STRING)
            ?: return null;
    val deserializeGemGearSet: GemItemStack = Json.decodeFromString(gemGearSetJson);
    return deserializeGemGearSet;
}
