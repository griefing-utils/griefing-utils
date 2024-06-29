package griefingutils.modules;

import griefingutils.util.MiscUtil;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.Script;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Privacy extends BetterModule {
    public SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> hideIPs = sgGeneral.add(new BoolSetting.Builder()
        .name("Hide IPs")
        .description("Tries to hide IPv4 Addresses and ports")
        .defaultValue(false)
        .build()
    );

    public final Setting<String> ipReplacement = sgGeneral.add(new StringSetting.Builder()
        .name("IP replacement")
        .description("The string the IPs and ports will be replaced to.")
        .defaultValue("<IPv4 address>")
        .visible(hideIPs::get)
        .wide()
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    public final Setting<Boolean> hideMOTDs = sgGeneral.add(new BoolSetting.Builder()
        .name("Hide MOTDs")
        .description("Hides the message of the day from servers.")
        .defaultValue(false)
        .build()
    );

    public final Setting<String> motdReplacement = sgGeneral.add(new StringSetting.Builder()
        .name("MOTD Replacement")
        .description("The string the MOTDs will be replaced to.")
        .defaultValue("<MOTD>")
        .visible(hideMOTDs::get)
        .wide()
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    public Privacy() {
        super(Categories.DEFAULT, "privacy", "Hides sensitive information.");
    }

    public String transform(String s) {
        if (!isActive()) return s;
        if (hideIPs.get()) s = censorIPs(s);
        return s;
    }

    // Modified version of first comment from https://stackoverflow.com/q/31178400
    private final Pattern IPv4Pattern = Pattern.compile("(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}(2[0-4][0-9]|25[0-5]|1[0-9]{2}|[1-9][0-9]|[0-9])(:[0-9]{1,5})?");
    public String censorIPs(String s) {
        // Fast checks that are faster than a RegEx
        if (s.length() < 7) return s;
        if (s.indexOf('.') == -1) return s;
        if (!(s.indexOf('0') != -1 || s.indexOf('1') != -1 || s.indexOf('2') != -1)) return s;

        Matcher matcher = IPv4Pattern.matcher(s);
        return matcher.replaceAll(getIpReplacement());
    }

    public Text transformMOTD(Text original, boolean isOnline) {
        if (!isActive() || !hideMOTDs.get()) return original;
        if (!isOnline) return original;
        return Text.of(getMOTDReplacement());
    }

    @Nullable
    private String getIpReplacement() {
        Script compiledTitle = MiscUtil.compileSilently(ipReplacement.get());
        if (compiledTitle == null) return "<IP replacement is malformed!>";
        return MeteorStarscript.run(compiledTitle);
    }

    private String getMOTDReplacement() {
        Script compiledTitle = MiscUtil.compileSilently(motdReplacement.get());
        if (compiledTitle == null) return "<MOTD replacement is malformed!>";
        return MeteorStarscript.run(compiledTitle);
    }
}
