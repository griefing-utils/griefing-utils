package griefingutils.screen;

import griefingutils.GriefingUtils;
import griefingutils.modules.PauseScreenPlus;
import griefingutils.util.TextConstants;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class GameMenuExtrasScreen extends Screen {
    public final GameMenuScreen parent;

    public GameMenuExtrasScreen(GameMenuScreen parent) {
        super(TextConstants.GAME_MENU);
        this.parent = parent;
    }

    @Override
    protected void init() {
        GridWidget gW = new GridWidget();
        gW.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gW.createAdder(2);

        addWideButton(adder, ScreenTexts.BACK, button -> client.setScreen(parent), gW, true);
        addButton(adder, TextConstants.COPY_IP, button -> client.keyboard.setClipboard(client.getCurrentServerEntry().address));
        addButton(adder, TextConstants.ACCOUNTS, button -> client.setScreen(new AccountsConfirmReconnectScreen(this)));
        addWideButton(adder, TextConstants.DISCONNECT_AND_DELETE, button -> {
            PauseScreenPlus.deleteCurrentServer();
            parent.disconnect();
        }, gW, false);

        gW.refreshPositions();
        SimplePositioningWidget.setPos(gW, 0, 0, this.width, this.height, 0.5f, 0.25f);
        gW.forEachChild(this::addDrawableChild);

        this.addDrawableChild(new TextWidget(0, 40, this.width, this.textRenderer.fontHeight, this.title, this.textRenderer));
    }

    private void addButton(GridWidget.Adder adder, Text message, ButtonWidget.PressAction onPress) {
        adder.add(new ButtonWidget.Builder(message, onPress).width(98).build());
    }

    private void addWideButton(GridWidget.Adder adder, Text message, ButtonWidget.PressAction onPress, GridWidget gridWidget, boolean marginTop) {
        ButtonWidget widget = new ButtonWidget.Builder(message, onPress).width(204).build();
        if (marginTop) adder.add(widget, 2, gridWidget.copyPositioner().marginTop(50));
        else adder.add(widget, 2);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.isCopy(keyCode) && copyServerIP()) return true;
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    public static boolean copyServerIP() {
        ServerInfo serverEntry = GriefingUtils.MC.getCurrentServerEntry();
        if (serverEntry != null) {
            GriefingUtils.MC.keyboard.setClipboard(serverEntry.address);
            return true;
        }
        return false;
    }
}
