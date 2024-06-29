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
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class ExplosiveHands extends BetterModule {
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

    public ExplosiveHands() {
        super(Categories.DEFAULT, "explosive-hands", "Spawns explosions at the block you're looking at. (requires creative mode)");
    }

    @EventHandler
    private void postTick(TickEvent.Post event) {
        if (notCreative()) {
            warning("You're not in creative mode!");
            toggle();
            return;
        }

        if (!mc.options.attackKey.isPressed() || mc.currentScreen != null) return;
        CreativeUtils.saveHeldStack();

        HitResult hitResult = mc.cameraEntity.raycast(900, 0, false);
        if (hitResult.getType() == HitResult.Type.MISS) return;
        Vec3d pos = hitResult.getPos().offset(Direction.DOWN, 1);

        CreativeUtils.giveToSelectedSlot(entity.get().generator.get(
            Items.MOOSHROOM_SPAWN_EGG,
            pos,
            NbtByte.of(strength.get().byteValue())
        ));

        CreativeUtils.interactBlockAtEyes();
        CreativeUtils.restoreSavedHeldStack();

    }
}
