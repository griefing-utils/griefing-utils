package griefingutils;

import com.mojang.logging.LogUtils;
import griefingutils.commands.*;
import griefingutils.modules.*;
import griefingutils.modules.creative.DoomBoom;
import griefingutils.modules.creative.ExplosiveHands;
import griefingutils.modules.creative.WitherAdvertise;
import griefingutils.modules.op.SidebarAdvertise;
import griefingutils.modules.op.WorldDeleter;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;

public class GriefingUtils extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final String MODID = "griefingutils";
    @Override
    public void onInitialize() {
        LOG.info("Initializing 0x06's Griefing Utils");
        registerModules();
        registerCommands();
    }

    private static void registerModules() {
        Modules.get().add(new AntiBlockEntityLag());
        Modules.get().add(new AntiCrash());
        Modules.get().add(new AntiItemLag());
        Modules.get().add(new AutoLavacast());
        Modules.get().add(new ContainerAction());
        Modules.get().add(new CrackedKickModule());
        Modules.get().add(new DisconnectScreenPlus());
        Modules.get().add(new DoomBoom());
        Modules.get().add(new ExplosiveHands());
        Modules.get().add(new GamemodeNotify());
        Modules.get().add(new PauseScreenPlus());
        Modules.get().add(new Privacy());
        Modules.get().add(new SidebarAdvertise());
        Modules.get().add(new SignChanger());
        Modules.get().add(new VanillaFlight());
        Modules.get().add(new WitherAdvertise());
        Modules.get().add(new WorldDeleter());
    }

    private static void registerCommands() {
        Commands.add(new CommandCompleteCrash());
        Commands.add(new ClipboardGive());
        Commands.add(new CrackedKickCommand());
        Commands.add(new Hologram());
        Commands.add(new PurpurCrash());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(Categories.DEFAULT);
    }

    @Override
    public String getPackage() {
        return "griefingutils";
    }

    private static final GithubRepo REPO;

    static {
        String id = getRepoProperty("id");
        if (id == null || id.equals("null")) {
            REPO = null;
        } else {
            String[] ownerNamePair = id.split("/");
            String owner = ownerNamePair[0];
            String name = ownerNamePair[1];
            REPO = new GithubRepo(
                owner,
                name,
                getRepoProperty("branch"),
                null
            );
        }
    }

    @Override
    public GithubRepo getRepo() {
        return REPO;
    }

    @Override
    public String getWebsite() {
        return getMetadata()
            .getContact()
            .get("homepage")
            .orElseThrow();
    }

    @Override
    public String getCommit() {
        String commit = getRepoProperty("commit");
        return commit.equals("null") ? null : commit;
    }

    private static String getRepoProperty(String key) {
        return getMetadata().getCustomValue("repo").getAsObject().get(key).getAsString();
    }

    private static ModMetadata getMetadata() {
        return FabricLoader.getInstance().getModContainer(MODID).orElseThrow().getMetadata();
    }
}
