package griefingutils.modules;

import griefingutils.util.ListMode;
import meteordevelopment.meteorclient.events.render.RenderItemEntityEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;

import java.util.List;

public class AntiItemLag extends BetterModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("filter-items")
        .description("The items to filter.")
        .build()
    );

    public final Setting<ListMode> filterType = sgGeneral.add(new EnumSetting.Builder<ListMode>()
        .name("filter-type")
        .description("The type of the filter.")
        .defaultValue(ListMode.Blacklist)
        .build()
    );

    public AntiItemLag() {
        super(Categories.DEFAULT, "anti-item-lag", "Disables the ticking, only renders the shadow of the specified items.");
    }

    @EventHandler
    public void onRender(RenderItemEntityEvent event) {
        Item item = event.itemEntity.getStack().getItem();
        if (!filterType.get().contains(items.get(), item))
            event.cancel();
    }
}
