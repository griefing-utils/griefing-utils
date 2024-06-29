package griefingutils.modules;

import griefingutils.toast.NotificationToast;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket.Action;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class GamemodeNotify extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> message = sgGeneral.add(new BoolSetting.Builder()
        .name("message")
        .description("Puts a message in chat.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> notification = sgGeneral.add(new BoolSetting.Builder()
        .name("notification")
        .description("Notifies you with a toast.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> notificationImportant = sgGeneral.add(new BoolSetting.Builder()
        .name("important")
        .description("The notification will flash red and alert you.")
        .defaultValue(false)
        .build()
    );

    public GamemodeNotify() {
        super(Categories.DEFAULT, "gamemode-notify", "Alerts you when someone changes their gamemode.");
    }

    @EventHandler
    public void onPacket(PacketEvent.Receive event) {
        if (!Utils.canUpdate() || !(event.packet instanceof PlayerListS2CPacket packet)) return;

        for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
            for (Action action : packet.getActions()) {
                if (action != Action.UPDATE_GAME_MODE ||
                    packet.getPlayerAdditionEntries().contains(entry)) continue;

                GameMode gameMode = entry.gameMode();
                String player = networkHandler().getPlayerListEntry(entry.profileId()).getProfile().getName();
                if (message.get()) info("%s has switched to %s mode!".formatted(player, gameMode.getName()));
                if (notification.get()) addToastWithLimit(() -> new NotificationToast(
                    Text.of("Gamemode Notify"),
                    Text.of("%s has switched:".formatted(player)),
                    gameMode.getTranslatableName(),
                    Items.COMMAND_BLOCK,
                    notificationImportant.get()
                ));
            }
        }
    }
}
