@file:UseSerializers(BukkitColorToMapSerializer::class, GemStatToString::class)

package me.bscal.mcgems

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

@Serializable
data class Gem(val Name: String,
               val Color: Color,
               val Stat: GemStat)

val GemNameKey = NamespacedKey(MCGems.Instance, "GemNameKey");

val NameList: ArrayList<String> = ArrayList();
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
    NameList.add(gem.Name);
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
            .persistentDataContainer[GemNameKey, PersistentDataType.STRING]
            ?: return null;
    return NameToGemMap[gemName];
}

fun GemAddToItemMeta(gem: Gem, itemMeta: ItemMeta)
{
    itemMeta.persistentDataContainer.set(GemNameKey, PersistentDataType.STRING, gem.Name);
}

fun ColorToTextColor(color: Color): TextColor
{
    return TextColor.color(color.red, color.green, color.blue)
}

data class GemStat(
        val Name: String,
        val LoreStr: String,
        val ApplyStats: (GemPlayer) -> Unit,
        val RemoveStats: (GemPlayer) -> Unit);


private var NextModelIds: Int = 10000

val STAT_COLOR = TextColor.color(0, 255, 0);

class GemStack(displayName: String, material: Material, gem: Gem)
{
    val ModelId: Int;
    val DefaultStack: ItemStack

    init
    {
        ModelId = NextModelIds++;
        DefaultStack = ItemStack(material)

        val im = DefaultStack.itemMeta;
        val name = Component.text(displayName, ColorToTextColor(gem.Color));
        im.displayName(name);

        val lore = ArrayList<Component>(1)
        val lineStat = Component.text(gem.Stat.LoreStr, STAT_COLOR);
        lore.add(lineStat);
        im.lore(lore)

        im.setCustomModelData(ModelId);

        GemAddToItemMeta(gem, im);

        DefaultStack.itemMeta = im;
    }
}