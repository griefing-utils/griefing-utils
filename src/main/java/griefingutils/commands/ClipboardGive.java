package griefingutils.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import griefingutils.util.CreativeUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStringReader;

public class ClipboardGive extends BetterCommand {
    public ClipboardGive() {
        super("clipboard-give", "Gives an item from a copied give command. (requires creative mode)", "clip-give", "cgive", "give-clip");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(this::run);
    }

    private int run(CommandContext<CommandSource> ctx) {
        try {
            String clipboard = mc.keyboard.getClipboard();
            if (!clipboard.startsWith("/give")) {
                warning("Clipboard content is not a give command");
                return SUCCESS;
            }
            String cmdArgumentsWithoutSelector = clipboard.substring(clipboard.indexOf(' ', 6) + 1);

            StringReader reader = new StringReader(cmdArgumentsWithoutSelector);

            ItemStringReader.ItemResult result = new ItemStringReader(REGISTRY_ACCESS).consume(reader);
            ItemStackArgument stackArgument = new ItemStackArgument(result.item(), result.components());

            CreativeUtils.giveToEmptySlot(stackArgument.createStack(1, false));

            return SUCCESS;
        } catch (Exception e) {
            warning(e.getMessage());
            return SUCCESS;
        }
    }
}
