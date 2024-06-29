package griefingutils.screen;

import griefingutils.GriefingUtils;
import griefingutils.util.TextConstants;
import meteordevelopment.meteorclient.gui.GuiThemes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import java.net.InetSocketAddress;

public class AccountsConfirmReconnectScreen extends BetterConfirmScreen {
    private boolean openedAccountsScreen = false;

    public AccountsConfirmReconnectScreen(GameMenuExtrasScreen parent) {
        super(
            parent,
            () -> AccountsConfirmReconnectScreen.reconnect(parent),
            TextConstants.RECONNECT_CONFIRM_TITLE,
            TextConstants.RECONNECT_CONFIRM_DESCRIPTION
        );
    }

    @Override
    protected void init() {
        super.init();
        if (!openedAccountsScreen) {
            client.setScreen(GuiThemes.get().accountsScreen());
            openedAccountsScreen = true;
        }
    }

    private static void reconnect(GameMenuExtrasScreen parent) {
        MinecraftClient client = GriefingUtils.MC;
        InetSocketAddress inetAddress = (InetSocketAddress) client.getNetworkHandler().getConnection().getAddress();
        ServerAddress address = new ServerAddress(inetAddress.getHostName(), inetAddress.getPort());

        ServerInfo server = client.getCurrentServerEntry();

        parent.parent.disconnect();
        ConnectScreen.connect(client.currentScreen, client, address, server, false, null);
    }
}
