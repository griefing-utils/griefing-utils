package griefingutils.util;

import griefingutils.GriefingUtils;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class CreativeUtils {
    private static ItemStack savedHeldStack = null;

    public static void saveHeldStack() {
        ClientPlayerEntity player = GriefingUtils.MC.player;
        int selectedSlot = player.getInventory().selectedSlot;
        savedHeldStack = player.getInventory().getStack(selectedSlot);
    }

    public static void restoreSavedHeldStack() {
        if (savedHeldStack == null) throw new NullPointerException("savedHeldStack is null (forgot to save beforehand?)");
        giveToSelectedSlot(savedHeldStack);
        savedHeldStack = null;
    }

    public static void giveToEmptySlot(ItemStack stack) {
        ClientPlayerEntity player = GriefingUtils.MC.player;
        ClientPlayerInteractionManager interactionManager = GriefingUtils.MC.interactionManager;
        int selectedSlot = player.getInventory().selectedSlot;

        if (player.getMainHandStack().isEmpty())
            interactionManager.clickCreativeStack(stack, 36 + selectedSlot);
        else {
            int nextEmptySlot = player.getInventory().getEmptySlot();
            if (nextEmptySlot < 9) interactionManager.clickCreativeStack(stack, 36 + nextEmptySlot);
            else
                interactionManager.clickCreativeStack(stack, 36 + selectedSlot);
        }
    }

    public static void giveToSelectedSlot(ItemStack stack) {
        ClientPlayerInteractionManager interactionManager = GriefingUtils.MC.interactionManager;
        int selectedSlot = GriefingUtils.MC.player.getInventory().selectedSlot;

        interactionManager.clickCreativeStack(stack, 36 + selectedSlot);
    }

    public static void interactBlockAtEyes() {
        ClientPlayerEntity player = GriefingUtils.MC.player;
        ClientPlayerInteractionManager interactionManager = GriefingUtils.MC.interactionManager;

        Vec3d eyePos = player.getPos();
        BlockPos blockPos = new BlockPos(
            MathHelper.floor(eyePos.x),
            MathHelper.floor(eyePos.y),
            MathHelper.floor(eyePos.z)
        );
        BlockHitResult bhr = new BlockHitResult(
            eyePos,
            Direction.UP,
            blockPos,
            false
        );
        interactionManager.interactBlock(player, Hand.MAIN_HAND, bhr);
    }
}
