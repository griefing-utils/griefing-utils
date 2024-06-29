package griefingutils.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import griefingutils.util.CreativeUtils;
import griefingutils.util.MiscUtil;
import griefingutils.util.entity.EggGenerator;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class Hologram extends BetterCommand {
    private CompletableFuture<BufferedImage> imageLoader = null;
    private String lastImagePath = null;

    public Hologram() {
        super("hologram", "Loads an image into the world. (requires creative mode)", "holo");
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes((ctx) -> execute(false))
            .then(
                literal("last")
                    .executes((ctx) -> execute(true))
            );
    }

    private int execute(boolean shouldGetLast) {
        if (notCreative()) {
            warning("You're not in creative mode!");
            return SUCCESS;
        }
        if (shouldGetLast && lastImagePath == null) {
            warning("There is no last image!");
            return SUCCESS;
        }
        if (imageLoader != null && !imageLoader.isDone()) {
            warning("An image is already loading!");
            return SUCCESS;
        }

        imageLoader = CompletableFuture.supplyAsync(() -> tryGetImage(shouldGetLast));

        return SUCCESS;
    }

    @EventHandler
    private void postTick(TickEvent.Post event) {
        if (imageLoader != null && imageLoader.isDone()) {
            BufferedImage image = imageLoader.getNow(null);
            if (image == null) return;
            onImageLoad(image);
        }
    }

    private void onImageLoad(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        CreativeUtils.saveHeldStack();

        Vec3d playerPos = mc.player.getPos();
        for (int y = 0; y < height; y++) {
            Color[] colorRow = new Color[width];
            for (int x = 0; x < width; x++) {
                colorRow[x] = new Color(image.getRGB(x, y));
            }
            Vec3d pos = playerPos.offset(Direction.UP, (height - y) * 0.23);
            String customName = getCustomName(colorRow);
            CreativeUtils.giveToSelectedSlot(EggGenerator.ARMOR_STAND.get(
                Items.COD_SPAWN_EGG,
                pos,
                NbtString.of(customName)
            ));
            CreativeUtils.interactBlockAtEyes();
        }

        CreativeUtils.restoreSavedHeldStack();
        imageLoader = null;
    }

    private String getCustomName(Color[] colorRow) {
        JsonArray customName = new JsonArray();
        for (Color color : colorRow) {
            JsonObject object = new JsonObject();
            object.addProperty("color", MiscUtil.hexifyColor(color));
            object.addProperty("text", "█");
            customName.add(object);
        }
        return customName.toString();
    }

    @Nullable
    private BufferedImage tryGetImage(boolean shouldGetLast) {
        if (shouldGetLast)
            return tryGetImage(lastImagePath);

        PointerBuffer filter = BufferUtils.createPointerBuffer(1)
            .put(MemoryUtil.memASCII("*jpg;*jpeg;*.png;*.bmp;*.gif")).rewind();

        String imagePath = TinyFileDialogs.tinyfd_openFileDialog(
            "Select Image",
            null,
            filter,
            null,
            false
        );

        lastImagePath = imagePath;

        return tryGetImage(imagePath);
    }

    @Nullable
    private BufferedImage tryGetImage(String path) {
        if (path == null) return null;

        File file = new File(path);
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            warning("Could not load the image: {}", e);
            return null;
        }
        info("Original Resolution: %dx%d", image.getWidth(), image.getHeight());
        if (image.getWidth() * image.getHeight() > 128 * 128) {
            warning("Image is too big, scaling down to 128x128");
            image = scaleImage(image, 128, 128);
        }
        return image;
    }

    private static BufferedImage scaleImage(BufferedImage image, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, image.getType());
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(image, 0, 0, width, height, 0, 0, image.getWidth(), image.getHeight(), null);
        g.dispose();
        return resized;
    }
}
