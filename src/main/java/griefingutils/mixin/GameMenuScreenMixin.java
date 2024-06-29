package griefingutils.mixin;

import griefingutils.GriefingUtils;
import griefingutils.modules.PauseScreenPlus;
import griefingutils.screen.BetterConfirmScreen;
import griefingutils.screen.GameMenuExtrasScreen;
import griefingutils.util.TextConstants;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.function.Supplier;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {
    protected GameMenuScreenMixin() {
        super(null);
    }

    @Shadow protected abstract ButtonWidget createButton(Text text, Supplier<Screen> screenSupplier);

    @Shadow public abstract void tick();

    @Shadow public abstract void disconnect();

    @Redirect(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;)Lnet/minecraft/client/gui/widget/Widget;", ordinal = 2))
    private Widget replaceSendFeedbackBtn(GridWidget.Adder instance, Widget sendFeedbackBtn) {
        if (Modules.get().isActive(PauseScreenPlus.class)) {
            ButtonWidget buttonWidget = createButton(TextConstants.MORE, () -> new GameMenuExtrasScreen((GameMenuScreen) (Object) this));
            if (GriefingUtils.MC.isInSingleplayer()) {
                buttonWidget.active = false;
                buttonWidget.setTooltip(Tooltip.of(TextConstants.NOT_AVAILABLE_IN_SINGLEPLAYER));
            }
            return instance.add(buttonWidget);
        } else return instance.add(sendFeedbackBtn);
    }

    @Inject(method = "initWidgets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/GridWidget;refreshPositions()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void beforeGridWidgetRefreshPositions(CallbackInfo ci, GridWidget gridWidget, GridWidget.Adder adder, Text text) {
        PauseScreenPlus bps = Modules.get().get(PauseScreenPlus.class);
        if (!bps.isActive() || !bps.disconnectAndDeleteButton.get()) return;
        ButtonWidget button = adder.add(ButtonWidget.builder(TextConstants.DISCONNECT_AND_DELETE, btn -> {
            GriefingUtils.MC.setScreen(new BetterConfirmScreen(this, () -> {
                PauseScreenPlus.deleteCurrentServer();
                disconnect();
            }, TextConstants.DISCONNECT_AND_DELETE_CONFIRM_TITLE, TextConstants.DISCONECT_AND_DELETE_CONFIRM_DESCRIPTION));
        }).width(204).build(), 2);
        if (GriefingUtils.MC.isInSingleplayer()) {
            button.active = false;
            button.setTooltip(Tooltip.of(TextConstants.NOT_AVAILABLE_IN_SINGLEPLAYER));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.isCopy(keyCode) && Modules.get().isActive(PauseScreenPlus.class) && GameMenuExtrasScreen.copyServerIP()) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
