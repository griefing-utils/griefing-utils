package griefingutils.modules.op;

import griefingutils.modules.BetterModule;
import griefingutils.modules.Categories;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class WorldDeleter extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("The delay between fill commands in ticks.")
        .defaultValue(1)
        .range(1, 100)
        .sliderRange(1, 100)
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

    private final Deque<ChunkPos> chunksToProcess = new ArrayDeque<>();
    private ChunkPos currentChunk = null;
    private int chunkToProcessStartSize = 0;

    public WorldDeleter() {
        super(Categories.DEFAULT, "world-deleter", "Deletes loaded chunks around you but needs some time to finish. (requires OP)");
    }

    @Override
    public void onActivate() {
        if (!Utils.canUpdate()) return;
        AtomicReferenceArray<WorldChunk> chunks =  mc.world.getChunkManager().chunks.chunks;
        for (int i = 0; i < chunks.length(); i++) {
            WorldChunk chunk = chunks.get(i);
            if (chunk == null) continue;
            ChunkPos chunkPos = chunk.getPos();

            // Neighbor check
            if (!mc.world.getChunkManager().isChunkLoaded(chunkPos.x + 1, chunkPos.z)) continue;
            if (!mc.world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z + 1)) continue;
            if (!mc.world.getChunkManager().isChunkLoaded(chunkPos.x - 1, chunkPos.z)) continue;
            if (!mc.world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z - 1)) continue;
            chunksToProcess.add(chunk.getPos());
        }
        chunkToProcessStartSize = chunksToProcess.size();
    }

    @Override
    public void onDeactivate() {
        chunksToProcess.clear();
    }

    @Override
    public String getInfoString() {
        if (!Utils.canUpdate()) return null;
        return "%.1f%%".formatted(MathHelper.getLerpProgress(
            (float) chunksToProcess.size(),
            (float) chunkToProcessStartSize,
            0f
        ) * 100);
    }

    @EventHandler
    private void postTick(TickEvent.Post event) {
        if (!Utils.canUpdate() || mc.world.getTime() % delay.get() != 0) return;
        if (notOp()) {
            warning("You don't have OP");
            toggle();
            return;
        }
        if (chunksToProcess.isEmpty()) {
            info("Finished deleting!");
            toggle();
            return;
        }

        ChunkPos chunkPos = chunksToProcess.pop();
        currentChunk = chunkPos;

        for (int i = mc.world.getBottomY(); i < mc.world.getHeight() + mc.world.getBottomY() - 1; i += 100) {
            sendCommand("fill %d %d %d %d %d %d air".formatted(
                chunkPos.getStartX() - 1,
                i,
                chunkPos.getStartZ() - 1,
                chunkPos.getEndX() + 1,
                MathHelper.clamp(i + 100, mc.world.getBottomY(), mc.world.getHeight() + mc.world.getBottomY() - 1),
                chunkPos.getEndZ() + 1
            ));
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        ((IVec3d) mc.player.getVelocity()).set(0, 0, 0);
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        if (currentChunk == null) return;
        event.renderer.box(
            currentChunk.getStartX(),
            mc.world.getBottomY(),
            currentChunk.getStartZ(),
            currentChunk.getEndX(),
            mc.world.getHeight() + mc.world.getBottomY() - 1,
            currentChunk.getEndZ(),
            sideColor.get(),
            lineColor.get(),
            shapeMode.get(),
            0
        );
    }
}
