package griefingutils.util;

import net.minecraft.text.Text;

public class TextConstants {
    public static final Text ACCOUNTS = Text.of("Accounts");
    public static final Text COPY_IP = Text.of("Copy IP");
    public static final Text DELETE = Text.translatable("selectServer.delete");
    public static final Text DELETE_CONFIRM_TITLE = Text.translatable("selectServer.deleteQuestion");
    public static final Text DISCONECT_AND_DELETE_CONFIRM_DESCRIPTION = Text.of("Do you really want to disconnect and delete this server?");
    public static final Text DISCONNECT_AND_DELETE = Text.of("Disconnect & Delete");
    public static final Text DISCONNECT_AND_DELETE_CONFIRM_TITLE = Text.of("Are you sure?");
    public static final Text GAME_MENU = Text.translatable("menu.game");
    public static final Text MORE = Text.translatable("createWorld.tab.more.title");
    public static final Text NOT_AVAILABLE_IN_SINGLEPLAYER = Text.of("Not available in singleplayer!");
    public static final Text RECONNECT_CONFIRM_DESCRIPTION = Text.of("Are you sure you want to reconnect with your new account?");
    public static final Text RECONNECT_CONFIRM_TITLE = Text.of("Confirm Reconnect");
    public static final TranslationSupplier DELETE_CONFIRM_DESCRIPTION = new TranslationSupplier("selectServer.deleteWarning");

    public static final class TranslationSupplier {
        private final String key;

        private TranslationSupplier(String key) {
            this.key = key;
        }

        public Text get(Object... args) {
            return Text.translatable(key, args);
        }
    }
}
