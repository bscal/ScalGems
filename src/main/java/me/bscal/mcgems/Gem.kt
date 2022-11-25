package me.bscal.mcgems

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.axay.kspigot.data.nbtData
import org.bukkit.Color
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

data class Gem(val Color: Color, val Stat: GemStat)

val STRENGTH_1: GemStat = GemStat({
    it.Strength += 1;
}, {
    it.Strength -= 1;
});

data class GemStat(
        val ApplyStats: (GemPlayer) -> Unit,
        val RemoveStats: (GemPlayer) -> Unit);

class GemGearSet
{
    lateinit var ColorArray: Array<Color>;
    lateinit var GemArray: Array<Gem?>;
    var SocketBonus: GemStat? = null;
    var IsBonusActive: Boolean = false;

    fun Initialize(colors: Array<Color>, socketBonus: GemStat)
    {
        ColorArray = colors;
        GemArray = arrayOfNulls(colors.size);
        SocketBonus = socketBonus;
    }

    fun Socket(slot: Int, gem: Gem) : Boolean
    {
        if (slot >= GemArray.size) return false;

        GemArray[slot] = gem;

        var allSocketsFilled: Boolean = false;
        for (i in 0..GemArray.size)
        {
            if (GemArray[i]?.Color != ColorArray[i])
            {
                allSocketsFilled = false;
                break;
            }
        }

        if (allSocketsFilled)
        {
            IsBonusActive = true;
        }

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

    fun SerializeItemStack(itemStack: ItemStack)
    {
        val im = itemStack.itemMeta;
        val serializedGemGearSet: String = Json.encodeToString(this);
        val gemGearSetKey = NamespacedKey(MCGems.Instance, "GEM_GEAR_SET");
        im.persistentDataContainer.set(gemGearSetKey, PersistentDataType.STRING, serializedGemGearSet);
        itemStack.itemMeta = im;
    }

}

fun DeserializeItemStack(itemStack: ItemStack) : GemGearSet?
{
    if (itemStack.hasItemMeta()) return null;
    val im = itemStack.itemMeta;
    val gemGearSetKey = NamespacedKey(MCGems.Instance, "GEM_GEAR_SET");
    val gemGearSet = im.persistentDataContainer.get(gemGearSetKey, PersistentDataType.STRING)
            ?: return null;
    val deserializeGemGearSet = Json.decodeFromString<GemGearSet>(gemGearSet);
    return deserializeGemGearSet;
}
