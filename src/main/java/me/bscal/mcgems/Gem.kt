@file:UseSerializers(BukkitColorToMapSerializer::class, GemStatToString::class)

package me.bscal.mcgems

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.axay.kspigot.chat.KColors
import net.axay.kspigot.extensions.bukkit.textColor
import net.axay.kspigot.items.addLore
import net.axay.kspigot.items.itemStack
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.Material
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
val NameToGemStackMap: HashMap<String, GemStack> = HashMap(32, .5f)
val GemColorToPane: HashMap<Color, ItemStack> = HashMap();
val UiItemStacks: HashSet<ItemStack> = HashSet();

private fun RegisterGemColorItem(color: Color, itemStack: ItemStack)
{
    UiItemStacks.add(itemStack);
    GemColorToPane[color] = ItemStack(Material.WHITE_STAINED_GLASS_PANE);
}

fun InitGemColors()
{
    RegisterGemColorItem(Color.WHITE, ItemStack(Material.WHITE_STAINED_GLASS_PANE));
    RegisterGemColorItem(Color.RED, ItemStack(Material.RED_STAINED_GLASS_PANE));
    RegisterGemColorItem(Color.BLUE, ItemStack(Material.BLUE_STAINED_GLASS_PANE));
    RegisterGemColorItem(Color.GREEN, ItemStack(Material.GREEN_STAINED_GLASS_PANE));
}

val STRENGTH_1: GemStat = RegisterGemStat(GemStat(
        "strength1", "+1 Strength", {
    it.Strength += 1;
}, {
    it.Strength -= 1;
}));

val TEST_GEM: Gem = RegisterGem("test_name", Color.WHITE, STRENGTH_1,
        "Rough Test Gem", Material.DIAMOND);

fun RegisterGem(name: String, color: Color, stat: GemStat,
                displayName: String, material: Material): Gem
{
    val gem = Gem(name, color, stat);
    val gemStack = GemStack(displayName, material, gem);
    NameToGemMap[gem.Name] = gem;
    NameToGemStackMap[gem.Name] = gemStack;
    UiItemStacks.add(gemStack.DefaultStack);
    return gem;
}

fun RegisterGemStat(gemStat: GemStat): GemStat
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
        val LoreStr: String,
        val ApplyStats: (GemPlayer) -> Unit,
        val RemoveStats: (GemPlayer) -> Unit);


private var NextModelIds: Int = 10000

class GemStack(displayName: String, material: Material, gem: Gem)
{
    val ModelId: Int;
    val DefaultStack: ItemStack

    init
    {
        ModelId = NextModelIds++;
        DefaultStack = itemStack(material)
        {
            editMeta {
                val name = Component.text(displayName, gem.Color.textColor);
                it.displayName(name)
                it.addLore {
                    val lore = Component.text(gem.Stat.LoreStr, KColors.LIGHTGREEN);
                    this.lorelist.add(lore);
                }
                it.setCustomModelData(ModelId);
            }
        }
    }
}