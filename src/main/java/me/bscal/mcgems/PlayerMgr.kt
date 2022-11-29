package me.bscal.mcgems

import org.bukkit.entity.Player
import java.util.UUID

// Maybe implement some caching?
val GemPlayerMap: HashMap<UUID, GemPlayer> = HashMap();

fun CreateGemPlayer(player: Player): GemPlayer
{
    val gm = GemPlayer(player);
    gm.Initialize();

    GemPlayerMap.put(player.uniqueId, gm);

    return gm;
}

fun RemoveGemPlayer(uuid: UUID)
{
    GemPlayerMap.remove(uuid);
}

// TODO kind of a dirty way of resting stats
fun RemoveAllStatsFromPlayer(uuid: UUID)
{
    val gemPlayer = GemPlayerMap.get(uuid);
    if (gemPlayer != null)
    {
        GemPlayerMap[uuid] = GemPlayer(gemPlayer.Player);
    }
}