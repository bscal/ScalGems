package me.bscal.mcgems

import io.github.rysefoxx.inventory.plugin.content.IntelligentItem
import io.github.rysefoxx.inventory.plugin.content.InventoryContents
import io.github.rysefoxx.inventory.plugin.content.InventoryProvider
import io.github.rysefoxx.inventory.plugin.pagination.RyseInventory
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

private val PLACEHOLDER_STACK = ItemStack(Material.GRAY_STAINED_GLASS_PANE);
private val CONFIRM_STACK = InitConfirmStack();
private val CANCEL_STACK = InitCancelStack();

private fun InitConfirmStack(): ItemStack
{
    val stack = ItemStack(Material.EMERALD_BLOCK);
    stack.editMeta {
        val name = Component.text("Confirm Sockets", TextColor.color(0, 255, 0));
        it.displayName(name);
    }
    return stack;
}

private fun InitCancelStack(): ItemStack
{
    val stack = ItemStack(Material.REDSTONE_BLOCK);
    stack.editMeta {
        val name = Component.text("Cancel Sockets", TextColor.color(255, 0, 0));
        it.displayName(name);
    }
    return stack;
}

private fun TrySocketGemUi(gemArrayIndex: Int,
                           event: InventoryClickEvent,
                           player: Player, gemPlayer: GemPlayer,
                           itemStack: ItemStack, gemItemStack: GemItemStack)
{
    if (event.isRightClick && event.inventory != player.inventory)
    {
        event.isCancelled = true;
        val clickedItem = event.currentItem ?: return;
        if (UiItemStacks.contains(clickedItem))
        {
            val cursorItem = event.cursor ?: return;
            val gem = GemFromItemStack(cursorItem) ?: return;
            val gemStack: GemStack = NameToGemStackMap[gem.Name] ?: return;
            if (gemItemStack.Socket(player, gemPlayer, gemArrayIndex, gem, itemStack))
            {
                event.inventory.setItem(event.slot, gemStack.DefaultStack);
                player.inventory.removeItem(gemStack.DefaultStack);
                // TODO debug
                player.sendMessage("You have successfully socketed a gem!")
            }
        }
    }

}

fun CreateGemSmithUi(player: Player, gemPlayer: GemPlayer,
                     itemStack: ItemStack, gemItemStack: GemItemStack)
{
    val ui = RyseInventory.builder().title("Sockets").rows(4).provider(
            object: InventoryProvider
            {
                override fun init(player: Player?, contents: InventoryContents?)
                {
                    player!!;
                    contents!!;

                    contents.fill(PLACEHOLDER_STACK);
                    contents.set(0, 8, IntelligentItem.of(CANCEL_STACK) {
                        contents.pagination().inventory().close(player);
                    })

                    for (i in gemItemStack.GemArray.indices)
                    {
                        val stack: ItemStack = if (gemItemStack.GemArray[i] == null)
                        {
                            GemColorToPane.getOrDefault(gemItemStack.ColorArray[i],
                                    ItemStack(Material.BARRIER));
                        } else
                        {
                            NameToGemStackMap[gemItemStack.GemArray[i]?.Name]?.DefaultStack
                                    ?: ItemStack(Material.BARRIER)
                        }
                        contents.set(2, 1 + (i * 2), IntelligentItem.of(stack) {
                            TrySocketGemUi(i, it, player, gemPlayer, itemStack, gemItemStack);
                        })
                    }

                }
            }
    ).build(MCGems.Instance);

    ui.open(player)
}