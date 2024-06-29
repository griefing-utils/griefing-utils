package griefingutils.commands;

import griefingutils.util.MCUtil;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.MinecraftClient;

public abstract class BetterCommand extends Command implements MCUtil {
    protected final int SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;
    protected final MinecraftClient mc = MC;

    public BetterCommand(String name, String description, String... aliases) {
        super(name, description, aliases);
    }
}
