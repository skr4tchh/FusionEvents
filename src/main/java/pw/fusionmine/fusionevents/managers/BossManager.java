package pw.fusionmine.fusionevents.managers;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pw.fusionmine.fusionevents.FusionEvents;
import pw.fusionmine.fusionevents.utils.ColorUtils;

import java.util.*;

public class BossManager {

    private final FusionEvents plugin;
    private LivingEntity currentBoss;
    private String currentBossId;
    private BukkitTask auraTask;
    private BukkitTask bossbarTask;
    private BukkitTask despawnTask;
    private BossBar bossBar;
    private final Map<UUID, Double> damageTracker;
    private double maxHealth;
    private Chunk forceLoadedChunk;
    private long bossSpawnTime;

    public BossManager(FusionEvents plugin) {
        this.plugin = plugin;
        this.damageTracker = new HashMap<>();
    }

    public void startAuraTasks() {
        startAuraTask();
    }

    public void stopTasks() {
        if (auraTask != null) {
            auraTask.cancel();
        }
        if (bossbarTask != null) {
            bossbarTask.cancel();
        }
        if (despawnTask != null) {
            despawnTask.cancel();
        }
        removeBossBar();
        unloadForceLoadedChunk();
    }

    private void startAuraTask() {
        auraTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (currentBoss == null || currentBoss.isDead() || !currentBoss.isValid()) {
                    return;
                }

                ConfigurationSection bossConfig = plugin.getConfigManager().getBossesConfig().getConfigurationSection(currentBossId);
                if (bossConfig == null) return;

                List<String> effects = bossConfig.getStringList("effect-aura");
                double radius = bossConfig.getDouble("aura-radius", 10.0);

                for (Entity nearby : currentBoss.getNearbyEntities(radius, radius, radius)) {
                    if (nearby instanceof Player) {
                        Player player = (Player) nearby;
                        for (String effectStr : effects) {
                            String[] parts = effectStr.split(":");
                            if (parts.length >= 2) {
                                PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                                int amplifier = Integer.parseInt(parts[1]) - 1;
                                int duration = parts.length > 2 ? Integer.parseInt(parts[2]) : 60;

                                if (type != null) {
                                    player.addPotionEffect(new PotionEffect(type, duration, amplifier));
                                }
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 40L, 40L);
    }

    public boolean spawnBoss(String name) {
        if (currentBoss != null && currentBoss.isValid() && !currentBoss.isDead()) {
            return false;
        }

        ConfigurationSection bossConfig = plugin.getConfigManager().getBossesConfig().getConfigurationSection(name);
        if (bossConfig == null) {
            return false;
        }

        ConfigurationSection spawnConfig = plugin.getConfigManager().getSpawnConfig().getConfigurationSection("spawn-location");
        if (spawnConfig == null) {
            return false;
        }

        String worldName = spawnConfig.getString("world", "world");
        double x = spawnConfig.getDouble("x", 0);
        double y = spawnConfig.getDouble("y", 64);
        double z = spawnConfig.getDouble("z", 0);

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return false;
        }

        Location location = new Location(world, x, y, z);
        Chunk chunk = location.getChunk();

        if (!chunk.isLoaded()) {
            chunk.load();
        }

        chunk.setForceLoaded(true);
        forceLoadedChunk = chunk;

        damageTracker.clear();

        EntityType entityType = EntityType.valueOf(bossConfig.getString("type", "ZOMBIE").toUpperCase());
        currentBoss = (LivingEntity) world.spawnEntity(location, entityType);
        currentBossId = name;

        String displayName = ColorUtils.colorize(bossConfig.getString("display-name", name));
        currentBoss.setCustomName(displayName);
        currentBoss.setCustomNameVisible(true);

        maxHealth = bossConfig.getDouble("hp", 100);
        currentBoss.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
        currentBoss.setHealth(maxHealth);

        double damage = bossConfig.getDouble("damage", 5);
        if (currentBoss.getAttribute(Attribute.ATTACK_DAMAGE) != null) {
            currentBoss.getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(damage);
        }

        currentBoss.setRemoveWhenFarAway(false);

        bossSpawnTime = System.currentTimeMillis();

        createBossBar(displayName);
        startDespawnTask();
        broadcastSpawnMessage(name, location, maxHealth);

        return true;
    }

    private void startDespawnTask() {
        if (despawnTask != null) {
            despawnTask.cancel();
        }

        long despawnTime = plugin.getConfigManager().getSpawnConfig().getLong("despawn-time", 300) * 20L;

        despawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (currentBoss != null && currentBoss.isValid() && !currentBoss.isDead()) {
                    broadcastDespawnMessage();
                    currentBoss.remove();
                    clearBoss();
                }
            }
        }.runTaskLater(plugin, despawnTime);
    }

    private void broadcastDespawnMessage() {
        if (currentBossId == null) return;

        ConfigurationSection bossConfig = plugin.getConfigManager().getBossesConfig().getConfigurationSection(currentBossId);
        String displayName = bossConfig != null ? ColorUtils.colorize(bossConfig.getString("display-name", currentBossId)) : currentBossId;

        String message = ColorUtils.colorize(plugin.getConfigManager().getMessage("boss-despawned").replace("{name}", displayName));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
        }
        Bukkit.getConsoleSender().sendMessage(message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 1.0f, 1.0f);
        }
    }

    public String getTimeLeftDespawn() {
        long despawnTime = plugin.getConfigManager().getSpawnConfig().getLong("despawn-time", 300) * 1000;
        long elapsed = System.currentTimeMillis() - bossSpawnTime;
        long remaining = despawnTime - elapsed;

        if (remaining <= 0) return "0с";

        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;

        if (minutes > 0) {
            return String.format("%dм %dс", minutes, seconds);
        } else {
            return String.format("%dс", seconds);
        }
    }

    private void unloadForceLoadedChunk() {
        if (forceLoadedChunk != null) {
            forceLoadedChunk.setForceLoaded(false);
            forceLoadedChunk = null;
        }
    }

    private void createBossBar(String bossDisplayName) {
        ConfigurationSection bossbarConfig = plugin.getConfigManager().getBossbarConfig();

        if (!bossbarConfig.getBoolean("enabled", true)) {
            return;
        }

        String title = bossbarConfig.getString("title", "&c&l⚔ {name} &8| &c❤ {health}/{max-health}");
        title = title.replace("{name}", bossDisplayName)
                .replace("{health}", String.valueOf((int) maxHealth))
                .replace("{max-health}", String.valueOf((int) maxHealth))
                .replace("{percent}", "100")
                .replace("{time-left}", getTimeLeftDespawn());

        BarColor color = BarColor.valueOf(bossbarConfig.getString("color-stages.high", "GREEN"));
        BarStyle style = BarStyle.valueOf(bossbarConfig.getString("style", "SEGMENTED_10"));

        bossBar = Bukkit.createBossBar(ColorUtils.colorize(title), color, style);
        bossBar.setProgress(1.0);
        bossBar.setVisible(true);

        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }

        startBossBarUpdateTask();
    }

    private void startBossBarUpdateTask() {
        ConfigurationSection bossbarConfig = plugin.getConfigManager().getBossbarConfig();
        long updateInterval = bossbarConfig.getLong("update-interval", 5);

        bossbarTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (currentBoss == null || currentBoss.isDead() || !currentBoss.isValid()) {
                    removeBossBar();
                    cancel();
                    return;
                }

                updateBossBar();
            }
        }.runTaskTimer(plugin, updateInterval, updateInterval);
    }

    public void updateBossBar() {
        if (bossBar == null || currentBoss == null) return;

        ConfigurationSection bossbarConfig = plugin.getConfigManager().getBossbarConfig();
        ConfigurationSection bossConfig = plugin.getConfigManager().getBossesConfig().getConfigurationSection(currentBossId);

        if (bossConfig == null) return;

        double currentHealth = currentBoss.getHealth();
        double healthPercent = (currentHealth / maxHealth) * 100;
        double progress = currentHealth / maxHealth;

        String displayName = ColorUtils.colorize(bossConfig.getString("display-name", currentBossId));
        String title = bossbarConfig.getString("title", "&c&l⚔ {name} &8| &c❤ {health}/{max-health}");
        title = title.replace("{name}", displayName)
                .replace("{health}", String.valueOf((int) currentHealth))
                .replace("{max-health}", String.valueOf((int) maxHealth))
                .replace("{percent}", String.format("%.1f", healthPercent))
                .replace("{time-left}", getTimeLeftDespawn());

        bossBar.setTitle(ColorUtils.colorize(title));
        bossBar.setProgress(Math.max(0, Math.min(1, progress)));

        int highThreshold = bossbarConfig.getInt("color-stages.high-threshold", 70);
        int mediumThreshold = bossbarConfig.getInt("color-stages.medium-threshold", 40);

        BarColor newColor;
        if (healthPercent >= highThreshold) {
            newColor = BarColor.valueOf(bossbarConfig.getString("color-stages.high", "GREEN"));
        } else if (healthPercent >= mediumThreshold) {
            newColor = BarColor.valueOf(bossbarConfig.getString("color-stages.medium", "YELLOW"));
        } else {
            newColor = BarColor.valueOf(bossbarConfig.getString("color-stages.low", "RED"));
        }
        bossBar.setColor(newColor);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!bossBar.getPlayers().contains(player)) {
                bossBar.addPlayer(player);
            }
        }
    }

    public void removeBossBar() {
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar.setVisible(false);
            bossBar = null;
        }
        if (bossbarTask != null) {
            bossbarTask.cancel();
            bossbarTask = null;
        }
    }

    public void addPlayerToBossBar(Player player) {
        if (bossBar != null && bossBar.isVisible()) {
            bossBar.addPlayer(player);
        }
    }

    private void broadcastSpawnMessage(String bossName, Location location, double maxHealth) {
        List<String> messages = plugin.getConfigManager().getMessageList("spawn");
        ConfigurationSection bossConfig = plugin.getConfigManager().getBossesConfig().getConfigurationSection(bossName);
        String displayName = bossConfig != null ? ColorUtils.colorize(bossConfig.getString("display-name", bossName)) : bossName;

        for (String line : messages) {
            String message = ColorUtils.colorize(line
                    .replace("{name}", displayName)
                    .replace("{max-health}", String.valueOf((int) maxHealth))
                    .replace("{x}", String.valueOf((int) location.getX()))
                    .replace("{y}", String.valueOf((int) location.getY()))
                    .replace("{z}", String.valueOf((int) location.getZ()))
                    .replace("{world}", location.getWorld().getName()));
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message);
            }
            Bukkit.getConsoleSender().sendMessage(message);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
        }
    }

    public void broadcastDeathMessage() {
        if (currentBossId == null) return;

        List<String> messages = plugin.getConfigManager().getMessageList("death");
        ConfigurationSection bossConfig = plugin.getConfigManager().getBossesConfig().getConfigurationSection(currentBossId);
        String displayName = bossConfig != null ? ColorUtils.colorize(bossConfig.getString("display-name", currentBossId)) : currentBossId;

        for (String line : messages) {
            String message = ColorUtils.colorize(line.replace("{name}", displayName));
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message);
            }
            Bukkit.getConsoleSender().sendMessage(message);
        }

        broadcastTopDamage();
        giveRewards();
        removeBossBar();
    }

    public void broadcastTopDamage() {
        List<String> topAnnouncement = plugin.getConfigManager().getMessageList("top-announcement");
        for (String line : topAnnouncement) {
            String message = ColorUtils.colorize(line);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(message);
            }
            Bukkit.getConsoleSender().sendMessage(message);
        }

        String topLineFormat = plugin.getConfigManager().getDesignConfig().getString("messages.top-line", "  &#FFB429{place}. &#FFB429{player} &7- &c{damage} урона");

        List<Map.Entry<UUID, Double>> sortedDamage = new ArrayList<>(damageTracker.entrySet());
        sortedDamage.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int place = 1;
        for (Map.Entry<UUID, Double> entry : sortedDamage) {
            if (place > 3) break;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
            String playerName = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";

            String topLine = ColorUtils.colorize(topLineFormat
                    .replace("{place}", String.valueOf(place))
                    .replace("{player}", playerName)
                    .replace("{damage}", String.format("%.1f", entry.getValue())));

            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(topLine);
            }
            Bukkit.getConsoleSender().sendMessage(topLine);
            place++;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage("");
        }
    }

    public void giveRewards() {
        if (currentBossId == null) return;

        List<Map.Entry<UUID, Double>> sortedDamage = new ArrayList<>(damageTracker.entrySet());
        sortedDamage.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        ConfigurationSection bossRewards = plugin.getConfigManager().getRewardsConfig().getConfigurationSection(currentBossId);

        if (bossRewards == null) {
            return;
        }

        int place = 1;
        for (Map.Entry<UUID, Double> entry : sortedDamage) {
            if (place > 3) break;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(entry.getKey());
            String playerName = offlinePlayer.getName();

            if (playerName == null) {
                place++;
                continue;
            }

            List<String> commands = bossRewards.getStringList("top-" + place + ".commands");
            for (String command : commands) {
                String finalCommand = command.replace("{user}", playerName);
                executeRewardAction(finalCommand);
            }

            place++;
        }
    }

    private void executeRewardAction(String action) {
        if (action.startsWith("[message]")) {
            String message = action.substring("[message]".length()).trim();
            String colorized = ColorUtils.colorize(message);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(colorized);
            }
            Bukkit.getConsoleSender().sendMessage(colorized);
        } else if (action.startsWith("[player]")) {
            String command = action.substring("[player]".length()).trim();
            String playerName = command.split(" ")[0];
            Player player = Bukkit.getPlayer(playerName);
            if (player != null) {
                player.performCommand(command);
            }
        } else {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action);
        }
    }

    public void addDamage(UUID playerUUID, double damage) {
        damageTracker.merge(playerUUID, damage, Double::sum);
    }

    public void killCurrentBoss() {
        if (currentBoss != null && currentBoss.isValid() && !currentBoss.isDead()) {
            currentBoss.setHealth(0);
        }
    }

    public void removeAllBosses() {
        if (despawnTask != null) {
            despawnTask.cancel();
            despawnTask = null;
        }
        if (currentBoss != null && currentBoss.isValid()) {
            currentBoss.remove();
        }
        removeBossBar();
        unloadForceLoadedChunk();
        currentBoss = null;
        currentBossId = null;
        damageTracker.clear();
    }

    public boolean hasActiveBoss() {
        return currentBoss != null && currentBoss.isValid() && !currentBoss.isDead();
    }

    public LivingEntity getCurrentBoss() {
        return currentBoss;
    }

    public String getCurrentBossId() {
        return currentBossId;
    }

    public void clearBoss() {
        if (despawnTask != null) {
            despawnTask.cancel();
            despawnTask = null;
        }
        removeBossBar();
        unloadForceLoadedChunk();
        currentBoss = null;
        currentBossId = null;
        damageTracker.clear();
        plugin.getEventManager().clearCurrentEvent();
    }

    public List<String> getBossList() {
        return new ArrayList<>(plugin.getConfigManager().getBossesConfig().getKeys(false));
    }

    public Map<UUID, Double> getDamageTracker() {
        return damageTracker;
    }
}