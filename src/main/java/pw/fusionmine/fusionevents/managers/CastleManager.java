package pw.fusionmine.fusionevents.managers;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pw.fusionmine.fusionevents.FusionEvents;
import pw.fusionmine.fusionevents.utils.ColorUtils;
import pw.fusionmine.fusionevents.utils.HologramUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class CastleManager implements InventoryHolder {

    private final FusionEvents plugin;
    private Location castleLocation;
    private List<Chunk> forceLoadedChunks = new ArrayList<>();
    private String mainHologramName;
    private BukkitTask despawnTask;
    private BukkitTask hologramUpdateTask;
    private long despawnStartTime;
    private boolean isActive;
    private final Map<String, CastleShulker> shulkers;
    private final List<String> shulkerHolograms;
    private final Set<String> processingShulkers = ConcurrentHashMap.newKeySet();
    private int durationSeconds;

    public CastleManager(FusionEvents plugin) {
        this.plugin = plugin;
        this.shulkers = new ConcurrentHashMap<>();
        this.shulkerHolograms = new ArrayList<>();
        this.isActive = false;
    }

    @Override
    public Inventory getInventory() { return null; }

    public String getKey(Location loc) {
        return loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    public Map<String, CastleShulker> getShulkers() {
        return shulkers;
    }

    public void spawnCastle() {
        if (isActive) return;
        FileConfiguration config = plugin.getConfigManager().getCastleConfig();
        String worldName = config.getString("spawn.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        double x = config.getDouble("spawn.x", 0.0);
        double y = config.getDouble("spawn.y", 0.0);
        double z = config.getDouble("spawn.z", 0.0);
        castleLocation = new Location(world, x, y, z);

        loadChunksInRadius(castleLocation, 2);

        isActive = true;
        shulkers.clear();
        shulkerHolograms.clear();

        spawnShulkers();
        createMainHologram();

        durationSeconds = config.getInt("duration", 600);
        despawnStartTime = System.currentTimeMillis();

        startDespawnTask();
        startHologramUpdateTask();
        broadcastSpawnMessage(castleLocation);
    }

    public void loadChunksInRadius(Location centerLocation, int radiusChunks) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            int minChunkX = (int) Math.floor(centerLocation.getX() / 16.0) - radiusChunks;
            int maxChunkX = (int) Math.ceil(centerLocation.getX() / 16.0) + radiusChunks;
            int minChunkZ = (int) Math.floor(centerLocation.getZ() / 16.0) - radiusChunks;
            int maxChunkZ = (int) Math.ceil(centerLocation.getZ() / 16.0) + radiusChunks;

            for (int x = minChunkX; x <= maxChunkX; x++) {
                for (int z = minChunkZ; z <= maxChunkZ; z++) {
                    Chunk chunk = centerLocation.getWorld().getChunkAt(x, z);
                    if (!chunk.isLoaded()) {
                        chunk.load();
                    }
                    chunk.setForceLoaded(true);
                    forceLoadedChunks.add(chunk);
                }
            }
        });
    }

    private void spawnShulkers() {
        List<String> spots = plugin.getConfigManager().getCastleConfig().getStringList("shulker-spots");
        if (castleLocation == null || spots.isEmpty()) return;

        plugin.getLogger().info("[Castle] Spawning " + spots.size() + " shulkers...");

        for (String spot : spots) {
            try {
                String[] parts = spot.split(":");
                if (parts.length < 2) continue;

                String[] coords = parts[1].split(",");
                int ox = Integer.parseInt(coords[0]);
                int oy = Integer.parseInt(coords[1]);
                int oz = Integer.parseInt(coords[2]);

                Location sLoc = new Location(castleLocation.getWorld(), ox, oy, oz);
                sLoc.getBlock().setType(Material.SHULKER_BOX);

                if (sLoc.getBlock().getState() instanceof ShulkerBox) {
                    ShulkerBox box = (ShulkerBox) sLoc.getBlock().getState();
                    box.setCustomName(ColorUtils.colorize("&fШалкер"));
                    box.update(true);
                }

                String rarity = getRandomRarity();
                CastleShulker cs = new CastleShulker(sLoc, rarity, parts[0]);
                String key = getKey(sLoc);
                shulkers.put(key, cs);
                createShulkerHologram(cs, key);

            } catch (Exception e) {
                plugin.getLogger().warning("[Castle] Failed to spawn shulker: " + e.getMessage());
            }
        }
    }

    private void createShulkerHologram(CastleShulker shulker, String key) {
        World w = shulker.getLocation().getWorld();
        if (w == null) return;

        Location base = shulker.getLocation().getBlock().getLocation().add(0.5, 1.0, 0.5);
        String rName = plugin.getConfigManager().getCastleRaritiesConfig().getString(shulker.getRarity() + ".display-name", "&7???");
        int max = getShulkerMaxHits(shulker.getRarity());

        List<String> lines = Arrays.asList(
                ColorUtils.colorize(rName),
                ColorUtils.colorize("&7Прочность: &c" + shulker.getHitsLeft() + "&7/&c" + max));

        HologramUtils.createHologram(key, base, lines);
        shulkerHolograms.add(key);
    }

    private void updateShulkerHologram(CastleShulker shulker, String key) {
        String rName = plugin.getConfigManager().getCastleRaritiesConfig().getString(shulker.getRarity() + ".display-name", "&7???");
        int max = getShulkerMaxHits(shulker.getRarity());
        List<String> newLines = Arrays.asList(
                ColorUtils.colorize(rName),
                ColorUtils.colorize("&7Прочность: &c" + shulker.getHitsLeft() + "&7/&c" + max));
        HologramUtils.updateHologramText(key, newLines);
    }

    private void removeShulkerHologram(String key) {
        HologramUtils.removeHologram(key);
    }

    private void removeAllShulkerHolograms() {
        shulkerHolograms.forEach(name -> HologramUtils.removeHologram(name));
        shulkerHolograms.clear();
    }

    private int getShulkerMaxHits(String rarity) {
        return plugin.getConfigManager().getCastleRaritiesConfig().getInt(rarity + ".shulker-hits", 5);
    }

    private String getRandomRarity() {
        FileConfiguration raritiesConfig = plugin.getConfigManager().getCastleRaritiesConfig();
        Set<String> rarities = raritiesConfig.getKeys(false);
        if (rarities.isEmpty()) return "common";

        List<String> rarityList = new ArrayList<>(rarities);
        double random = ThreadLocalRandom.current().nextDouble() * 100;
        double cumulative = 0;

        for (String rarity : rarityList) {
            cumulative += raritiesConfig.getDouble(rarity + ".chance", 0);
            if (random <= cumulative) {
                return rarity;
            }
        }
        return rarityList.get(0);
    }

    private void createMainHologram() {
        if (castleLocation == null) return;
        List<String> lines = Arrays.asList(
                ColorUtils.colorize("&#FF6B6B&l🏰 ЗАМОК "),
                ColorUtils.colorize("&fЗачистите шалкеры!"),
                ColorUtils.colorize("&7Время: &#FF6B6B" + durationSeconds + "с")
        );
        Location base = castleLocation.getBlock().getLocation().add(0.5, 12.0, 0.5);
        mainHologramName = HologramUtils.createHologram(base, lines);
    }

    private void updateMainHologram() {
        if (castleLocation == null) return;
        String timeLeft = getTimeLeft();
        List<String> lines = Arrays.asList(
                ColorUtils.colorize("&#FF6B6B&l🏰 ЗАМОК "),
                ColorUtils.colorize("&fЗачистите шалкеры!"),
                ColorUtils.colorize("&7Время: &#FF6B6B" + timeLeft)
        );
        HologramUtils.updateHologramText(mainHologramName, lines);
    }

    private void removeMainHolograms() {
        if(mainHologramName != null) {
            HologramUtils.removeHologram(mainHologramName);
            mainHologramName = null;
        }
    }

    private void startDespawnTask() {
        if (despawnTask != null) despawnTask.cancel();
        despawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) { cancel(); return; }
                long elapsed = (System.currentTimeMillis() - despawnStartTime) / 1000;
                if (elapsed >= durationSeconds) {
                    broadcastDespawnMessage();
                    removeCastle();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void startHologramUpdateTask() {
        if (hologramUpdateTask != null) hologramUpdateTask.cancel();
        hologramUpdateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isActive) { cancel(); return; }
                updateMainHologram();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public boolean handleShulkerBreak(Location loc, Player player) {
        String key = loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
        if (processingShulkers.contains(key)) return false;

        CastleShulker shulker = shulkers.get(key);
        if (shulker == null) return false;

        processingShulkers.add(key);
        shulker.damage();
        updateShulkerHologram(shulker, key);

        if (shulker.isBroken()) {
            shulkers.remove(key);
            removeShulkerHologram(key);
            player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getCastleMessage("shulker-opened")));
            final Location safeLoc = loc.clone();
            new BukkitRunnable() {
                @Override
                public void run() {
                    dropLootAndRemoveBlock(safeLoc, shulker.getRarity(), player);
                }
            }.runTaskLater(plugin, 1L);
            new BukkitRunnable() {
                @Override
                public void run() {
                    forceRemoveBlock(safeLoc);
                }
            }.runTaskLater(plugin, 2L);

            processingShulkers.remove(key);
            return true;
        } else {
            player.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getCastleMessage("shulker-broken").replace("{hits}", String.valueOf(shulker.getHitsLeft()))));
            processingShulkers.remove(key);
            return false;
        }
    }

    private void dropLootAndRemoveBlock(Location loc, String rarity, Player player) {
        List<ItemStack> loot = getRandomLoot(rarity, 1, 2);
        for (ItemStack item : loot) {
            loc.getWorld().dropItemNaturally(loc.clone().add(0.5, 0.5, 0.5), item);
        }

        if (!loot.isEmpty()) {
            loc.getWorld().playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);
            loc.getWorld().spawnParticle(Particle.ENCHANT, loc.clone().add(0.5, 0.5, 0.5), 30, 0.3, 0.3, 0.3, 0.05);
        }

        Block block = loc.getBlock();
        block.setType(Material.AIR, true);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendBlockChange(loc, Material.AIR.createBlockData());
        }
        loc.getWorld().refreshChunk(loc.getChunk().getX(), loc.getChunk().getZ());
        if (shulkers.isEmpty()) {
            removeCastle();
        }
    }

    private void forceRemoveBlock(Location loc) {
        Block block = loc.getBlock();
        if (block.getType() != Material.AIR) {
            block.setType(Material.AIR, true);
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendBlockChange(loc, Material.AIR.createBlockData());
            }
        }
    }

    private List<ItemStack> getRandomLoot(String rarity, int min, int max) {
        File f = new File(plugin.getDataFolder(), "castle/items/" + rarity + ".yml");
        if (!f.exists()) return Collections.emptyList();

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        List<ItemStack> pool = new ArrayList<>();
        for (String k : cfg.getKeys(false)) {
            ItemStack item = cfg.getItemStack(k);
            if (item != null) pool.add(item.clone());
        }

        if (pool.isEmpty()) return Collections.emptyList();

        Collections.shuffle(pool);
        int count = ThreadLocalRandom.current().nextInt(min, max + 1);
        count = Math.min(count, pool.size());
        return new ArrayList<>(pool.subList(0, count));
    }

    private void broadcastSpawnMessage(Location loc) {
        for (String line : plugin.getConfigManager().getCastleMessageList("spawn")) {
            String p = line.replace("{x}", String.valueOf(loc.getBlockX()))
                    .replace("{y}", String.valueOf(loc.getBlockY()))
                    .replace("{z}", String.valueOf(loc.getBlockZ()))
                    .replace("{world}", loc.getWorld().getName());
            executeAction(p);
        }

        Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f));
    }

    private void broadcastDespawnMessage() {
        for (String line : plugin.getConfigManager().getCastleMessageList("despawn")) executeAction(line);

        Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 1.0f));
    }

    private void executeAction(String action) {
        if (action.startsWith("[message]")) {
            String msg = ColorUtils.colorize(action.substring(9).trim());
            for (Player p : Bukkit.getOnlinePlayers()) p.sendMessage(msg);
            Bukkit.getConsoleSender().sendMessage(msg);
        } else {
            String msg = ColorUtils.colorize(action);
            for (Player p : Bukkit.getOnlinePlayers()) p.sendMessage(msg);
            Bukkit.getConsoleSender().sendMessage(msg);
        }
    }

    public void removeCastle() {
        if (despawnTask != null) { despawnTask.cancel(); despawnTask = null; }
        if (hologramUpdateTask != null) { hologramUpdateTask.cancel(); hologramUpdateTask = null; }

        removeMainHolograms();
        removeAllShulkerHolograms();

        for (String key : new ArrayList<>(shulkers.keySet())) {
            try {
                Location loc = shulkers.get(key).getLocation();
                if (loc.getBlock().getType() == Material.SHULKER_BOX) {
                    loc.getBlock().setType(Material.AIR);
                }
            } catch (Exception ignored) {}
        }

        shulkers.clear();
        processingShulkers.clear();
        castleLocation = null;
        isActive = false;
        plugin.getEventManager().clearCurrentEvent();

        forceLoadedChunks.forEach(chunk -> chunk.setForceLoaded(false));
        forceLoadedChunks.clear();
    }

    public boolean hasActiveCastle() { return isActive && castleLocation != null; }
    public Location getCastleLocation() { return castleLocation; }
    public boolean isShulkerBox(Location loc) { return shulkers.containsKey(getKey(loc)); }

    public void saveRarityItems(String rarity, Inventory inventory) {
        File folder = new File(plugin.getDataFolder(), "castle/items");
        if (!folder.exists()) folder.mkdirs();
        File f = new File(folder, rarity + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();

        int idx = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                cfg.set("item_" + idx, item);
                idx++;
            }
        }

        try {
            cfg.save(f);
            plugin.getLogger().info("Saved " + idx + " items for rarity: " + rarity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Inventory loadRarityItems(String rarity) {
        Inventory inv = Bukkit.createInventory(null, 54, ColorUtils.colorize("&4&lРедактор замка: &c" + rarity));
        File f = new File(plugin.getDataFolder(), "castle/items/" + rarity + ".yml");
        if (!f.exists()) return inv;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(f);
        int slot = 0;

        for (String key : cfg.getKeys(false)) {
            ItemStack item = cfg.getItemStack(key);
            if (item != null && slot < 54) {
                inv.setItem(slot, item);
                slot++;
            }
        }

        return inv;
    }

    public List<String> getRarityList() {
        return new ArrayList<>(plugin.getConfigManager().getCastleRaritiesConfig().getKeys(false));
    }

    public String getTimeLeft() {
        if (!isActive) return "0с";
        long elapsed = (System.currentTimeMillis() - despawnStartTime) / 1000;
        long remaining = durationSeconds - elapsed;
        if (remaining <= 0) return "0с";
        long min = remaining / 60;
        long sec = remaining % 60;
        return min > 0 ? String.format("%dм %dс", min, sec) : String.format("%dс", sec);
    }

    public class CastleShulker {
        private final Location location;
        private final String rarity;
        private final String spotName;
        private int hitsLeft;

        public CastleShulker(Location location, String rarity, String spotName) {
            this.location = location;
            this.rarity = rarity;
            this.spotName = spotName;
            this.hitsLeft = getShulkerMaxHits(rarity);
        }

        public void damage() { if (hitsLeft > 0) hitsLeft--; }
        public boolean isBroken() { return hitsLeft <= 0; }
        public int getHitsLeft() { return hitsLeft; }
        public Location getLocation() { return location; }
        public String getRarity() { return rarity; }
        public String getSpotName() { return spotName; }
    }
}