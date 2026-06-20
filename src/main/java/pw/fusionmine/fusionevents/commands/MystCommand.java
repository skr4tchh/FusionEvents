package pw.fusionmine.fusionevents.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import pw.fusionmine.fusionevents.FusionEvents;
import pw.fusionmine.fusionevents.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MystCommand implements CommandExecutor, TabCompleter {

    private final FusionEvents plugin;

    public MystCommand(FusionEvents plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("fusionevents.admin")) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMystMessage("no-permission")));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.getEventManager().stopAutoSpawn();
                plugin.getBossManager().stopTasks();
                plugin.getMystManager().stopTasks();
                plugin.getConfigManager().reloadConfigs();
                plugin.getBossManager().startAuraTasks();
                plugin.getMystManager().startGpsTasks();
                plugin.getEventManager().startAutoSpawn();

                sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMystMessage("config-reloaded")));
                break;

            case "spawn":
                if (plugin.getMystManager().hasActiveChest()) {
                    sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMystMessage("chest-already-exists")));
                    return true;
                }

                plugin.getMystManager().spawnChestAsync();
                sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMystMessage("chest-spawning")));
                break;

            case "remove":
                if (!plugin.getMystManager().hasActiveChest()) {
                    sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMystMessage("no-chest-active")));
                    return true;
                }

                plugin.getMystManager().removeChest();
                sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMystMessage("chest-removed")));
                break;

            case "admin":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ColorUtils.colorize("&cТолько для игроков!"));
                    return true;
                }

                if (args.length < 3 || !args[1].equalsIgnoreCase("inventory")) {
                    sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMystPrefix() + "&cИспользование: /myst admin inventory <редкость>"));
                    return true;
                }

                String rarity = args[2].toLowerCase();
                List<String> rarities = plugin.getMystManager().getRarityList();

                if (!rarities.contains(rarity)) {
                    sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMystMessage("rarity-not-found").replace("{rarity}", rarity)));
                    return true;
                }

                Player admin = (Player) sender;
                Inventory inv = plugin.getMystManager().loadRarityItems(rarity);
                admin.openInventory(inv);
                sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMystPrefix() + "&aОткрыт редактор для редкости: &e" + rarity));
                break;

            default:
                sendUsage(sender);
                break;
        }

        return true;
    }

    private void sendUsage(CommandSender sender) {
        List<String> usage = plugin.getConfigManager().getMystCommandUsage();
        for (String line : usage) {
            sender.sendMessage(ColorUtils.colorize(line));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("fusionevents.admin")) {
            return completions;
        }

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("reload", "spawn", "remove", "admin");
            for (String sub : subcommands) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
            if ("inventory".startsWith(args[1].toLowerCase())) {
                completions.add("inventory");
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("admin") && args[1].equalsIgnoreCase("inventory")) {
            for (String rarity : plugin.getMystManager().getRarityList()) {
                if (rarity.toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(rarity);
                }
            }
        }

        return completions;
    }
}