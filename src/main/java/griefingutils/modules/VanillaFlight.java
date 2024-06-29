package griefingutils.modules;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayerEntityAccessor;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class VanillaFlight extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> hSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal-speed")
        .description("Horizontal speed in blocks per second.")
        .defaultValue(110)
        .range(0, 300)
        .sliderRange(0, 300)
        .build()
    );

    private final Setting<Double> vSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical-speed")
        .description("Vertical speed in blocks per second.")
        .defaultValue(110)
        .range(0, 110)
        .sliderRange(0, 110)
        .build()
    );

    private final Setting<Double> normalSpeedMultiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("normal-speed-multiplier")
        .description("The multiplier of your speed when you're not sprinting.")
        .defaultValue(0.5)
        .range(0, 1)
        .sliderRange(0, 1)
        .build()
    );

    public VanillaFlight() {
        super(Categories.DEFAULT, "vanilla-flight", "Flight with smart anti-rubberband & packet-based anti-kick.");
    }

    @EventHandler
    private void postTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;

        // packet-based smart anti-kick
        if (mc.world.getTime() % 10 == 0 &&
            !playerCollides(mc.player.getPos().offset(Direction.DOWN, 0.03126), true, false))
        {
            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 0.03126, mc.player.getZ(), false));
            ((ClientPlayerEntityAccessor) mc.player).setTicksSinceLastPositionPacketSent(19);
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        double speedMul = mc.options.sprintKey.isPressed() ? 1 : normalSpeedMultiplier.get();

        boolean isMovingDown = mc.options.sneakKey.isPressed();
        boolean isMovingUp = mc.options.jumpKey.isPressed();
        double vSpeed = isMovingUp ? Math.min(this.vSpeed.get() * speedMul, 75) : this.vSpeed.get() * speedMul;
        double hSpeed;

        if (isMovingDown) hSpeed = 0;
        else if (isMovingUp) hSpeed = Math.min(this.hSpeed.get() * speedMul, 75);
        else hSpeed = this.hSpeed.get() * speedMul;

        double velY = 0;
        if (mc.options.jumpKey.isPressed()) velY += (vSpeed + 0.001) / 20;
        if (isMovingDown) velY -= (vSpeed + 0.001) / 20;

        Vec3d vel = PlayerUtils.getHorizontalVelocity(hSpeed - 0.001).add(0, velY, 0);
        ((IVec3d)mc.player.getVelocity()).set(vel.x, vel.y, vel.z);
    }
}

