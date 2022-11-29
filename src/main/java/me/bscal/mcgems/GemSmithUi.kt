package me.bscal.mcgems

import net.axay.kspigot.chat.literalText
import net.axay.kspigot.gui.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.entity.Player
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

private fun TrySocketGemUi(slot: SingleInventorySlot<ForRowFour>,
                           guiClickEvent: GUIClickEvent<ForInventoryFourByNine>,
                           player: Player, gemPlayer: GemPlayer,
                           itemStack: ItemStack, gemItemStack: GemItemStack)
{
    guiClickEvent.bukkitEvent.isCancelled = true;
    if (guiClickEvent.bukkitEvent.isRightClick)
    {
        val clickedItem = guiClickEvent.bukkitEvent.currentItem ?: return;
        if (UiItemStacks.contains(clickedItem))
        {
            player.sendMessage("trying to socket!")
            guiClickEvent.guiInstance[slot] = ItemStack(Material.GOLD_BLOCK);
        }
    }
}

fun CreateGemSmithUi(player: Player, gemPlayer: GemPlayer,
                     itemStack: ItemStack, gemItemStack: GemItemStack)
{
    val ui = kSpigotGUI(GUIType.FOUR_BY_NINE)
    {
        title = literalText("Gem Sockets")

        page(1)
        {
            placeholder(Slots.RowOneSlotOne rectTo Slots.RowFourSlotNine, PLACEHOLDER_STACK);

            button(Slots.RowOneSlotSix, CANCEL_STACK)
            {
                it.guiInstance.gui.closeGUI();
            }

            button(Slots.RowOneSlotFour, CONFIRM_STACK)
            {
                it.guiInstance.gui.closeGUI();
            }

            val column = 1;
            for (i in gemItemStack.GemArray.indices)
            {
                val slot = SingleInventorySlot<ForRowFour>(3, column + 2)
                val slotStack = GemColorToPane.getOrDefault(gemItemStack.ColorArray[i],
                        ItemStack(Material.OBSIDIAN));
                button(slot, slotStack) {
                    TrySocketGemUi(slot, it, player, gemPlayer, itemStack, gemItemStack);
                }
            }
        }
    }

    player.openGUI(ui);
}