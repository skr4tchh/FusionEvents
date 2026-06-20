package pw.fusionmine.fusionevents.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pw.fusionmine.fusionevents.FusionEvents;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EventManager {

    private final FusionEvents plugin;
    private BukkitTask autoSpawnTask;
    private long nextSpawnTime;
    private String currentEventType;

    public EventManager(FusionEvents plugin) {
        this.plugin = plugin;
        this.currentEventType = null;
    }

    public void startAutoSpawn() {
        FileConfiguration config = plugin.getConfigManager().getEventConfig();
        if (!config.getBoolean("auto-spawn.enabled", true)) return;

        long interval = config.getLong("auto-spawn.interval", 3600) * 20L;
        nextSpawnTime = System.currentTimeMillis() + (interval / 20 * 1000);

        autoSpawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                spawnRandomEvent();
                nextSpawnTime = System.currentTimeMillis() + (interval / 20 * 1000);
            }
        }.runTaskTimer(plugin, interval, interval);
    }

    public void stopAutoSpawn() {
        if (autoSpawnTask != null) {
            autoSpawnTask.cancel();
            autoSpawnTask = null;
        }
    }

    public void spawnRandomEvent() {
        if (hasActiveEvent()) return;

        FileConfiguration config = plugin.getConfigManager().getEventConfig();
        if (Bukkit.getOnlinePlayers().size() < config.getInt("min-online-to-spawn", 1)) return;

        List<String> enabledEvents = new ArrayList<>();

        if (config.getBoolean("events.bosses.enabled", true)) enabledEvents.add("bosses");
        if (config.getBoolean("events.myst.enabled", true)) enabledEvents.add("myst");
        if (config.getBoolean("events.castle.enabled", true)) enabledEvents.add("castle");

        if (enabledEvents.isEmpty()) return;

        String selected = enabledEvents.get(ThreadLocalRandom.current().nextInt(enabledEvents.size()));

        switch (selected) {
            case "bosses":
                List<String> bosses = plugin.getBossManager().getBossList();
                if (!bosses.isEmpty()) {
                    if (plugin.getBossManager().spawnBoss(bosses.get(ThreadLocalRandom.current().nextInt(bosses.size())))) {
                        currentEventType = "bosses";
                    }
                }
                break;
            case "myst":
                plugin.getMystManager().spawnChestAsync();
                currentEventType = "myst";
                break;
            case "castle":
                plugin.getCastleManager().spawnCastle();
                currentEventType = "castle";
                break;
        }
    }

    public boolean hasActiveEvent() {
        return plugin.getBossManager().hasActiveBoss() ||
                plugin.getMystManager().hasActiveChest() ||
                plugin.getCastleManager().hasActiveCastle();
    }

    public String getCurrentEventType() {
        if (plugin.getBossManager().hasActiveBoss()) return "bosses";
        if (plugin.getMystManager().hasActiveChest()) return "myst";
        if (plugin.getCastleManager().hasActiveCastle()) return "castle";
        return null;
    }

    public void clearCurrentEvent() {
        currentEventType = null;
    }

    public String getTimeLeft() {
        long diff = nextSpawnTime - System.currentTimeMillis();
        if (diff <= 0) return "Скоро";

        long sec = diff / 1000;
        long min = sec / 60;
        long hour = min / 60;
        sec %= 60;
        min %= 60;

        if (hour > 0) return String.format("%dч %dм %dс", hour, min, sec);
        if (min > 0) return String.format("%dм %dс", min, sec);
        return String.format("%dс", sec);
    }

    public long getNextSpawnTime() {
        return nextSpawnTime;
    }
}