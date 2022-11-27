@file:UseSerializers(BukkitColorToMapSerializer::class, GemStatToString::class)

package me.bscal.mcgems

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.bukkit.Color
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Serializable
data class Gem(val Name: String,
               val Color: Color,
               val Stat: GemStat)

val GemNameKey = NamespacedKey(MCGems.Instance, "GemNameKey");

val NameToGemMap: HashMap<String, Gem> = HashMap(32, .5f);
val NameToGemStatMap: HashMap<String, GemStat> = HashMap(32, .5f);

val STRENGTH_1: GemStat = RegisterGemStat(GemStat("Strength1", {
    it.Strength += 1;
}, {
    it.Strength -= 1;
}));

fun RegisterGem(name: String, color: Color, stat: GemStat): Gem
{
    val gem = Gem(name, color, stat);
    NameToGemMap[gem.Name] = gem;
    return gem;
}

fun RegisterGemStat(gemStat: GemStat) : GemStat
{
    NameToGemStatMap[gemStat.Name] = gemStat;
    return gemStat;
}

fun GemFromItemStack(itemStack: ItemStack): Gem?
{
    if (!itemStack.hasItemMeta()) return null;
    val gemName = itemStack.itemMeta
            .persistentDataContainer[GemNameKey, PersistentDataType.STRING] ?: return null;
    return NameToGemMap[gemName];
}

data class GemStat(
        val Name: String,
        val ApplyStats: (GemPlayer) -> Unit,
        val RemoveStats: (GemPlayer) -> Unit);

val TEST_GEM: Gem = RegisterGem("TestGem", Color.GRAY, STRENGTH_1);