package griefingutils.toast;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class NotificationToast implements Toast {
    private static final Identifier TEXTURE = new Identifier("toast/advancement");
    private final Text title, line1, line2;
    private final ItemStack icon;
    private final boolean isImportant;
    private int remainingDings = 3;
    private long nextDingTime;

    public NotificationToast(Text title, Text line1, @Nullable Text line2, Item icon, boolean isImportant) {
        this.title = title;
        this.line1 = line1;
        this.line2 = line2 == null ? Text.empty() : line2;
        this.icon = icon.getDefaultStack();
        this.isImportant = isImportant;
    }

    @Override
    public Visibility draw(DrawContext ctx, ToastManager manager, long startTime) {
        TextRenderer textRenderer = manager.getClient().textRenderer;
        if (isImportant) {
            if (startTime % 1000 < 500)
                RenderSystem.setShaderColor(3.0f, 0.5f, 0.5f, 1.0f);
        } else {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
        ctx.drawGuiTexture(TEXTURE, 0, 0, this.getWidth(), this.getHeight());
        if (isImportant) RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int height = this.getHeight() / 2 - textRenderer.fontHeight / 2;
        if (startTime < 1500L) {
            int a = MathHelper.floor(MathHelper.clamp((float)(1500L - startTime) / 300.0f, 0.0f, 1.0f) * 255.0f) << 24 | 0x4000000;
            ctx.drawText(textRenderer, title, 30, height, 0xffffff | a, true);
        } else {
            int a = MathHelper.floor(MathHelper.clamp((float)(startTime - 1500L) / 300.0f, 0.0f, 1.0f) * 252.0f) << 24 | 0x4000000;
            if(!Objects.equals(line2, Text.empty())) {
                ctx.drawText(textRenderer, line1, 30, height - (int) (textRenderer.fontHeight * .5f) - 1, 0xFFFFFF | a, false);
                ctx.drawText(textRenderer, line2, 30, height + (int) (textRenderer.fontHeight * .5f) + 1, 0xFFFFFF | a, false);
            } else {
                ctx.drawText(textRenderer, line1, 30, height, 0xFFFFFF | a, false);
            }
        }

        ctx.drawItem(icon, 8, 8);
        ctx.fill(3, 26, 157, 29, 0xff000000);
        ctx.fill(4, 27, getProgressBarX(startTime, 7000), 28, 0xff00bb00);

        if(nextDingTime < startTime && remainingDings > 0 && isImportant) {
            manager.getClient().getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), 1f, 1f));
            remainingDings--;
            nextDingTime = startTime + 1000L;
        }
        return startTime >= 7000L ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    public static int getProgressBarX(long startTime, long maxTime) {
        if(startTime == 0) return 4;
        float delta = MathHelper.clamp((float) startTime / maxTime, 0f, 1f);
        return (int) (152 * delta) + 4;
    }
}
