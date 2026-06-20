package pw.fusionmine.fusionevents.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import pw.fusionmine.fusionevents.FusionEvents;
import pw.fusionmine.fusionevents.utils.ColorUtils;

public class CastleListener implements Listener {

    private final FusionEvents plugin;

    public CastleListener(FusionEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShulkerBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.SHULKER_BOX) return;
        if (!plugin.getCastleManager().hasActiveCastle()) return;

        Location loc = block.getLocation();
        Player player = event.getPlayer();
        if (!plugin.getCastleManager().isShulkerBox(loc)) return;

        if (!player.hasPermission("fusionevents.castle.break")) {
            event.setCancelled(true);
            player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getCastlePrefix() + "&cВы не можете ломать шалкеры!"));
            return;
        }

        event.setCancelled(true);
        plugin.getCastleManager().handleShulkerBreak(loc, player);
    }

    @EventHandler
    public void onAdminInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        if (!title.startsWith(ColorUtils.colorize("&4&lРедактор замка: &c"))) return;
        String rarity = title.replace(ColorUtils.colorize("&4&lРедактор замка: &c"), "");
        plugin.getCastleManager().saveRarityItems(rarity, event.getInventory());
        if (event.getPlayer() instanceof Player) {
            ((Player) event.getPlayer()).sendMessage(ColorUtils.colorize(
                    plugin.getConfigManager().getCastlePrefix() + "&aПредметы для редкости &e" + rarity + " &aсохранены!"));
        }
    }
}