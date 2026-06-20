package pw.fusionmine.fusionevents.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import pw.fusionmine.fusionevents.FusionEvents;
import pw.fusionmine.fusionevents.utils.ColorUtils;

public class MystListener implements Listener {

    private final FusionEvents plugin;

    public MystListener(FusionEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChestInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CHEST) return;

        if (!plugin.getMystManager().hasActiveChest()) return;

        Block chestBlock = plugin.getMystManager().getChestBlock();
        if (chestBlock == null || !block.equals(chestBlock)) return;

        event.setCancelled(true);

        if (!plugin.getMystManager().isOpened()) {
            if (!plugin.getMystManager().isOpening()) {
                plugin.getMystManager().startOpeningTimer(event.getPlayer());
            } else {
                event.getPlayer().sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMystMessage("chest-already-opening")));
            }
        } else {
            plugin.getMystManager().openMystInventory(event.getPlayer());
        }
    }

    @EventHandler
    public void onMystInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Inventory inv = event.getInventory();

        if (!plugin.getMystManager().isMystInventory(inv)) return;

        int slot = event.getRawSlot();
        if (slot < 0 || slot >= inv.getSize()) return;

        event.setCancelled(true);

        if (plugin.getMystManager().isObfuscatedSlot(slot)) {
            ItemStack realItem = plugin.getMystManager().revealItem(slot);
            if (realItem != null) {
                Player player = (Player) event.getWhoClicked();
                inv.setItem(slot, null);

                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(realItem);
                } else {
                    player.getWorld().dropItemNaturally(player.getLocation(), realItem);
                }
            }
        }
    }

    @EventHandler
    public void onChestBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Block chestBlock = plugin.getMystManager().getChestBlock();

        if (chestBlock != null && block.equals(chestBlock)) {
            if (!event.getPlayer().hasPermission("fusionevents.myst.admin")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMystMessage("cannot-break-chest")));
            } else {
                plugin.getMystManager().removeChest();
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getMystManager().removeGpsPlayer(event.getPlayer());
    }

    @EventHandler
    public void onAdminInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();

        if (!title.startsWith(ColorUtils.colorize("&5&lРедактор: &d"))) return;

        String rarity = title.replace(ColorUtils.colorize("&5&lРедактор: &d"), "");
        plugin.getMystManager().saveRarityItems(rarity, event.getInventory());

        if (event.getPlayer() instanceof Player) {
            ((Player) event.getPlayer()).sendMessage(ColorUtils.colorize(
                    plugin.getConfigManager().getMystPrefix() + "&aПредметы для редкости &e" + rarity + " &aсохранены!"));
        }
    }
}