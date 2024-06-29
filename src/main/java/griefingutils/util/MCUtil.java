package griefingutils.util;

import griefingutils.imixin.ICustomPayloadC2SPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.toast.Toast;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.UnknownCustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface MCUtil {
    MinecraftClient MC = MinecraftClient.getInstance();
    
    default ClientPlayNetworkHandler networkHandler() {
        return MC.getNetworkHandler();
    }

    default void sendPacket(Packet<?> packet) {
        networkHandler().sendPacket(packet);
    }

    default void sendCustomPayload(Identifier id, Consumer<PacketByteBuf> dataWriter) {
        CustomPayloadC2SPacket packet = new CustomPayloadC2SPacket(new UnknownCustomPayload(id));
        ICustomPayloadC2SPacket packet2 = (ICustomPayloadC2SPacket) (Object) packet;
        packet2.griefing_utils$setCustomData(id, dataWriter);
        sendPacket(packet);
    }

    default boolean notCreative() {
        return !MC.player.isCreative();
    }

    default boolean isSpectator() {
        return MC.player.isSpectator();
    }

    default boolean notOp() {
        return !MC.player.hasPermissionLevel(4);
    }

    default boolean playerCollides(Vec3d pos, boolean blocks, boolean entities) {
        return playerCollides(pos.x, pos.y, pos.z, pos.x, pos.y, pos.z, blocks, entities);
    }

    default boolean playerCollides(double fromX, double fromY, double fromZ, double toX, double toY, double toZ, boolean blocks, boolean entities) {
        ClientPlayerEntity player = MC.player;
        EntityDimensions dimensions = player.getDimensions(player.getPose());
        Box box = dimensions.getBoxAt(fromX, fromY, fromZ).union(dimensions.getBoxAt(toX, toY, toZ));
        if (blocks && entities)
            return player.getWorld().getCollisions(null, box).iterator().hasNext();
        else if (blocks)
            return player.getWorld().getBlockCollisions(null, box).iterator().hasNext();
        else if (entities)
            return !player.getWorld().getEntityCollisions(null, box).isEmpty();
        else
            return false;
    }

    default void sendCommand(String command) {
        networkHandler().sendCommand(command);
    }

    default void addToastWithLimit(Supplier<Toast> getter) {
        if (MC.getToastManager().toastQueue.size() >= 5) return;
        MC.getToastManager().add(getter.get());
    }
}
