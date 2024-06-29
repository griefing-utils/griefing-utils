package griefingutils.mixin;

import griefingutils.modules.DisconnectScreenPlus;
import griefingutils.modules.PauseScreenPlus;
import griefingutils.screen.BetterConfirmScreen;
import griefingutils.util.TextConstants;
import it.unimi.dsi.fastutil.Pair;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AutoReconnect;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DisconnectedScreen.class)
public class DisconnectedScreenMixin extends Screen {
    protected DisconnectedScreenMixin() {
        super(null);
    }

    @Shadow @Final private DirectionalLayoutWidget grid;

    @Shadow @Final private Screen parent;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;refreshPositions()V"))
    private void beforeRefreshPositions(CallbackInfo ci) {
        if (!Modules.get().isActive(DisconnectScreenPlus.class)) return;
        Pair<ServerAddress, ServerInfo> lastServerConn = Modules.get().get(AutoReconnect.class).lastServerConnection;

        if (lastServerConn == null) return;

        grid.add(ButtonWidget.builder(TextConstants.DELETE, btn -> {
            if (!(this.parent instanceof MultiplayerScreen)) return;
            client.setScreen(new BetterConfirmScreen(
                this.parent,
                () -> {
                    PauseScreenPlus.deleteServer(lastServerConn.right());
                    ((MultiplayerScreen) parent).refresh();
                },
                TextConstants.DELETE_CONFIRM_TITLE,
                TextConstants.DELETE_CONFIRM_DESCRIPTION.get(lastServerConn.right().name)
            ));
        }).build());
    }
}
