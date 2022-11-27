package me.bscal.mcgems

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import org.bukkit.Color

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = GemStat::class)
object GemStatToString : KSerializer<GemStat>
{
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("GemStat", PrimitiveKind.STRING);

    override fun serialize(encoder: Encoder, value: GemStat)
    {
        encoder.encodeString(value.Name);
    }

    override fun deserialize(decoder: Decoder): GemStat
    {
        val value = decoder.decodeString();
        val gemStat: GemStat = NameToGemStatMap[value] ?: error("GemStat $value is not registered to McGems");
        return gemStat;
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Color::class)
object BukkitColorToMapSerializer : KSerializer<Color>
{
    private val delegateSerializer = MapSerializer(String.serializer(), Int.serializer());
    override val descriptor: SerialDescriptor = SerialDescriptor("Color", delegateSerializer.descriptor);

    override fun serialize(encoder: Encoder, value: Color)
    {
        val serializedValue = value.serialize() as Map<String, Int>;
        encoder.encodeSerializableValue(delegateSerializer, serializedValue);
    }

    override fun deserialize(decoder: Decoder): Color
    {
        val value = decoder.decodeSerializableValue(delegateSerializer);
        return Color.deserialize(value);
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = Array<Color>::class)
object BukkitColorArrayToMapSerializer : KSerializer<Array<Color>>
{
    private val delegateSerializer = ArraySerializer(BukkitColorToMapSerializer);
    override val descriptor: SerialDescriptor = SerialDescriptor("ColorArray", delegateSerializer.descriptor);

    override fun serialize(encoder: Encoder, value: Array<Color>)
    {
        val c = encoder.beginStructure(descriptor);
        c.encodeIntElement(descriptor, 0, value.size);
        for (i in value.indices)
        {
            c.encodeSerializableElement(descriptor, i + 1, BukkitColorToMapSerializer, value[i]);
        }
        c.endStructure(descriptor);
    }

    override fun deserialize(decoder: Decoder): Array<Color>
    {
        return decoder.decodeStructure(descriptor)
        {
            val result: Array<Color> = Array(this.decodeIntElement(descriptor, 0))
            {
                this.decodeSerializableElement(descriptor, it + 1, BukkitColorToMapSerializer)
            }
            result;
        }
    }
}