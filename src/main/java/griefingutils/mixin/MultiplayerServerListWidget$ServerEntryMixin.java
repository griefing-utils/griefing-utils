package griefingutils.mixin;

import griefingutils.modules.Privacy;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiplayerServerListWidget.ServerEntry.class)
public abstract class MultiplayerServerListWidget$ServerEntryMixin {
    @Shadow @Final private ServerInfo server;

    @Inject(method = "render", at = @At("HEAD"))
    private void overwriteMOTDGet(CallbackInfo ci) {
        Privacy privacy = Modules.get().get(Privacy.class);
        Text original = server.label;
        if (privacy.isActive()) {
            server.label = privacy.transformMOTD(original, server.getStatus() != ServerInfo.Status.UNREACHABLE);
        } else server.label = original;
    }
}
