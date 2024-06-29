package griefingutils.screen;

import griefingutils.GriefingUtils;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class BetterConfirmScreen extends ConfirmScreen {
    public final Screen parent;

    public BetterConfirmScreen(Screen parent, Runnable confirmCallback, Text title, Text message) {
        super(
            confirmed -> {
                if (confirmed) confirmCallback.run();
                else GriefingUtils.MC.setScreen(parent);
            }, title, message, ScreenTexts.YES, ScreenTexts.NO
        );
        this.parent = parent;
    }
}
