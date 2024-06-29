package griefingutils.modules.creative;

import griefingutils.modules.BetterModule;
import griefingutils.modules.Categories;
import griefingutils.util.CreativeUtils;
import griefingutils.util.MiscUtil;
import griefingutils.util.entity.EggGenerator;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.starscript.Script;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

public class WitherAdvertise extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> name = sgGeneral.add(new StringSetting.Builder()
        .name("name")
        .description("Their names.")
        .defaultValue("")
        .wide()
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    private final Setting<SettingColor> color = sgGeneral.add(new ColorSetting.Builder()
        .name("color")
        .description("The color of their names.")
        .defaultValue(new Color(255, 75, 0))
        .build()
    );

    private final Setting<Integer> amount = sgGeneral.add(new IntSetting.Builder()
        .name("amount")
        .description("The amount of withers you want to spawn.")
        .defaultValue(10)
        .range(1, 100)
        .sliderRange(1, 100)
        .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("How far away to spawn the withers.")
        .defaultValue(50)
        .range(1, 200)
        .sliderRange(1, 200)
        .build()
    );

    public WitherAdvertise() {
        super(Categories.DEFAULT, "wither-advertise", "Spawns withers nearby with a name.");
    }

    @EventHandler
    private void postTick(TickEvent.Post event) {
        if (notCreative()) {
            warning("You don't have creative");
            toggle();
            return;
        }
        if (parseName() == null) return;
        CreativeUtils.saveHeldStack();

        for (int i = 0; i < amount.get(); i++) {
            Vec3d pos = getRandomPos();
            if (pos == null) continue;
            String customName = "{\"text\":\"%s\",\"color\":\"%s\"}".formatted(parseName(), MiscUtil.hexifyColor(color.get()));
            CreativeUtils.giveToSelectedSlot(EggGenerator.WITHER.get(
                Items.WITHER_SPAWN_EGG,
                pos,
                NbtString.of(customName)
            ));
            CreativeUtils.interactBlockAtEyes();
        }

        CreativeUtils.restoreSavedHeldStack();
        toggle();
    }

    @Nullable
    private Vec3d getRandomPos() {
        double x = mc.player.getX() + range.get() - range.get() * 2 * mc.player.getRandom().nextFloat();
        double z = mc.player.getZ() + range.get() - range.get() * 2 * mc.player.getRandom().nextFloat();
        int sx = ChunkSectionPos.getSectionCoord(x);
        int sz = ChunkSectionPos.getSectionCoord(z);
        if (!mc.world.getChunkManager().isChunkLoaded(sx, sz)) return null;
        double y = mc.world.getTopY(Heightmap.Type.WORLD_SURFACE, MathHelper.floor(x), MathHelper.floor(z)) + 20;
        return new Vec3d(x, y, z);
    }

    @Nullable
    private String parseName() {
        Script compiledName = MeteorStarscript.compile(name.get());
        if (compiledName == null) {
            warning("Name is malformed!");
            toggle();
            return null;
        }
        return MeteorStarscript.run(compiledName);
    }
}
