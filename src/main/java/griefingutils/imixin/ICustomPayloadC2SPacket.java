package griefingutils.imixin;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public interface ICustomPayloadC2SPacket {
    void griefing_utils$setCustomData(Identifier id, Consumer<PacketByteBuf> dataWriter);
    boolean griefing_utils$hasCustomData();
    Identifier griefing_utils$getId();
    Consumer<PacketByteBuf> griefing_utils$getDataWriter();
}
