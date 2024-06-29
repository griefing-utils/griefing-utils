package griefingutils.modules;

import griefingutils.util.ListMode;
import meteordevelopment.meteorclient.events.packets.InventoryEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class ContainerAction extends BetterModule {
    private final SettingGroup sgThrow = settings.createGroup("Throw");
    private final SettingGroup sgSearch = settings.createGroup("Search");
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Boolean> throwItems = sgThrow.add(new BoolSetting.Builder()
        .name("throw")
        .description("Throws items from nearby chests.")
        .defaultValue(false)
        .build()
    );

    private final Setting<ThrowDirection> throwDirection = sgThrow.add(new EnumSetting.Builder<ThrowDirection>()
        .name("throw-direction")
        .description("Lets you change the direction you throw the items at.")
        .defaultValue(ThrowDirection.DOWNWARDS)
        .visible(throwItems::get)
        .build()
    );

    private final Setting<Boolean> throwFilter = sgThrow.add(new BoolSetting.Builder()
        .name("filter")
        .description("Whether to filter the items you throw.")
        .defaultValue(false)
        .visible(throwItems::get)
        .build()
    );

    private final Setting<List<Item>> throwItemFilter = sgThrow.add(new ItemListSetting.Builder()
        .name("filter-items")
        .description("The items to filter.")
        .defaultValue(
            Items.LAVA_BUCKET,
            Items.WATER_BUCKET,
            Items.BUCKET,
            Items.SAND,
            Items.GUNPOWDER,
            Items.TNT,
            Items.FLINT_AND_STEEL,
            Items.FLINT,
            Items.IRON_INGOT,
            Items.FIRE_CHARGE
        )
        .visible(() -> throwItems.get() && throwFilter.get())
        .onChanged((value) -> invalidateContainers())
        .build()
    );

    private final Setting<ListMode> throwFilterType = sgThrow.add(new EnumSetting.Builder<ListMode>()
        .name("filter-type")
        .description("The type of the filter.")
        .defaultValue(ListMode.Blacklist)
        .visible(throwItemFilter::isVisible)
        .build()
    );

    private final Setting<Boolean> search = sgSearch.add(new BoolSetting.Builder()
        .name("search")
        .description("Search things in containers.")
        .defaultValue(false)
        .build()
    );

    private final Setting<List<Item>> searchFilter = sgSearch.add(new ItemListSetting.Builder()
        .name("filter-items")
        .description("The items to filter.")
        .defaultValue(
            Items.LAVA_BUCKET,
            Items.WATER_BUCKET,
            Items.BUCKET,
            Items.SAND,
            Items.GUNPOWDER,
            Items.TNT,
            Items.FLINT_AND_STEEL,
            Items.FLINT,
            Items.IRON_INGOT,
            Items.FIRE_CHARGE
        )
        .visible(search::get)
        .onChanged((value) -> invalidateContainers())
        .build()
    );

    private final Setting<Boolean> searchQuickMove = sgSearch.add(new BoolSetting.Builder()
        .name("quick-move")
        .description("Whether to move the found items into your inventory.")
        .defaultValue(true)
        .visible(search::get)
        .onChanged((value) -> invalidateContainers())
        .build()
    );

    private final Setting<Boolean> searchLog = sgSearch.add(new BoolSetting.Builder()
        .name("log-items")
        .description("Whether to log found items in the chat.")
        .defaultValue(false)
        .visible(search::get)
        .build()
    );

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Whether to render things.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> renderFound = sgSearch.add(new BoolSetting.Builder()
        .name("render-found")
        .description("Whether to render containers where items were found.")
        .defaultValue(false)
        .visible(() -> render.get() && search.get())
        .build()
    );

    private final Setting<Boolean> renderUnopenedContainers = sgRender.add(new BoolSetting.Builder()
        .name("render-unopened-containers")
        .description("Wheter to render unopened containers.")
        .defaultValue(true)
        .visible(render::get)
        .build()
    );

    private final Setting<Integer> renderRange = sgRender.add(new IntSetting.Builder()
        .name("render-range")
        .description("Render range for unopened containers.")
        .range(8, 128)
        .sliderRange(8, 128)
        .defaultValue(32)
        .visible(render::get)
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

    private final Setting<ShapeMode> importantShapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("important-shape-mode")
        .description("How the shapes are rendered for important containers.")
        .defaultValue(ShapeMode.Both)
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> importantSideColor = sgRender.add(new ColorSetting.Builder()
        .name("important-side-color")
        .description("The side color for important containers.")
        .defaultValue(new SettingColor(127, 255, 0, 127))
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> importantLineColor = sgRender.add(new ColorSetting.Builder()
        .name("important-line-color")
        .description("The line color for important containers.")
        .defaultValue(new SettingColor(127, 255, 0))
        .visible(render::get)
        .build()
    );

    public Deque<BlockPos> importantContainers = new ArrayDeque<>();
    public Deque<BlockPos> processedContainers = new ArrayDeque<>();
    private BlockPos processingPos = null;
    private boolean isProcessing = false;
    public int count = 0;

    public ContainerAction() {
        super(Categories.DEFAULT, "container-action", "Does stuff with nearby containers.");
    }

    @Override
    public void onDeactivate() {
        invalidateContainers();
        processingPos = null;
        isProcessing = false;
    }

    private void invalidateContainers() {
        importantContainers.clear();
        processedContainers.clear();
        count = 0;
    }

    @Override
    public String getInfoString() {
        return Integer.toString(count);
    }

    @EventHandler
    private void preTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        renderContainers();

        ClientPlayerEntity p = mc.player;
        if (p.isSneaking() ||
            p.currentScreenHandler != p.playerScreenHandler ||
            isProcessing ||
            isSpectator() ||
            mc.currentScreen != null) return;

        for(BlockEntity be : Utils.blockEntities()) {
            BlockState blockState = mc.world.getBlockState(be.getPos());
            Block block = blockState.getBlock();

            if (isNotAContainer(block) ||
                isProcessing ||
                processedContainers.contains(be.getPos()) ||
                !PlayerUtils.isWithinReach(be.getPos())) continue;

            BlockHitResult bhr = new BlockHitResult(be.getPos().toCenterPos().offset(Direction.DOWN, 0.5), Direction.DOWN, be.getPos(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
            processingPos = be.getPos();
            isProcessing = true;

            if (block instanceof ChestBlock) {
                handleChest(be.getPos(), cbe -> {
                    count++;
                    processedContainers.add(cbe.getPos());
                    renderContainer(be.getPos());
                }, (cbe1, cbe2) -> {
                    count += 2;
                    processedContainers.add(cbe1.getPos());
                    processedContainers.add(cbe2.getPos());
                    renderContainer(cbe1.getPos());
                    renderContainer(cbe2.getPos());
                });
            } else {
                count++;
                processedContainers.add(be.getPos());
                renderContainer(be.getPos());
            }
        }
    }

    public void renderContainers() {
        for(BlockEntity be : Utils.blockEntities()) {
            BlockState blockState = mc.world.getBlockState(be.getPos());
            Block block = blockState.getBlock();
            if (isNotAContainer(block)) continue;

            int rangeSquared = renderRange.get() * renderRange.get();
            if (mc.player.getEyePos().squaredDistanceTo(be.getPos().toCenterPos()) > rangeSquared) continue;

            if (renderUnopenedContainers.get() && !processedContainers.contains(be.getPos()))
                renderContainer(be.getPos(), 1, false, false);

            if (renderFound.get() && importantContainers.contains(be.getPos()))
                renderContainer(be.getPos(), 1, false, true);
        }
    }

    private boolean isNotAContainer(Block block) {
        return !(block instanceof ChestBlock ||
            block instanceof ShulkerBoxBlock ||
            block instanceof BarrelBlock ||
            block instanceof HopperBlock ||
            block instanceof DispenserBlock ||
            block instanceof FurnaceBlock ||
            block instanceof BlastFurnaceBlock ||
            block instanceof SmokerBlock ||
            block instanceof BrewingStandBlock);
    }

    @EventHandler
    private void onInventory(InventoryEvent event) {
        if (!isProcessing) return;
        Vec2f rot = throwDirection.get().getThrowAngle(mc.player);
        if (throwItems.get() && throwDirection.get() != ThrowDirection.FORWARDS)
            sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(rot.x, rot.y, mc.player.isOnGround()));
        ScreenHandler handler = mc.player.currentScreenHandler;
        int size = SlotUtils.indexToId(SlotUtils.MAIN_START);

        boolean foundItem = false;
        for (int i = 0; i < size; i++) {
            if (!handler.getSlot(i).hasStack()) continue;
            ItemStack stack = handler.getSlot(i).getStack();

            if (throwItems.get())
                if (!throwFilter.get() || throwFilterType.get().contains(throwItemFilter.get(), stack.getItem()))
                    mc.interactionManager.clickSlot(handler.syncId, i, 1, SlotActionType.THROW, mc.player);

            if (search.get()) {
                if (!searchFilter.get().contains(stack.getItem())) continue;
                if (searchLog.get())
                    foundItem = true;
                if (searchQuickMove.get()) {
                    FindItemResult result = InvUtils.findEmpty();
                    if(result.found())
                        mc.interactionManager.clickSlot(handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                }
                Block block = mc.world.getBlockState(processingPos).getBlock();
                if (block instanceof ChestBlock) {
                    handleChest(processingPos, cbe -> {
                        importantContainers.add(cbe.getPos());
                    }, (cbe1, cbe2) -> {
                        importantContainers.add(cbe1.getPos());
                        importantContainers.add(cbe2.getPos());
                    });
                } else {
                    importantContainers.add(processingPos);
                }
            }
        }

        if (foundItem && searchLog.get()) {
            info("Found mathching item(s) at %d %d %d!".formatted(
                processingPos.getX(),
                processingPos.getY(),
                processingPos.getZ()
            ));
        }

        mc.player.closeHandledScreen();
        isProcessing = false;
    }

    private void handleChest(
        BlockPos pos,
        Consumer<ChestBlockEntity> singleChestHandler,
        BiConsumer<ChestBlockEntity, ChestBlockEntity> doubleChestHandler
    ) {
        DoubleBlockProperties.PropertyRetriever<ChestBlockEntity, Void> chestHandler = new DoubleBlockProperties.PropertyRetriever<>() {
            @Override
            public Void getFromBoth(ChestBlockEntity first, ChestBlockEntity second) {
                doubleChestHandler.accept(first, second);
                return null;
            }

            @Override
            public Void getFrom(ChestBlockEntity single) {
                singleChestHandler.accept(single);
                return null;
            }

            @Override
            public Void getFallback() {
                return null;
            }
        };
        BlockState blockState = mc.world.getBlockState(pos);
        Block block = blockState.getBlock();
        if (!(block instanceof ChestBlock cb)) return;
        cb.getBlockEntitySource(blockState, mc.world, pos, true).apply(chestHandler);
    }

    private void renderContainer(BlockPos pos) {
        renderContainer(pos, 10, true, false);
    }

    private void renderContainer(BlockPos pos, int duration, boolean fade, boolean important) {
        if (!render.get()) return;
        RenderUtils.renderTickingBlock(
            pos.toImmutable(),
            important ? importantSideColor.get() : sideColor.get(),
            important ? importantLineColor.get() : lineColor.get(),
            important ? importantShapeMode.get() : shapeMode.get(),
            0,
            duration,
            fade,
            false
        );
    }

    private enum ThrowDirection {
        FORWARDS((x, y) -> new Vec2f(x, -30)),
        DOWNWARDS((yaw, pitch) -> new Vec2f(yaw, 90)),
        UPWARDS((yaw, pitch) -> new Vec2f(yaw, -90)),
        BACKWARDS((yaw, pitch) -> new Vec2f(MathHelper.wrapDegrees(yaw + 180), -30));

        private final BiFunction<Float, Float, Vec2f> rotationGetter;

        ThrowDirection(BiFunction<Float, Float, Vec2f> rotationGetter) {
            this.rotationGetter = rotationGetter;
        }

        Vec2f getThrowAngle(ClientPlayerEntity player) {
            float yaw = player.getYaw();
            float pitch = player.getPitch();
            return rotationGetter.apply(yaw, pitch);
        }
    }
}
