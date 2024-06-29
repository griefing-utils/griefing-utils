package griefingutils.modules;

import com.mojang.authlib.GameProfile;
import griefingutils.GriefingUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.AntiPacketKick;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.s2c.common.CookieRequestS2CPacket;
import net.minecraft.network.packet.s2c.login.*;
import net.minecraft.text.Text;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CrackedKickModule extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> ban = sgGeneral.add(new BoolSetting.Builder()
        .name("ban")
        .description("Whether to kick joining players.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> kickFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("kick-friends")
        .description("Whether to kick friends.")
        .defaultValue(false)
        .build()
    );

    private static final HashSet<GameProfile> processingPlayers = new HashSet<>();

    public CrackedKickModule() {
        super(Categories.DEFAULT, "cracked-kick", "Kicks everyone on a cracked server.");
    }

    @Override
    public void onActivate() {
        AntiPacketKick apk = Modules.get().get(AntiPacketKick.class);
        if (apk.isActive() && apk.logExceptions.get())
            info("Disable \"Log Exceptions\" in Anti Packet Kick if you don't want to get spammed!");
    }

    @EventHandler
    private void postTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;
        if (mc.isInSingleplayer()) {
            warning("Not available in singleplayer!");
            return;
        }
        Collection<PlayerListEntry> entries = networkHandler().getPlayerList();
        for(PlayerListEntry entry: entries) {
            kick(this, entry, kickFriends.get());
        }
        if (!ban.get()) toggle();
    }


    public static void kick(CrackedKickModule module, PlayerListEntry entry, boolean kickFriends) {
        MinecraftClient mc = GriefingUtils.MC;
        GameProfile profile = entry.getProfile();

        if (profile.equals(mc.player.getGameProfile())) return;
        if (Friends.get().isFriend(entry) && !kickFriends) return;
        if (processingPlayers.contains(profile)) return;
        processingPlayers.add(profile);

        InetSocketAddress address = (InetSocketAddress) mc.getNetworkHandler().getConnection().getAddress();
        ClientConnection connection = new ClientConnection(NetworkSide.CLIENTBOUND);

        CompletableFuture.runAsync(() -> {
            try {
                ClientConnection.connect(address, mc.options.shouldUseNativeTransport(), connection).get(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | TimeoutException | ExecutionException ignored) {
                processingPlayers.remove(profile);
                connection.channel.close().awaitUninterruptibly();
                return;
            }
            connection.connect(address.getHostName(), address.getPort(), new ClientLoginPacketListener() {
                @Override
                public void onCookieRequest(CookieRequestS2CPacket packet) {}

                @Override
                public void onHello(LoginHelloS2CPacket packet) {}

                @Override
                public void onSuccess(LoginSuccessS2CPacket packet) {}

                @Override
                public void onDisconnect(LoginDisconnectS2CPacket packet) {}

                @Override
                public void onCompression(LoginCompressionS2CPacket packet) {}

                @Override
                public void onQueryRequest(LoginQueryRequestS2CPacket packet) {}

                @Override
                public void onDisconnected(Text reason) {}

                @Override
                public boolean isConnectionOpen() {
                    return connection.isOpen();
                }
            });

            connection.send(new LoginHelloC2SPacket(profile.getName(), profile.getId()));
            processingPlayers.remove(profile);
            // Server does not read packets when the connection is closed
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
            connection.channel.close().syncUninterruptibly();
        });
    }
}
