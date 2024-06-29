package griefingutils.modules;

import griefingutils.util.ListMode;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.EntityPose;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.*;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class AutoLavacast extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<List<Block>> blocksFilter = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("The blocks to filter.")
        .build()
    );

    private final Setting<ListMode> blocksFilterType = sgGeneral.add(new EnumSetting.Builder<ListMode>()
        .name("blocks-filter")
        .description("The type of the filter.")
        .defaultValue(ListMode.Blacklist)
        .build()
    );

    private final Setting<Boolean> renderSwing = sgGeneral.add(new BoolSetting.Builder()
        .name("swing")
        .description("Renders your client-side swing.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> autoSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-switch")
        .description("Automatically swaps to a block before placing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> enablePlace = sgGeneral.add(new BoolSetting.Builder()
        .name("enable-place")
        .description("Places a block below you when enabled.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> enablePlaceTeleport = sgGeneral.add(new BoolSetting.Builder()
        .name("enable-place-teleport")
        .description("Teleports you to the block placed by Enable Place.")
        .defaultValue(true)
        .visible(enablePlace::get)
        .build()
    );

    private final Setting<Boolean> fastMode = sgGeneral.add(new BoolSetting.Builder()
        .name("fast-mode")
        .description("Whether to place more blocks per tick.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> ticksPerBlock = sgGeneral.add(new IntSetting.Builder()
        .name("ticks-per-block")
        .description("How many ticks to wait between block places.")
        .defaultValue(1)
        .range(1, 10)
        .sliderRange(1, 10)
        .visible(() -> !fastMode.get())
        .build()
    );

    private final Setting<Integer> blocksPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("blocks-per-tick")
        .description("How many blocks to place per tick.")
        .defaultValue(1)
        .range(1, 5)
        .sliderRange(1, 5)
        .visible(fastMode::get)
        .build()
    );

    public final Setting<InputType> inputType = sgGeneral.add(new EnumSetting.Builder<InputType>()
        .name("input-type")
        .description("The type of the input for placing blocks.")
        .defaultValue(InputType.RightClick)
        .build()
    );

    private final Setting<Keybind> keybind = sgGeneral.add(new KeybindSetting.Builder()
        .name("keybind")
        .description("The keybind to place blocks.")
        .defaultValue(Keybind.fromKey(GLFW.GLFW_KEY_SPACE))
        .visible(() -> inputType.get() == InputType.KeyBind)
        .build()
    );

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Whether to render things.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color.")
        .defaultValue(new SettingColor(255, 75, 0, 127))
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color.")
        .defaultValue(new SettingColor(255, 75, 0))
        .visible(render::get)
        .build()
    );

    private final Setting<Boolean> dontLerp = sgRender.add(new BoolSetting.Builder()
        .name("don't-lerp")
        .description("Disable the lerping that smoothes your position.")
        .defaultValue(true)
        .build()
    );

    public AutoLavacast() {
        super(Categories.DEFAULT, "auto-lavacast", "Lava casting made easy.");
    }

    @Override
    public void onActivate() {
        if (!Utils.canUpdate() || !enablePlace.get()) return;

        ((IVec3d)mc.player.getVelocity()).set(0, 0, 0);

        BlockPos pos = mc.player.getBlockPos().down();
        place(pos);
        if (enablePlaceTeleport.get()) mc.player.setPosition(pos.toCenterPos().offset(Direction.UP, 0.5));
    }

    @EventHandler
    private void postTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;
        boolean goingDown = mc.player.getPitch() > 30;
        List<BlockPos> blockPoses = getBlockPoses(fastMode.get() ? blocksPerTick.get() : 1, goingDown);

        for (BlockPos pos: blockPoses)
            renderPos(pos, 1, false);

        if (!inputting()) return;

        if (!fastMode.get() && mc.world.getTime() % ticksPerBlock.get() != 0) return;

        for (BlockPos pos: blockPoses) {
            FindItemResult fir = InvUtils.findInHotbar(itemStack -> validItem(itemStack, pos));
            if(!(fir.isHotbar() || fir.isOffhand())) break;
            place(pos);
            if (!move(pos, goingDown)) break;
        }
    }

    @EventHandler
    private void onMove(PlayerMoveEvent event) {
        if (!Utils.canUpdate() || !inputting()) return;
        ((IVec3d) mc.player.getVelocity()).set(0, 0, 0);
    }

    private boolean isRightClickPressed = false;
    public boolean inputting() {
        if (inputType.get() == InputType.KeyBind)
            return keybind.get().isPressed() && mc.currentScreen == null;
        else
            return isRightClickPressed && mc.currentScreen == null;
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            isRightClickPressed = event.action == KeyAction.Press;
    }

    private List<BlockPos> getBlockPoses(int amount, boolean goingDown) {
        List<BlockPos> blockPoses = new ArrayList<>(amount);
        Direction facingDir = mc.player.getHorizontalFacing();
        for (int i = 1; i < amount + 1; i++) {
            Vec3i offsetVector = new Vec3i(facingDir.getOffsetX() * i, goingDown ? (-1 - i): -1 + i, facingDir.getOffsetZ() * i);
            BlockPos pos = mc.player.getBlockPos().add(offsetVector);
            if (mc.world.isOutOfHeightLimit(pos.getY() + 3)) break;
            blockPoses.add(pos);
        }
        return blockPoses;
    }

    private boolean move(BlockPos pos, boolean goingDown) {
        Vec3d newPos = pos.toCenterPos().add(0, 0.5, 0);
        Vec3d firstPacketPos = new Vec3d(goingDown ? newPos.x : mc.player.getX(),
            goingDown ? mc.player.getY() : newPos.y,
            goingDown ? newPos.z : mc.player.getZ());
        if (isCollidingWithSomething(firstPacketPos) || isCollidingWithSomething(newPos)) return false;
        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(firstPacketPos.x, firstPacketPos.y, firstPacketPos.z, false));
        sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(newPos.x, newPos.y, newPos.z, true));
        mc.player.setPosition(newPos.x, newPos.y, newPos.z);
        if (dontLerp.get()) mc.player.resetPosition();
        return true;
    }

    private boolean isCollidingWithSomething(Vec3d pos) {
        Box boundingBox = mc.player.getDimensions(EntityPose.STANDING).getBoxAt(pos);
        return mc.world.getBlockCollisions(null, boundingBox).iterator().hasNext();
    }

    private boolean place(BlockPos bp) {
        FindItemResult item = InvUtils.findInHotbar(itemStack -> validItem(itemStack, bp));
        if (!item.found()) return false;

        if (item.getHand() == null && !autoSwitch.get()) return false;

        if (BlockUtils.place(bp, item, false, -1, renderSwing.get(), true, false)) {
            // Render block if was placed
            if (render.get())
                renderPos(bp.toImmutable(), 8, true);
            return true;
        }
        return false;
    }

    private boolean validItem(ItemStack itemStack, BlockPos pos) {
        if (!(itemStack.getItem() instanceof BlockItem)) return false;

        Block block = ((BlockItem) itemStack.getItem()).getBlock();

        if (!blocksFilterType.get().contains(blocksFilter.get(), block)) return false;

        if (!Block.isShapeFullCube(block.getDefaultState().getCollisionShape(mc.world, pos))) return false;
        return !(block instanceof FallingBlock) || !FallingBlock.canFallThrough(mc.world.getBlockState(pos));
    }

    private void renderPos(BlockPos pos, int duration, boolean fade) {
        RenderUtils.renderTickingBlock(pos.toImmutable(), sideColor.get(), lineColor.get(), shapeMode.get(), 0, duration, fade, false);
    }

    public enum InputType {
        KeyBind,
        RightClick
    }
}
