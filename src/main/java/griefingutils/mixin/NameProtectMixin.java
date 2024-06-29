package griefingutils.mixin;

import griefingutils.modules.Privacy;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.NameProtect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(NameProtect.class)
public class NameProtectMixin {
    @ModifyVariable(method = "replaceName", at = @At("HEAD"), ordinal = 0, argsOnly = true, remap = false)
    private String privacy(String original) {
        return Modules.get().get(Privacy.class).transform(original);
    }
}
