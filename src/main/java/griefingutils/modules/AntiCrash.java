package griefingutils.modules;

import griefingutils.toast.NotificationToast;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.passive.SnifferEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.text.Text;

import static net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket.*;

public class AntiCrash extends BetterModule {
    public static boolean mixinFailing = false;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> cancelDemoScreen = sgGeneral.add(new BoolSetting.Builder()
        .name("cancel-demo-screen")
        .description("Cancels demo related packets that should never be sent.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> message = sgGeneral.add(new BoolSetting.Builder()
        .name("message")
        .description("Puts a message in chat.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> notification = sgGeneral.add(new BoolSetting.Builder()
        .name("notification")
        .description("Notifies you with a toast.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> notificationImportant = sgGeneral.add(new BoolSetting.Builder()
        .name("important")
        .description("The notification will flash red and alert you.")
        .defaultValue(false)
        .build()
    );

    public AntiCrash() {
        super(Categories.DEFAULT, "anti-crash", "Cancels packets that may freeze your game.");
    }

    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onPacketReceive(PacketEvent.Receive e) {
        switch (e.packet) {
            case ExplosionS2CPacket p -> { if (isInvalid(p)) cancel(e, "invalid explosion"); }
            case ParticleS2CPacket p -> { if (isInvalid(p)) cancel(e, "invalid particles"); }
            case PlayerPositionLookS2CPacket p -> { if (isInvalid(p)) cancel(e, "invalid movement"); }
            case EntityVelocityUpdateS2CPacket p -> { if (isInvalid(p)) cancel(e, "invalid velocity update"); }
            case InventoryS2CPacket p -> { if (isInvalid(p)) cancel(e, "invalid inventory"); }
            case ScreenHandlerSlotUpdateS2CPacket p -> { if (isInvalid(p)) cancel(e, "invalid slot update"); }
            case WorldBorderInitializeS2CPacket p -> { if (isInvalid(p)) cancel(e, "invalid world border"); }
            case WorldBorderInterpolateSizeS2CPacket p -> { if (isInvalid(p)) cancel(e, "invalid world border"); }
            case WorldBorderSizeChangedS2CPacket p -> { if (isInvalid(p)) cancel(e, "invalid world border"); }
            case EntityStatusS2CPacket p -> { if (isInvalid(p)) cancel(e, "invalid entity status"); }
            case EntityAnimationS2CPacket p -> { if (isInvalid(p)) cancel(e, "invalid animation"); }
            case GameStateChangeS2CPacket p -> { if (cancelDemoScreen.get() && isInvalid(p)) cancel(e, "demo packet"); }
            default -> {}
        }
    }

    private static boolean isInvalid(ExplosionS2CPacket p) {
        return p.getX() > 30_000_000 ||
            p.getY() > 30_000_000 ||
            p.getZ() > 30_000_000 ||
            p.getX() < -30_000_000 ||
            p.getY() < -30_000_000 ||
            p.getZ() < -30_000_000 ||
            p.getRadius() > 1000 ||
            p.getAffectedBlocks().size() > 1_000_000 ||
            p.getPlayerVelocityX() > 100_000 ||
            p.getPlayerVelocityY() > 100_000 ||
            p.getPlayerVelocityZ() > 100_000 ||
            p.getPlayerVelocityX() < -100_000 ||
            p.getPlayerVelocityY() < -100_000 ||
            p.getPlayerVelocityZ() < -100_000;
    }

    private static boolean isInvalid(ParticleS2CPacket p) {
        return p.getCount() > 100_000 ||
            p.getSpeed() > 100_000;
    }

    private static boolean isInvalid(PlayerPositionLookS2CPacket p) {
        return p.getX() > 30_000_000 ||
            p.getY() > 30_000_000 ||
            p.getZ() > 30_000_000 ||
            p.getX() < -30_000_000 ||
            p.getY() < -30_000_000 ||
            p.getZ() < -30_000_000;
    }

    private static boolean isInvalid(EntityVelocityUpdateS2CPacket p) {
        return p.getVelocityX() > 30_000_000 ||
            p.getVelocityY() > 30_000_000 ||
            p.getVelocityZ() > 30_000_000 ||
            p.getVelocityX() < -30_000_000 ||
            p.getVelocityY() < -30_000_000 ||
            p.getVelocityZ() < -30_000_000;
    }

    private boolean isInvalid(InventoryS2CPacket packet) {
        if (mc.player == null) return true;
        if (packet.getSyncId() == 0) {
            return packet.getContents().size() > mc.player.playerScreenHandler.slots.size();
        } else
            return mc.player.currentScreenHandler == null ||
                packet.getContents().size() > mc.player.currentScreenHandler.slots.size() + mc.player.playerScreenHandler.slots.size();
    }

    private boolean isInvalid(ScreenHandlerSlotUpdateS2CPacket packet) {
        if (mc.player == null) return true;
        if (packet.getSyncId() == 0) {
            return packet.getSlot() > mc.player.playerScreenHandler.slots.size();
        } else
            return mc.player.currentScreenHandler == null ||
                packet.getSlot() > mc.player.currentScreenHandler.slots.size() + mc.player.playerScreenHandler.slots.size();
    }

    private static boolean isInvalid(GameStateChangeS2CPacket packet) {
        return packet.getReason() == GameStateChangeS2CPacket.DEMO_MESSAGE_SHOWN;
    }

    private static boolean isInvalid(WorldBorderInitializeS2CPacket packet) {
        return sizeLerpTargetInvalid(packet.getSizeLerpTarget());
    }

    private static boolean isInvalid(WorldBorderInterpolateSizeS2CPacket packet) {
        return sizeLerpTargetInvalid(packet.getSizeLerpTarget());
    }

    private static boolean isInvalid(WorldBorderSizeChangedS2CPacket packet) {
        return sizeLerpTargetInvalid(packet.getSizeLerpTarget());
    }

    private boolean isInvalid(ItemPickupAnimationS2CPacket packet) {
        return !(mc.world.getEntityById(packet.getCollectorEntityId()) instanceof LivingEntity);
    }

    private static boolean sizeLerpTargetInvalid(double value) {
        return value < 0 || value > 60_000_000;
    }

    public static boolean isInvalid(BundleS2CPacket packet) {
        for (Packet<? super ClientPlayPacketListener> p : packet.getPackets()) {
            if (!(p instanceof BundleS2CPacket)) continue;
            return true;
        }
        return false;
    }

    private boolean isInvalid(EntityStatusS2CPacket packet) {
        if (packet.getStatus() == EntityStatuses.PLAY_GUARDIAN_ATTACK_SOUND &&
            !(packet.getEntity(mc.world) instanceof GuardianEntity)) return true;

        if (packet.getStatus() == EntityStatuses.START_DIGGING &&
            !(packet.getEntity(mc.world) instanceof SnifferEntity)) return true;

        return false;
    }

    private boolean isInvalid(EntityAnimationS2CPacket packet) {
        Entity entity = mc.world.getEntityById(packet.getId());
        if (entity == null) return false; // vanilla already checks this
        return switch (packet.getAnimationId()) {
            case SWING_MAIN_HAND, SWING_OFF_HAND -> !(entity instanceof LivingEntity);
            case WAKE_UP -> !(entity instanceof PlayerEntity);
            default -> false;
        };
    }

    private void cancel(PacketEvent.Receive event, String reason) {
        alert(reason);
        event.cancel();
    }

    public void alert(String reason) {
        if (message.get()) info("Received a bad packet: " + reason);
        if (notification.get()) addToastWithLimit(() -> new NotificationToast(
            Text.of("Anti Crash"),
            Text.of("Received a bad packet:"),
            Text.of(reason),
            Items.BARRIER,
            notificationImportant.get()
        ));
    }
}
