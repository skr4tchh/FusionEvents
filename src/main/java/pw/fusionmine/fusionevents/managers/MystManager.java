package pw.fusionmine.fusionevents.managers;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import pw.fusionmine.fusionevents.FusionEvents;
import pw.fusionmine.fusionevents.utils.ColorUtils;
import pw.fusionmine.fusionevents.utils.WorldGuardUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class MystManager implements InventoryHolder {

    private final FusionEvents plugin;
    private static final String REGION_NAME = "fusionevents_myst_chest";
    private Location chestLocation;
    private Block chestBlock;
    private List<ArmorStand> holograms;
    private BukkitTask openTimerTask;
    private BukkitTask despawnTask;
    private BukkitTask gpsTask;
    private BukkitTask itemAppearTask;
    private Chunk forceLoadedChunk;
    private long despawnStartTime;
    private boolean isOpening;
    private boolean isOpened;
    private int openTimeLeft;
    private final Map<Integer, ItemStack> obfuscatedItems;
    private final Map<Integer, ItemStack> realItems;
    private final Set<UUID> gpsPlayers;
    private Inventory mystInventory;
    private int inventorySize;

    public MystManager(FusionEvents plugin) {
        this.plugin = plugin;
        this.holograms = new ArrayList<>();
        this.obfuscatedItems = new HashMap<>();
        this.realItems = new HashMap<>();
        this.gpsPlayers = new HashSet<>();
        this.isOpening = false;
        this.isOpened = false;
    }

    @Override
    public Inventory getInventory() {
        return mystInventory;
    }

    public void startGpsTasks() {
        startGpsTask();
    }

    public void stopTasks() {
        if (openTimerTask != null) {
            openTimerTask.cancel();
            openTimerTask = null;
        }
        if (despawnTask != null) {
            despawnTask.cancel();
            despawnTask = null;
        }
        if (gpsTask != null) {
            gpsTask.cancel();
            gpsTask = null;
        }
        if (itemAppearTask != null) {
            itemAppearTask.cancel();
            itemAppearTask = null;
        }
    }

    private void startGpsTask() {
        gpsTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!hasActiveChest() || chestLocation == null) {
                    return;
                }

                Iterator<UUID> iterator = gpsPlayers.iterator();
                while (iterator.hasNext()) {
                    UUID uuid = iterator.next();
                    Player player = Bukkit.getPlayer(uuid);

                    if (player == null || !player.isOnline()) {
                        iterator.remove();
                        continue;
                    }

                    if (!player.getWorld().equals(chestLocation.getWorld())) {
                        continue;
                    }

                    player.setCompassTarget(chestLocation);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void addGpsPlayer(Player player) {
        gpsPlayers.add(player.getUniqueId());
        if (chestLocation != null) {
            player.setCompassTarget(chestLocation);
        }
    }

    public void removeGpsPlayer(Player player) {
        gpsPlayers.remove(player.getUniqueId());
    }

    public void spawnChestAsync() {
        CompletableFuture.supplyAsync(() -> {
            FileConfiguration spawnConfig = plugin.getConfigManager().getMystSpawnConfig();
            int minX = spawnConfig.getInt("bounds.min-x", -1000);
            int maxX = spawnConfig.getInt("bounds.max-x", 1000);
            int minZ = spawnConfig.getInt("bounds.min-z", -1000);
            int maxZ = spawnConfig.getInt("bounds.max-z", 1000);

            int x = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
            int z = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);

            return new int[]{x, z};
        }).thenAccept(coords -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    spawnChestAt(coords[0], coords[1]);
                }
            }.runTask(plugin);
        });
    }

    public void spawnChestAt(int x, int z) {
        FileConfiguration spawnConfig = plugin.getConfigManager().getMystSpawnConfig();
        String worldName = spawnConfig.getString("world", "world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) return;

        int y = world.getHighestBlockYAt(x, z) + 1;
        Location location = new Location(world, x, y, z);

        Chunk chunk = location.getChunk();
        if (!chunk.isLoaded()) {
            chunk.load();
        }
        chunk.setForceLoaded(true);
        forceLoadedChunk = chunk;

        chestLocation = location;
        chestBlock = location.getBlock();
        chestBlock.setType(Material.CHEST);

        isOpening = false;
        isOpened = false;
        obfuscatedItems.clear();
        realItems.clear();
        gpsPlayers.clear();
        mystInventory = null;

        if (spawnConfig.getBoolean("region.enabled", true)) {
            int regionRadius = spawnConfig.getInt("region.radius", 7);
            WorldGuardUtils.createMystRegion(chestLocation, regionRadius, REGION_NAME);
        }

        createHologram();

        despawnStartTime = System.currentTimeMillis();
        startDespawnTask();

        broadcastSpawnMessage();
    }

    private void createMystInventory() {
        FileConfiguration spawnConfig = plugin.getConfigManager().getMystSpawnConfig();
        FileConfiguration designConfig = plugin.getConfigManager().getMystDesignConfig();

        int slots = spawnConfig.getInt("slots", 27);
        inventorySize = ((slots - 1) / 9 + 1) * 9;
        if (inventorySize < 9) inventorySize = 9;
        if (inventorySize > 54) inventorySize = 54;

        String title = designConfig.getString("inventory-title", "&#BE44FC&l✦ &#9C50F6&lМИСТИЧЕСКИЙ СУНДУК &#BE44FC&l✦");
        mystInventory = Bukkit.createInventory(this, inventorySize, ColorUtils.colorize(title));
    }

    private void startDespawnTask() {
        if (despawnTask != null) {
            despawnTask.cancel();
            despawnTask = null;
        }

        long despawnTime = plugin.getConfigManager().getMystSpawnConfig().getLong("despawn-time", 300) * 20L;

        despawnTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (hasActiveChest()) {
                    broadcastDespawnMessage();
                    removeChest();
                }
            }
        }.runTaskLater(plugin, despawnTime);
    }

    private void broadcastDespawnMessage() {
        List<String> messages = plugin.getConfigManager().getMystMessageList("despawn");

        for (String line : messages) {
            executeAction(line);
        }

        Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f));
    }

    public String getTimeLeftDespawn() {
        if (isOpening) return "Открывается";

        long despawnTime = plugin.getConfigManager().getMystSpawnConfig().getLong("despawn-time", 300) * 1000;
        long elapsed = System.currentTimeMillis() - despawnStartTime;
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

    private void createHologram() {
        if (chestLocation == null) return;

        removeHolograms();

        FileConfiguration designConfig = plugin.getConfigManager().getMystDesignConfig();
        List<String> lines = designConfig.getStringList("hologram.waiting");

        double heightStart = designConfig.getDouble("hologram.height", 2.5);
        double lineSpacing = designConfig.getDouble("hologram.line-spacing", 0.3);

        for (int i = 0; i < lines.size(); i++) {
            Location holoLoc = chestLocation.clone().add(0.5, heightStart - (i * lineSpacing), 0.5);
            ArmorStand stand = (ArmorStand) chestLocation.getWorld().spawnEntity(holoLoc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
            stand.setCustomName(ColorUtils.colorize(lines.get(i)));
            stand.setMarker(true);
            stand.setInvulnerable(true);
            holograms.add(stand);
        }
    }

    public void updateHologram(List<String> lines) {
        removeHolograms();

        if (chestLocation == null) return;

        FileConfiguration designConfig = plugin.getConfigManager().getMystDesignConfig();
        double heightStart = designConfig.getDouble("hologram.height", 2.5);
        double lineSpacing = designConfig.getDouble("hologram.line-spacing", 0.3);

        for (int i = 0; i < lines.size(); i++) {
            Location holoLoc = chestLocation.clone().add(0.5, heightStart - (i * lineSpacing), 0.5);
            ArmorStand stand = (ArmorStand) chestLocation.getWorld().spawnEntity(holoLoc, EntityType.ARMOR_STAND);
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setCustomNameVisible(true);
            stand.setCustomName(ColorUtils.colorize(lines.get(i)));
            stand.setMarker(true);
            stand.setInvulnerable(true);
            holograms.add(stand);
        }
    }

    private void removeHolograms() {
        for (ArmorStand stand : holograms) {
            if (stand != null && stand.isValid()) {
                stand.remove();
            }
        }
        holograms.clear();
    }

    public void startOpeningTimer(Player activator) {
        if (isOpening || isOpened) return;

        isOpening = true;

        if (despawnTask != null) {
            despawnTask.cancel();
            despawnTask = null;
        }

        FileConfiguration spawnConfig = plugin.getConfigManager().getMystSpawnConfig();
        openTimeLeft = spawnConfig.getInt("open-timer", 30);

        FileConfiguration designConfig = plugin.getConfigManager().getMystDesignConfig();

        openTimerTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (openTimeLeft <= 0) {
                    openChest();
                    cancel();
                    return;
                }

                List<String> lines = new ArrayList<>();
                for (String line : designConfig.getStringList("hologram.opening")) {
                    lines.add(line.replace("{time}", String.valueOf(openTimeLeft)));
                }
                updateHologram(lines);

                openTimeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void openChest() {
        isOpened = true;
        isOpening = false;

        if (openTimerTask != null) {
            openTimerTask.cancel();
            openTimerTask = null;
        }

        FileConfiguration designConfig = plugin.getConfigManager().getMystDesignConfig();
        updateHologram(designConfig.getStringList("hologram.opened"));

        createMystInventory();

        broadcastOpenedMessage();

        startItemAppearance();

        despawnStartTime = System.currentTimeMillis();
        startDespawnTask();
    }

    private void broadcastOpenedMessage() {
        List<String> messages = plugin.getConfigManager().getMystMessageList("opened");

        for (String line : messages) {
            executeAction(line);
        }
    }

    public void openMystInventory(Player player) {
        if (mystInventory != null) {
            player.openInventory(mystInventory);
        }
    }

    private void startItemAppearance() {
        if (mystInventory == null) return;

        FileConfiguration raritiesConfig = plugin.getConfigManager().getMystRaritiesConfig();
        FileConfiguration spawnConfig = plugin.getConfigManager().getMystSpawnConfig();

        int itemCount = spawnConfig.getInt("item-count", 9);
        int slots = spawnConfig.getInt("slots", 27);
        int actualSlots = Math.min(slots, inventorySize);

        int minItemsPerTick = spawnConfig.getInt("item-appearance.items-per-tick.min", 1);
        int maxItemsPerTick = spawnConfig.getInt("item-appearance.items-per-tick.max", 3);
        int minDelay = spawnConfig.getInt("item-appearance.delay.min", 20);
        int maxDelay = spawnConfig.getInt("item-appearance.delay.max", 40);

        List<Integer> availableSlots = new ArrayList<>();
        for (int i = 0; i < actualSlots; i++) {
            availableSlots.add(i);
        }
        Collections.shuffle(availableSlots);

        List<ItemData> itemsToSpawn = new ArrayList<>();

        for (int i = 0; i < Math.min(itemCount, availableSlots.size()); i++) {
            String rarity = selectRarity(raritiesConfig);
            ItemStack item = getRandomItemFromRarity(rarity);

            if (item != null) {
                int slot = availableSlots.get(i);
                ItemStack obfuscatedItem = obfuscateItem(item, rarity);

                realItems.put(slot, item.clone());
                obfuscatedItems.put(slot, obfuscatedItem);

                itemsToSpawn.add(new ItemData(slot, obfuscatedItem));
            }
        }

        Collections.shuffle(itemsToSpawn);

        spawnItemsGradually(itemsToSpawn, 0, minItemsPerTick, maxItemsPerTick, minDelay, maxDelay);
    }

    private void spawnItemsGradually(List<ItemData> itemsToSpawn, int startIndex,
                                     int minItemsPerTick, int maxItemsPerTick,
                                     int minDelay, int maxDelay) {
        if (startIndex >= itemsToSpawn.size()) return;
        if (mystInventory == null) return;

        int delay = startIndex == 0 ? 0 : ThreadLocalRandom.current().nextInt(minDelay, maxDelay + 1);

        itemAppearTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (mystInventory == null) {
                    return;
                }

                int itemsThisTick = ThreadLocalRandom.current().nextInt(minItemsPerTick, maxItemsPerTick + 1);
                int currentIndex = startIndex;

                for (int j = 0; j < itemsThisTick && currentIndex < itemsToSpawn.size(); j++) {
                    ItemData data = itemsToSpawn.get(currentIndex);

                    if (obfuscatedItems.containsKey(data.slot)) {
                        mystInventory.setItem(data.slot, data.item);

                        try {
                            if (chestLocation != null) {
                                chestLocation.getWorld().playSound(chestLocation, Sound.ENTITY_ITEM_PICKUP, 0.5f, 1.0f);
                            }
                        } catch (IllegalArgumentException ignored) {
                        }
                    }

                    currentIndex++;
                }

                if (currentIndex < itemsToSpawn.size()) {
                    spawnItemsGradually(itemsToSpawn, currentIndex, minItemsPerTick, maxItemsPerTick, minDelay, maxDelay);
                }
            }
        }.runTaskLater(plugin, delay);
    }

    private static class ItemData {
        int slot;
        ItemStack item;

        ItemData(int slot, ItemStack item) {
            this.slot = slot;
            this.item = item;
        }
    }

    private String selectRarity(FileConfiguration raritiesConfig) {
        double random = ThreadLocalRandom.current().nextDouble() * 100;
        double cumulative = 0;

        for (String rarity : raritiesConfig.getKeys(false)) {
            cumulative += raritiesConfig.getDouble(rarity + ".chance", 0);
            if (random <= cumulative) {
                return rarity;
            }
        }

        return raritiesConfig.getKeys(false).iterator().next();
    }

    private ItemStack getRandomItemFromRarity(String rarity) {
        File itemsFile = new File(plugin.getDataFolder(), "mysts/items/" + rarity + ".yml");
        if (!itemsFile.exists()) return null;

        YamlConfiguration itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        List<String> keys = new ArrayList<>(itemsConfig.getKeys(false));

        if (keys.isEmpty()) return null;

        String randomKey = keys.get(ThreadLocalRandom.current().nextInt(keys.size()));
        return itemsConfig.getItemStack(randomKey);
    }

    private ItemStack obfuscateItem(ItemStack realItem, String rarity) {
        FileConfiguration obfConfig = plugin.getConfigManager().getMystObfConfig();
        FileConfiguration raritiesConfig = plugin.getConfigManager().getMystRaritiesConfig();

        if (!obfConfig.getBoolean("enabled", true)) {
            return realItem;
        }

        String materialName = obfConfig.getString("material", "GRAY_STAINED_GLASS_PANE");
        Material obfMaterial = Material.valueOf(materialName.toUpperCase());

        ItemStack obfuscated = new ItemStack(obfMaterial);
        ItemMeta meta = obfuscated.getItemMeta();

        if (meta != null) {
            String displayName = raritiesConfig.getString(rarity + ".display-name", "&7???");
            meta.setDisplayName(ColorUtils.colorize(displayName));

            List<String> lore = new ArrayList<>();
            for (String line : obfConfig.getStringList("lore")) {
                lore.add(ColorUtils.colorize(line.replace("{rarity}",
                        ColorUtils.colorize(raritiesConfig.getString(rarity + ".display-name", rarity)))));
            }
            meta.setLore(lore);
            obfuscated.setItemMeta(meta);
        }

        return obfuscated;
    }

    public ItemStack revealItem(int slot) {
        if (realItems.containsKey(slot)) {
            ItemStack real = realItems.remove(slot);
            obfuscatedItems.remove(slot);
            return real;
        }
        return null;
    }

    public boolean isObfuscatedSlot(int slot) {
        return obfuscatedItems.containsKey(slot);
    }

    public boolean isMystInventory(Inventory inventory) {
        return mystInventory != null && inventory.getHolder() == this;
    }

    private void broadcastSpawnMessage() {
        List<String> messages = plugin.getConfigManager().getMystMessageList("spawn");

        for (String line : messages) {
            String processed = line
                    .replace("{x}", String.valueOf(chestLocation.getBlockX()))
                    .replace("{y}", String.valueOf(chestLocation.getBlockY()))
                    .replace("{z}", String.valueOf(chestLocation.getBlockZ()))
                    .replace("{world}", chestLocation.getWorld().getName());
            executeAction(processed);
        }

        Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f));
    }

    private void executeAction(String action) {
        if (action.startsWith("[message]")) {
            String message = action.substring("[message]".length()).trim();
            String colorized = ColorUtils.colorize(message);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(colorized);
            }
            Bukkit.getConsoleSender().sendMessage(colorized);
        } else {
            String colorized = ColorUtils.colorize(action);
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.sendMessage(colorized);
            }
            Bukkit.getConsoleSender().sendMessage(colorized);
        }
    }

    public void removeChest() {
        if (openTimerTask != null) {
            openTimerTask.cancel();
            openTimerTask = null;
        }

        if (despawnTask != null) {
            despawnTask.cancel();
            despawnTask = null;
        }

        if (itemAppearTask != null) {
            itemAppearTask.cancel();
            itemAppearTask = null;
        }

        if (mystInventory != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getOpenInventory().getTopInventory().getHolder() == this) {
                    player.closeInventory();
                }
            }
            mystInventory = null;
        }

        removeHolograms();

        if (chestLocation != null) {
            WorldGuardUtils.removeMystRegion(chestLocation.getWorld(), REGION_NAME);
        }

        if (chestBlock != null) {
            chestBlock.setType(Material.AIR);
        }

        if (forceLoadedChunk != null) {
            forceLoadedChunk.setForceLoaded(false);
            forceLoadedChunk = null;
        }

        chestLocation = null;
        chestBlock = null;
        isOpening = false;
        isOpened = false;
        obfuscatedItems.clear();
        realItems.clear();
        gpsPlayers.clear();

        plugin.getEventManager().clearCurrentEvent();
    }

    public boolean hasActiveChest() {
        return chestBlock != null && chestBlock.getType() == Material.CHEST;
    }

    public Location getChestLocation() {
        return chestLocation;
    }

    public boolean isOpening() {
        return isOpening;
    }

    public boolean isOpened() {
        return isOpened;
    }

    public Block getChestBlock() {
        return chestBlock;
    }

    public void saveRarityItems(String rarity, Inventory inventory) {
        File itemsFolder = new File(plugin.getDataFolder(), "mysts/items");
        if (!itemsFolder.exists()) {
            itemsFolder.mkdirs();
        }

        File itemsFile = new File(itemsFolder, rarity + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        int index = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                config.set("item_" + index, item);
                index++;
            }
        }

        try {
            config.save(itemsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Inventory loadRarityItems(String rarity) {
        Inventory inv = Bukkit.createInventory(null, 54, ColorUtils.colorize("&5&lРедактор: &d" + rarity));

        File itemsFile = new File(plugin.getDataFolder(), "mysts/items/" + rarity + ".yml");
        if (!itemsFile.exists()) {
            return inv;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(itemsFile);

        int slot = 0;
        for (String key : config.getKeys(false)) {
            ItemStack item = config.getItemStack(key);
            if (item != null && slot < 54) {
                inv.setItem(slot, item);
                slot++;
            }
        }

        return inv;
    }

    public List<String> getRarityList() {
        return new ArrayList<>(plugin.getConfigManager().getMystRaritiesConfig().getKeys(false));
    }

}