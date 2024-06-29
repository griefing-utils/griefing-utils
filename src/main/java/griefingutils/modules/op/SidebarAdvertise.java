package griefingutils.modules.op;

import griefingutils.modules.BetterModule;
import griefingutils.modules.Categories;
import griefingutils.util.MiscUtil;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.starscript.Script;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class SidebarAdvertise extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> title = sgGeneral.add(new StringSetting.Builder()
        .name("title")
        .description("The title of the scoreboard.")
        .defaultValue("0x06's Griefing Utils")
        .wide()
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    private final Setting<SettingColor> titleColor = sgGeneral.add(new ColorSetting.Builder()
        .name("title-color")
        .description("The color of the title. (alpha is ignored)")
        .defaultValue(new Color(255, 150, 50))
        .build()
    );

    private final Setting<List<String>> lines = sgGeneral.add(new StringListSetting.Builder()
        .name("lines")
        .description("The lines (content) of the scoreboard.")
        .defaultValue(
            "Griefed by {player}!",
            "{date}"
        )
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    private final Setting<SettingColor> linesColor = sgGeneral.add(new ColorSetting.Builder()
        .name("lines-color")
        .description("The color of the lines. (alpha is ignored)")
        .defaultValue(new Color(255, 75, 0))
        .build()
    );

    public SidebarAdvertise() {
        super(Categories.DEFAULT, "sidebar-advertise", "Creates a scoreboard with some content. (requires OP)");
    }

    @Override
    public void onActivate() {
        if (notOp()) {
            warning("You don't have OP");
            toggle();
            return;
        }

        String title = parseTitle();
        if (title == null) return;

        List<String> lines = parseLines();
        if (lines == null) return;

        String scoreboardID = RandomStringUtils.randomAlphanumeric(6).toLowerCase(Locale.ROOT);
        if (!sendCommandChecked(
            "scoreboard objectives add %s dummy {\"text\":\"%s\",\"color\":\"%s\"}"
                .formatted(scoreboardID, title, MiscUtil.hexifyColor(titleColor.get())),
            s -> "Title is too long! Reduce it by %d characters."
                .formatted(s.length() - 255)
        )) return;
        sendCommand("scoreboard objectives setdisplay sidebar %s".formatted(scoreboardID));
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineNum = i + 1;
            int inverseNum = lines.size() - i;

            String teamID = RandomStringUtils.randomAlphanumeric(6).toLowerCase(Locale.ROOT);
            sendCommand("team add %s".formatted(teamID));
            if (!sendCommandChecked(
                "team modify %s suffix {\"text\":\" %s\",\"color\":\"%s\"}"
                    .formatted(teamID, line, MiscUtil.hexifyColor(linesColor.get())),
                s -> "Content line #%d is too long! Reduce it by %d characters."
                    .formatted(lineNum, s.length() - 255)
            )) return;
            sendCommand("team modify %s color %s".formatted(teamID, "red"));
            sendCommand("team join %s %d".formatted(teamID, inverseNum));
            sendCommand("scoreboard players set %d %s %d"
                .formatted(inverseNum, scoreboardID, inverseNum));
        }
        toggle();
    }

    @Nullable
    private String parseTitle() {
        Script compiledTitle = MeteorStarscript.compile(title.get());
        if (compiledTitle == null) {
            warning("Title is malformed!");
            toggle();
            return null;
        }
        return MeteorStarscript.run(compiledTitle);
    }

    @Nullable
    private List<String> parseLines() {
        List<String> uncompiledLines = this.lines.get();
        List<String> lines = new ArrayList<>(uncompiledLines.size());
        for (int i = 0; i < uncompiledLines.size(); i++) {
            String str = uncompiledLines.get(i);
            Script script = MeteorStarscript.compile(str);
            if (script == null) {
                warning("Content line #%d is malformed!".formatted(i + 1));
                toggle();
                return null;
            }
            lines.add(MeteorStarscript.run(script));
        }
        return lines;
    }

    public boolean sendCommandChecked(String command, Function<String, String> tooLongMessageGenerator) {
        if (command.length() >= 256) {
            String tooLongMessage = tooLongMessageGenerator.apply(command);
            warning(tooLongMessage);
            return false;
        }
        sendCommand(command);
        return true;
    }
}