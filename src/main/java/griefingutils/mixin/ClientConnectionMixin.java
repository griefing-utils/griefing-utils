package griefingutils.mixin;

import griefingutils.GriefingUtils;
import griefingutils.modules.AntiCrash;
import griefingutils.toast.NotificationToast;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BundleS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientConnection.class, priority = Integer.MIN_VALUE)
public class ClientConnectionMixin {
    @Inject(method = "handlePacket", at = @At("HEAD"), cancellable = true)
    private static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener, CallbackInfo ci) {
        if (packet instanceof BundleS2CPacket p) {
            AntiCrash ac = Modules.get().get(AntiCrash.class);
            if (AntiCrash.mixinFailing) return;
            if (ac == null) {
                AntiCrash.mixinFailing = true;
                GriefingUtils.MC.getToastManager().add(new NotificationToast(
                   Text.of("AntiCrash - error"),
                   Text.of("Some parts of AntiCrash"),
                    Text.of("might not work"),
                    Items.BARRIER,
                    false
                ));
                return;
            };
            if (!ac.isActive()) return;
            if (AntiCrash.isInvalid(p)) {
                ac.alert("invalid bundle");
                ci.cancel();
            }
        }
    }
}
