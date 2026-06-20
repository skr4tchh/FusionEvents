package pw.fusionmine.fusionevents.commands;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import pw.fusionmine.fusionevents.FusionEvents;
import pw.fusionmine.fusionevents.utils.ColorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventCommand implements CommandExecutor, TabCompleter {

    private final FusionEvents plugin;

    public EventCommand(FusionEvents plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "delay":
                handleDelay(sender);
                break;

            case "gps":
                handleGps(sender);
                break;

            default:
                sendUsage(sender);
                break;
        }

        return true;
    }

    private void handleDelay(CommandSender sender) {
        String currentEvent = plugin.getEventManager().getCurrentEventType();

        if (currentEvent != null) {
            if (currentEvent.equals("myst") && plugin.getMystManager().hasActiveChest()) {
                List<String> messages = plugin.getConfigManager().getEventMessageList("delay-myst-active");
                Location loc = plugin.getMystManager().getChestLocation();

                for (String line : messages) {
                    String message = ColorUtils.colorize(line
                            .replace("{x}", String.valueOf(loc.getBlockX()))
                            .replace("{y}", String.valueOf(loc.getBlockY()))
                            .replace("{z}", String.valueOf(loc.getBlockZ()))
                            .replace("{world}", loc.getWorld().getName())
                            .replace("{despawn-time}", plugin.getMystManager().getTimeLeftDespawn()));
                    sender.sendMessage(message);
                }
            } else if (currentEvent.equals("bosses") && plugin.getBossManager().hasActiveBoss()) {
                List<String> messages = plugin.getConfigManager().getEventMessageList("delay-boss-active");
                ConfigurationSection bossConfig = plugin.getConfigManager().getBossesConfig()
                        .getConfigurationSection(plugin.getBossManager().getCurrentBossId());
                String bossName = bossConfig != null ?
                        ColorUtils.colorize(bossConfig.getString("display-name", plugin.getBossManager().getCurrentBossId())) :
                        plugin.getBossManager().getCurrentBossId();

                for (String line : messages) {
                    String message = ColorUtils.colorize(line
                            .replace("{name}", bossName)
                            .replace("{despawn-time}", plugin.getBossManager().getTimeLeftDespawn()));
                    sender.sendMessage(message);
                }
            } else if (currentEvent.equals("castle") && plugin.getCastleManager().hasActiveCastle()) {
                List<String> messages = plugin.getConfigManager().getEventMessageList("delay-castle-active");
                Location loc = plugin.getCastleManager().getCastleLocation();

                for (String line : messages) {
                    String message = ColorUtils.colorize(line
                            .replace("{x}", String.valueOf(loc.getBlockX()))
                            .replace("{y}", String.valueOf(loc.getBlockY()))
                            .replace("{z}", String.valueOf(loc.getBlockZ()))
                            .replace("{world}", loc.getWorld().getName())
                            .replace("{despawn-time}", plugin.getCastleManager().getTimeLeft()));
                    sender.sendMessage(message);
                }
            }
        } else {
            List<String> messages = plugin.getConfigManager().getEventMessageList("delay-waiting");

            for (String line : messages) {
                String message = ColorUtils.colorize(line
                        .replace("{time-left}", plugin.getEventManager().getTimeLeft()));
                sender.sendMessage(message);
            }
        }
    }

    private void handleGps(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorUtils.colorize("&cТолько для игроков!"));
            return;
        }

        Player player = (Player) sender;

        if (!plugin.getMystManager().hasActiveChest()) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getEventMessage("no-myst-active")));
            return;
        }

        if (!hasCompass(player)) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getEventMessage("no-compass")));
            return;
        }

        plugin.getMystManager().addGpsPlayer(player);
        sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getEventMessage("gps-activated")));
    }

    private boolean hasCompass(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.COMPASS) {
                return true;
            }
        }
        return false;
    }

    private void sendUsage(CommandSender sender) {
        List<String> usage = plugin.getConfigManager().getEventCommandUsage();
        for (String line : usage) {
            sender.sendMessage(ColorUtils.colorize(line));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subcommands = Arrays.asList("delay", "gps");
            for (String sub : subcommands) {
                if (sub.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        }

        return completions;
    }
}