package griefingutils.modules.creative;

import griefingutils.modules.BetterModule;
import griefingutils.modules.Categories;
import griefingutils.util.CreativeUtils;
import griefingutils.util.entity.ExplosiveEntity;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtByte;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

public class DoomBoom extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<ExplosiveEntity> entity = sgGeneral.add(new EnumSetting.Builder<ExplosiveEntity>()
        .name("entity")
        .description("The entity to spawn.")
        .defaultValue(ExplosiveEntity.CREEPER)
        .build()
    );

    private final Setting<Integer> strength = sgGeneral.add(new IntSetting.Builder()
        .name("strength")
        .description("The strength of the explosion.")
        .defaultValue(10)
        .range(1, 127)
        .sliderRange(1, 50)
        .visible(() -> entity.get().hasCustomExplosionSize)
        .build()
    );

    private final Setting<Integer> rate = sgGeneral.add(new IntSetting.Builder()
        .name("rate")
        .description("How much things to spawn per tick.")
        .defaultValue(1)
        .range(1, 100)
        .sliderRange(1, 100)
        .build()
    );

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("range")
        .description("How far away to spawn things.")
        .defaultValue(100)
        .range(1, 200)
        .sliderRange(1, 200)
        .build()
    );

    public DoomBoom() {
        super(Categories.DEFAULT, "doom-boom", "Obliterates nearby terrain. (requires creative mode)");
    }

    @EventHandler
    private void postTick(TickEvent.Post event) {
        if (notCreative()) {
            warning("You're not in creative mode!");
            toggle();
            return;
        }
        CreativeUtils.saveHeldStack();

        for (int i = 0; i < rate.get(); i++) {
            Vec3d pos = getRandomPos();
            if (pos == null) continue;
            CreativeUtils.giveToSelectedSlot(entity.get().generator.get(
                Items.MOOSHROOM_SPAWN_EGG,
                pos,
                NbtByte.of(strength.get().byteValue())
            ));
            CreativeUtils.interactBlockAtEyes();
        }

        CreativeUtils.restoreSavedHeldStack();
    }

    @Nullable
    private Vec3d getRandomPos() {
        double x = mc.player.getX() + range.get() - range.get() * 2 * mc.player.getRandom().nextFloat();
        double z = mc.player.getZ() + range.get() - range.get() * 2 * mc.player.getRandom().nextFloat();
        int sx = ChunkSectionPos.getSectionCoord(x);
        int sz = ChunkSectionPos.getSectionCoord(z);
        if (!mc.world.getChunkManager().isChunkLoaded(sx, sz)) return null;
        double y = mc.world.getTopY(Heightmap.Type.WORLD_SURFACE, MathHelper.floor(x), MathHelper.floor(z)) - 1;
        return new Vec3d(x, y, z);
    }
}
