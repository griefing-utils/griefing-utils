package griefingutils.mixin;

import griefingutils.imixin.ICustomPayloadC2SPacket;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(CustomPayloadC2SPacket.class)
public class CustomPayloadC2SPacketMixin implements ICustomPayloadC2SPacket {
    @Unique
    boolean griefing_utils$hasCustomData = false;

    @Unique
    Identifier griefing_utils$id = null;

    @Unique
    Consumer<PacketByteBuf> griefing_utils$dataWriter = null;

    @Mutable
    @Shadow @Final public static PacketCodec<PacketByteBuf, CustomPayloadC2SPacket> CODEC;

    static {
        var ORIGINAL_CODEC = CODEC;
        CODEC = PacketCodec.of((packet, buf) -> {
            ICustomPayloadC2SPacket packet2 = (ICustomPayloadC2SPacket) (Object) packet;
            if (packet2.griefing_utils$hasCustomData()) {
                buf.writeIdentifier(packet2.griefing_utils$getId());
                packet2.griefing_utils$getDataWriter().accept(buf);
            } else ORIGINAL_CODEC.encode(buf, packet);
        }, ORIGINAL_CODEC);
    }

    @Inject(method = "apply(Lnet/minecraft/network/listener/ServerCommonPacketListener;)V", at = @At("HEAD"), cancellable = true)
    private void apply(ServerCommonPacketListener serverCommonPacketListener, CallbackInfo ci) {
        if (griefing_utils$hasCustomData) ci.cancel();
    }

    @Override
    public void griefing_utils$setCustomData(Identifier id, Consumer<PacketByteBuf> dataWriter) {
        griefing_utils$hasCustomData = true;
        this.griefing_utils$id = id;
        this.griefing_utils$dataWriter = dataWriter;
    }

    @Override
    public boolean griefing_utils$hasCustomData() {
        return griefing_utils$hasCustomData;
    }

    @Override
    public Identifier griefing_utils$getId() {
        return griefing_utils$id;
    }

    @Override
    public Consumer<PacketByteBuf> griefing_utils$getDataWriter() {
        return griefing_utils$dataWriter;
    }
}
