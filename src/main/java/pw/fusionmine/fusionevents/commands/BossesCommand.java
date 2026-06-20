package pw.fusionmine.fusionevents.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import pw.fusionmine.fusionevents.FusionEvents;
import pw.fusionmine.fusionevents.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BossesCommand implements CommandExecutor, TabCompleter {

    private final FusionEvents plugin;

    public BossesCommand(FusionEvents plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("fusionevents.admin")) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("no-permission")));
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

                sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("config-reloaded")));
                break;

            case "spawn":
                if (args.length < 2) {
                    sendUsage(sender);
                    return true;
                }

                if (plugin.getBossManager().hasActiveBoss()) {
                    sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("boss-already-exists")));
                    return true;
                }

                String bossName = args[1];
                if (plugin.getBossManager().spawnBoss(bossName)) {
                    ConfigurationSection bossConfig = plugin.getConfigManager().getBossesConfig().getConfigurationSection(bossName);
                    String displayName = bossConfig != null ? ColorUtils.colorize(bossConfig.getString("display-name", bossName)) : bossName;
                    sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("boss-spawned").replace("{name}", displayName)));
                } else {
                    sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("boss-not-found").replace("{id}", bossName)));
                }
                break;

            case "kill":
                if (!plugin.getBossManager().hasActiveBoss()) {
                    sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("no-boss-active")));
                    return true;
                }

                plugin.getBossManager().killCurrentBoss();
                sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessage("boss-killed")));
                break;

            case "list":
                List<String> bosses = plugin.getBossManager().getBossList();
                sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getPrefix() + "&6&lСписок боссов:"));
                sender.sendMessage("");
                for (String boss : bosses) {
                    ConfigurationSection bossConfig = plugin.getConfigManager().getBossesConfig().getConfigurationSection(boss);
                    String displayName = bossConfig != null ? ColorUtils.colorize(bossConfig.getString("display-name", boss)) : boss;
                    double hp = bossConfig != null ? bossConfig.getDouble("hp", 100) : 100;
                    sender.sendMessage(ColorUtils.colorize("  &e" + boss + " &7- " + displayName + " &7(&c" + (int) hp + " ❤&7)"));
                }
                sender.sendMessage("");
                break;

            default:
                sendUsage(sender);
                break;
        }

        return true;
    }

    private void sendUsage(CommandSender sender) {
        List<String> usage = plugin.getConfigManager().getCommandUsage();
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
            List<String> subcommands = Arrays.asList("reload", "spawn", "kill", "list");
            for (String sub : subcommands) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) {
            for (String boss : plugin.getBossManager().getBossList()) {
                if (boss.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(boss);
                }
            }
        }

        return completions;
    }
}