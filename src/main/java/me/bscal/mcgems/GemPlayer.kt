package me.bscal.mcgems

import org.bukkit.entity.Player
import java.util.UUID

class GemPlayer(player: Player)
{

    var Player: Player = player;
    var Strength: Int = 0;
    var Intelligence: Int = 0;
    var Agility: Int = 0;

    fun Initialize()
    {
        for (equipment in this.Player.equipment.armorContents)
        {
            val gemSet = DeserializeItemStack(equipment);
            gemSet?.Equip(this)
        }
    }
}