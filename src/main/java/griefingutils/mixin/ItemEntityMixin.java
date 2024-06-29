package griefingutils.mixin;

import griefingutils.modules.AntiItemLag;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin() {
        super(null, null);
    }

    @Shadow public abstract ItemStack getStack();

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onItemTick(CallbackInfo ci) {
        AntiItemLag module = Modules.get().get(AntiItemLag.class);
        if (!module.isActive() || !getWorld().isClient) return;
        if (!module.filterType.get().contains(module.items.get(), getStack().getItem()))
            ci.cancel();
    }
}
