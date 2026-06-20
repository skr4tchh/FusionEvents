package pw.fusionmine.fusionevents.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pw.fusionmine.fusionevents.FusionEvents;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final FusionEvents plugin;
    private File bossesFolder;
    private File mystsFolder;
    private File castleFolder;
    private File bossesFile;
    private File designFile;
    private File spawnFile;
    private File rewardsFile;
    private File bossbarFile;
    private File mystDesignFile;
    private File mystSpawnFile;
    private File mystRaritiesFile;
    private File mystObfFile;
    private File castleConfigFile;
    private File castleRaritiesFile;
    private File eventFile;
    private FileConfiguration bossesConfig;
    private FileConfiguration designConfig;
    private FileConfiguration spawnConfig;
    private FileConfiguration rewardsConfig;
    private FileConfiguration bossbarConfig;
    private FileConfiguration mystDesignConfig;
    private FileConfiguration mystSpawnConfig;
    private FileConfiguration mystRaritiesConfig;
    private FileConfiguration mystObfConfig;
    private FileConfiguration castleConfig;
    private FileConfiguration castleRaritiesConfig;
    private FileConfiguration eventConfig;

    public ConfigManager(FusionEvents plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        bossesFolder = new File(plugin.getDataFolder(), "bosses");
        if (!bossesFolder.exists()) {
            bossesFolder.mkdirs();
        }

        mystsFolder = new File(plugin.getDataFolder(), "mysts");
        if (!mystsFolder.exists()) {
            mystsFolder.mkdirs();
        }

        File mystsItemsFolder = new File(mystsFolder, "items");
        if (!mystsItemsFolder.exists()) {
            mystsItemsFolder.mkdirs();
        }

        castleFolder = new File(plugin.getDataFolder(), "castle");
        if (!castleFolder.exists()) {
            castleFolder.mkdirs();
        }

        File castleItemsFolder = new File(castleFolder, "items");
        if (!castleItemsFolder.exists()) {
            castleItemsFolder.mkdirs();
        }

        bossesFile = new File(bossesFolder, "bosses.yml");
        designFile = new File(bossesFolder, "design.yml");
        spawnFile = new File(bossesFolder, "spawn.yml");
        rewardsFile = new File(bossesFolder, "rewards.yml");
        bossbarFile = new File(bossesFolder, "bossbar.yml");

        mystDesignFile = new File(mystsFolder, "design.yml");
        mystSpawnFile = new File(mystsFolder, "spawn.yml");
        mystRaritiesFile = new File(mystsFolder, "rarities.yml");
        mystObfFile = new File(mystsFolder, "obf.yml");

        castleConfigFile = new File(castleFolder, "config.yml");
        castleRaritiesFile = new File(castleFolder, "rarities.yml");

        eventFile = new File(plugin.getDataFolder(), "event.yml");

        createDefaultBossesConfig();
        createDefaultDesignConfig();
        createDefaultSpawnConfig();
        createDefaultRewardsConfig();
        createDefaultBossbarConfig();

        createDefaultMystDesignConfig();
        createDefaultMystSpawnConfig();
        createDefaultMystRaritiesConfig();
        createDefaultMystObfConfig();

        createDefaultCastleConfig();
        createDefaultCastleRaritiesConfig();

        createDefaultEventConfig();

        bossesConfig = YamlConfiguration.loadConfiguration(bossesFile);
        designConfig = YamlConfiguration.loadConfiguration(designFile);
        spawnConfig = YamlConfiguration.loadConfiguration(spawnFile);
        rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
        bossbarConfig = YamlConfiguration.loadConfiguration(bossbarFile);

        mystDesignConfig = YamlConfiguration.loadConfiguration(mystDesignFile);
        mystSpawnConfig = YamlConfiguration.loadConfiguration(mystSpawnFile);
        mystRaritiesConfig = YamlConfiguration.loadConfiguration(mystRaritiesFile);
        mystObfConfig = YamlConfiguration.loadConfiguration(mystObfFile);

        castleConfig = YamlConfiguration.loadConfiguration(castleConfigFile);
        castleRaritiesConfig = YamlConfiguration.loadConfiguration(castleRaritiesFile);

        eventConfig = YamlConfiguration.loadConfiguration(eventFile);
    }

    private void createDefaultBossesConfig() {
        if (bossesFile.exists()) return;

        YamlConfiguration config = new YamlConfiguration();

        config.set("zombie_king.type", "ZOMBIE");
        config.set("zombie_king.display-name", "&x&f&f&d&7&0&0&lКОРОЛЬ ЗОМБИ");
        config.set("zombie_king.hp", 300.0);
        config.set("zombie_king.damage", 12.0);
        config.set("zombie_king.aura-radius", 8.0);
        List<String> zombieEffects = new ArrayList<>();
        zombieEffects.add("HUNGER:2:40");
        zombieEffects.add("CONFUSION:1:30");
        config.set("zombie_king.effect-aura", zombieEffects);

        config.set("skeleton_lord.type", "SKELETON");
        config.set("skeleton_lord.display-name", "&x&c&0&c&0&c&0&lПОВЕЛИТЕЛЬ СКЕЛЕТОВ");
        config.set("skeleton_lord.hp", 250.0);
        config.set("skeleton_lord.damage", 10.0);
        config.set("skeleton_lord.aura-radius", 10.0);
        List<String> skeletonEffects = new ArrayList<>();
        skeletonEffects.add("SLOW:2:40");
        skeletonEffects.add("WEAKNESS:1:40");
        config.set("skeleton_lord.effect-aura", skeletonEffects);

        config.set("dark_wither.type", "WITHER_SKELETON");
        config.set("dark_wither.display-name", "&x&8&b&0&0&f&f&lТЕМНЫЙ ИССУШИТЕЛЬ");
        config.set("dark_wither.hp", 750.0);
        config.set("dark_wither.damage", 20.0);
        config.set("dark_wither.aura-radius", 12.0);
        List<String> witherEffects = new ArrayList<>();
        witherEffects.add("POISON:2:50");
        witherEffects.add("BLINDNESS:1:50");
        config.set("dark_wither.effect-aura", witherEffects);

        config.set("ancient_giant.type", "GIANT");
        config.set("ancient_giant.display-name", "&x&5&1&c&f&6&6&lДРЕВНИЙ ГИГАНТ");
        config.set("ancient_giant.hp", 1000.0);
        config.set("ancient_giant.damage", 25.0);
        config.set("ancient_giant.aura-radius", 10.0);
        List<String> giantEffects = new ArrayList<>();
        giantEffects.add("WEAKNESS:1:40");
        giantEffects.add("SLOW:2:40");
        config.set("ancient_giant.effect-aura", giantEffects);

        try {
            config.save(bossesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultDesignConfig() {
        if (designFile.exists()) return;

        YamlConfiguration config = new YamlConfiguration();

        config.set("prefix", "&x&F&B&9&C&0&8&lБ&x&F&C&A&6&0&6&lО&x&F&D&B&0&0&4&lС&x&F&E&B&A&0&2&lС&x&F&F&C&4&0&0&lЫ &8›&r ");

        config.set("colors.primary", "&6");
        config.set("colors.secondary", "&e");
        config.set("colors.error", "&c");
        config.set("colors.success", "&a");

        List<String> spawnMsg = new ArrayList<>();
        spawnMsg.add("&f");
        spawnMsg.add("     {prefix}");
        spawnMsg.add("&f");
        spawnMsg.add("  &fПоявился босс: {name} &fздоровье: §x§F§5§0§0§0§A{max-health} ❤");
        spawnMsg.add("  &fСкорее победи, и получи награду");
        spawnMsg.add("  &fПо слухам он на арене: &x&F&B&9&C&0&8/warp boss");
        spawnMsg.add("&f");
        config.set("messages.spawn", spawnMsg);

        List<String> deathMsg = new ArrayList<>();
        deathMsg.add("&f");
        deathMsg.add("     {prefix}");
        deathMsg.add("&f");
        deathMsg.add("  &fБосс: {name} &fбыл убит");
        deathMsg.add("  &fСледующий босс через &x&F&B&9&C&0&82 &fчаса");
        deathMsg.add("&f");
        config.set("messages.death", deathMsg);

        List<String> topAnnouncement = new ArrayList<>();
        topAnnouncement.add("&f");
        topAnnouncement.add("  &#F8D27D&lТ&#F8D078&lо&#F9CE73&lп &#FACB68&lи&#FAC963&lг&#FBC75E&lр&#FBC558&lо&#FCC353&lк&#FCC14E&lо&#FCBF49&lв &#FDBC3E&lп&#FEBA39&lо &#FFB62E&lн&#FFB429&lа&#FFB429&lн&#FFB429&lе&#FFB429&lс&#FFB429&lё&#FFB429&lн&#FFB429&lн&#FFB429&lо&#FFB429&lм&#FFB429&lу &#FFB429&lу&#FFB429&lр&#FFB429&lо&#FFB429&lн&#FFB429&lу&#FFB429&l:");
        topAnnouncement.add("&f");
        config.set("messages.top-announcement", topAnnouncement);

        config.set("messages.top-line", "  &#FFB429{place}. &#FFB429{player} &7- &c{damage} урона");
        config.set("messages.no-boss-active", "{prefix}&cНет активного босса!");
        config.set("messages.boss-spawned", "{prefix}&aБосс {name} &aуспешно создан!");
        config.set("messages.boss-not-found", "{prefix}&cБосс с ID &4{id} &cне найден!");
        config.set("messages.config-reloaded", "{prefix}&aКонфигурация перезагружена!");
        config.set("messages.boss-killed", "{prefix}&aАктивный босс убит!");
        config.set("messages.boss-already-exists", "{prefix}&cБосс уже активен!");
        config.set("messages.boss-despawned", "{prefix}&fБосс {name} &fисчез, так как его никто не победил!");
        config.set("messages.no-permission", "{prefix}&cУ вас нет прав для выполнения этой команды!");

        List<String> usageMsg = new ArrayList<>();
        usageMsg.add("{prefix}&6&lИспользование команд:");
        usageMsg.add("");
        usageMsg.add("  &e/bosses reload &7- Перезагрузить конфигурацию");
        usageMsg.add("  &e/bosses spawn <id> &7- Создать босса");
        usageMsg.add("  &e/bosses kill &7- Убить активного босса");
        usageMsg.add("  &e/bosses list &7- Список всех боссов");
        usageMsg.add("");
        config.set("commands.usage", usageMsg);

        try {
            config.save(designFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultSpawnConfig() {
        if (spawnFile.exists()) return;

        YamlConfiguration config = new YamlConfiguration();

        config.set("spawn-location.world", "world");
        config.set("spawn-location.x", 0.0);
        config.set("spawn-location.y", 64.0);
        config.set("spawn-location.z", 0.0);

        config.set("spawn-sound", "ENTITY_ENDER_DRAGON_GROWL");
        config.set("despawn-sound", "ENTITY_ENDER_DRAGON_DEATH");
        config.set("despawn-time", 300);

        try {
            config.save(spawnFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultRewardsConfig() {
        if (rewardsFile.exists()) return;

        YamlConfiguration config = new YamlConfiguration();

        List<String> zombieTop1 = new ArrayList<>();
        zombieTop1.add("eco give {user} 30000");
        zombieTop1.add("give {user} diamond 32");
        zombieTop1.add("crate givekey {user} rare 3");
        zombieTop1.add("[message] &x&F&B&9&C&0&8&lБОССЫ &8› &a{user} &fзанял &6&l1 место &fза убийство &x&f&f&d&7&0&0&lКОРОЛЯ ЗОМБИ&f!");
        config.set("zombie_king.top-1.commands", zombieTop1);

        List<String> zombieTop2 = new ArrayList<>();
        zombieTop2.add("eco give {user} 15000");
        zombieTop2.add("give {user} diamond 16");
        zombieTop2.add("crate givekey {user} rare 2");
        zombieTop2.add("[message] &x&F&B&9&C&0&8&lБОССЫ &8› &a{user} &fзанял &e&l2 место &fза убийство &x&f&f&d&7&0&0&lКОРОЛЯ ЗОМБИ&f!");
        config.set("zombie_king.top-2.commands", zombieTop2);

        List<String> zombieTop3 = new ArrayList<>();
        zombieTop3.add("eco give {user} 7500");
        zombieTop3.add("give {user} diamond 8");
        zombieTop3.add("crate givekey {user} rare 1");
        zombieTop3.add("[message] &x&F&B&9&C&0&8&lБОССЫ &8› &a{user} &fзанял &c&l3 место &fза убийство &x&f&f&d&7&0&0&lКОРОЛЯ ЗОМБИ&f!");
        config.set("zombie_king.top-3.commands", zombieTop3);

        List<String> skeletonTop1 = new ArrayList<>();
        skeletonTop1.add("eco give {user} 25000");
        skeletonTop1.add("give {user} diamond 24");
        skeletonTop1.add("crate givekey {user} rare 2");
        skeletonTop1.add("[message] &x&F&B&9&C&0&8&lБОССЫ &8› &a{user} &fзанял &6&l1 место &fза убийство &x&c&0&c&0&c&0&lПОВЕЛИТЕЛЯ СКЕЛЕТОВ&f!");
        config.set("skeleton_lord.top-1.commands", skeletonTop1);

        List<String> skeletonTop2 = new ArrayList<>();
        skeletonTop2.add("eco give {user} 12500");
        skeletonTop2.add("give {user} diamond 12");
        skeletonTop2.add("crate givekey {user} rare 1");
        skeletonTop2.add("[message] &x&F&B&9&C&0&8&lБОССЫ &8› &a{user} &fзанял &e&l2 место &fза убийство &x&c&0&c&0&c&0&lПОВЕЛИТЕЛЯ СКЕЛЕТОВ&f!");
        config.set("skeleton_lord.top-2.commands", skeletonTop2);

        List<String> skeletonTop3 = new ArrayList<>();
        skeletonTop3.add("eco give {user} 6000");
        skeletonTop3.add("give {user} diamond 6");
        skeletonTop3.add("[message] &x&F&B&9&C&0&8&lБОССЫ &8› &a{user} &fзанял &c&l3 место &fза убийство &x&c&0&c&0&c&0&lПОВЕЛИТЕЛЯ СКЕЛЕТОВ&f!");
        config.set("skeleton_lord.top-3.commands", skeletonTop3);

        List<String> witherTop1 = new ArrayList<>();
        witherTop1.add("eco give {user} 75000");
        witherTop1.add("give {user} diamond 64");
        witherTop1.add("give {user} netherite_ingot 8");
        witherTop1.add("crate givekey {user} legendary 5");
        witherTop1.add("[message] &x&F&B&9&C&0&8&lБОССЫ &8› &a{user} &fзанял &6&l1 место &fза убийство &x&8&b&0&0&f&f&lТЕМНОГО ИССУШИТЕЛЯ&f!");
        config.set("dark_wither.top-1.commands", witherTop1);

        List<String> witherTop2 = new ArrayList<>();
        witherTop2.add("eco give {user} 40000");
        witherTop2.add("give {user} diamond 32");
        witherTop2.add("give {user} netherite_ingot 4");
        witherTop2.add("crate givekey {user} legendary 3");
        witherTop2.add("[message] &x&F&B&9&C&0&8&lБОССЫ &8› &a{user} &fзанял &e&l2 место &fза убийство &x&8&b&0&0&f&f&lТЕМНОГО ИССУШИТЕЛЯ&f!");
        config.set("dark_wither.top-2.commands", witherTop2);

        List<String> witherTop3 = new ArrayList<>();
        witherTop3.add("eco give {user} 20000");
        witherTop3.add("give {user} diamond 16");
        witherTop3.add("give {user} netherite_ingot 2");
        witherTop3.add("crate givekey {user} legendary 1");
        witherTop3.add("[message] &x&F&B&9&C&0&8&lБОССЫ &8› &a{user} &fзанял &c&l3 место &fза убийство &x&8&b&0&0&f&f&lТЕМНОГО ИССУШИТЕЛЯ&f!");
        config.set("dark_wither.top-3.commands", witherTop3);

        List<String> giantTop1 = new ArrayList<>();
        giantTop1.add("eco give {user} 100000");
        giantTop1.add("give {user} diamond_block 16");
        giantTop1.add("give {user} netherite_ingot 16");
        giantTop1.add("crate givekey {user} legendary 10");
        giantTop1.add("[message] &x&F&B&9&C&0&8&lБОССЫ &8› &a{user} &fзанял &6&l1 место &fза убийство &x&5&1&c&f&6&6&lДРЕВНЕГО ГИГАНТА&f!");
        config.set("ancient_giant.top-1.commands", giantTop1);

        List<String> giantTop2 = new ArrayList<>();
        giantTop2.add("eco give {user} 50000");
        giantTop2.add("give {user} diamond_block 8");
        giantTop2.add("give {user} netherite_ingot 8");
        giantTop2.add("crate givekey {user} legendary 5");
        giantTop2.add("[message] &x&F&B&9&C&0&8&lБОССЫ &8› &a{user} &fзанял &e&l2 место &fза убийство &x&5&1&c&f&6&6&lДРЕВНЕГО ГИГАНТА&f!");
        config.set("ancient_giant.top-2.commands", giantTop2);

        List<String> giantTop3 = new ArrayList<>();
        giantTop3.add("eco give {user} 25000");
        giantTop3.add("give {user} diamond_block 4");
        giantTop3.add("give {user} netherite_ingot 4");
        giantTop3.add("crate givekey {user} legendary 2");
        giantTop3.add("[message] &x&F&B&9&C&0&8&lБОССЫ &8› &a{user} &fзанял &c&l3 место &fза убийство &x&5&1&c&f&6&6&lДРЕВНЕГО ГИГАНТА&f!");
        config.set("ancient_giant.top-3.commands", giantTop3);

        try {
            config.save(rewardsFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultBossbarConfig() {
        if (bossbarFile.exists()) return;

        YamlConfiguration config = new YamlConfiguration();

        config.set("enabled", true);
        config.set("title", "&x&F&B&9&C&0&8&l⚔ {name} &8| &c❤ {health}&7/&c{max-health} &8| &e{percent}% &8| &7⏱ {time-left}");
        config.set("color", "RED");
        config.set("style", "SEGMENTED_10");
        config.set("update-interval", 5);

        config.set("color-stages.high", "GREEN");
        config.set("color-stages.high-threshold", 70);
        config.set("color-stages.medium", "YELLOW");
        config.set("color-stages.medium-threshold", 40);
        config.set("color-stages.low", "RED");

        try {
            config.save(bossbarFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultMystDesignConfig() {
        if (mystDesignFile.exists()) return;

        YamlConfiguration config = new YamlConfiguration();

        config.set("prefix", "&#BE44FC&lМ&#B348FA&lИ&#A74CF8&lС&#9C50F6&lТ&#9054F4&lИ&#8557F1&lЧ&#795BEF&lЕ&#6E5FED&lС&#6263EB&lК&#5767E9&lИ&#5767E9&lЙ &#5767E9&lС&#5767E9&lУ&#5767E9&lН&#5767E9&lД&#5767E9&lУ&#5767E9&lК &8›&r ");

        config.set("inventory-title", "&#BE44FC&l✦ &#9C50F6&lМИСТИЧЕСКИЙ СУНДУК &#BE44FC&l✦");

        List<String> holoWaiting = new ArrayList<>();
        holoWaiting.add("&#BE44FC&l✦ &#9C50F6&lМИСТИЧЕСКИЙ СУНДУК &#BE44FC&l✦");
        holoWaiting.add("&7Нажмите &fПКМ &7чтобы активировать");
        holoWaiting.add("&#5767E9▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        config.set("hologram.waiting", holoWaiting);

        List<String> holoOpening = new ArrayList<>();
        holoOpening.add("&#BE44FC&l✦ &#9C50F6&lМИСТИЧЕСКИЙ СУНДУК &#BE44FC&l✦");
        holoOpening.add("&7Открытие через: &#BE44FC{time} &7секунд");
        holoOpening.add("&#5767E9▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        config.set("hologram.opening", holoOpening);

        List<String> holoOpened = new ArrayList<>();
        holoOpened.add("&#BE44FC&l✦ &#9C50F6&lСУНДУК ОТКРЫТ &#BE44FC&l✦");
        holoOpened.add("&7Забирайте добычу!");
        holoOpened.add("&#5767E9▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
        config.set("hologram.opened", holoOpened);

        config.set("hologram.height", 2.5);
        config.set("hologram.line-spacing", 0.3);

        List<String> spawnMsg = new ArrayList<>();
        spawnMsg.add("[message] &f");
        spawnMsg.add("[message]      {prefix}");
        spawnMsg.add("[message] &f");
        spawnMsg.add("[message]   &#BE44FC✦ &fМистический сундук появился в мире!");
        spawnMsg.add("[message]   &fКоординаты: &#9C50F6X: {x} Y: {y} Z: {z}");
        spawnMsg.add("[message]   &fИспользуйте &#BE44FC/event gps &fдля навигации");
        spawnMsg.add("[message] &f");
        spawnMsg.add("[sound] ENTITY_PLAYER_LEVELUP");
        config.set("messages.spawn", spawnMsg);

        List<String> openedMsg = new ArrayList<>();
        openedMsg.add("[message] {prefix}&aСундук открыт! Забирайте добычу!");
        openedMsg.add("[sound] BLOCK_CHEST_OPEN");
        config.set("messages.opened", openedMsg);

        List<String> despawnMsg = new ArrayList<>();
        despawnMsg.add("[message] {prefix}&cСундук исчез, так как его никто не открыл!");
        despawnMsg.add("[sound] ENTITY_ITEM_BREAK");
        config.set("messages.despawn", despawnMsg);

        config.set("messages.no-permission", "{prefix}&cУ вас нет прав для выполнения этой команды!");
        config.set("messages.config-reloaded", "{prefix}&aКонфигурация перезагружена!");
        config.set("messages.chest-spawning", "{prefix}&aСундук спавнится...");
        config.set("messages.chest-already-exists", "{prefix}&cСундук уже существует!");
        config.set("messages.no-chest-active", "{prefix}&cНет активного сундука!");
        config.set("messages.chest-removed", "{prefix}&aСундук удалён!");
        config.set("messages.chest-already-opening", "{prefix}&cСундук уже открывается!");
        config.set("messages.cannot-break-chest", "{prefix}&cВы не можете сломать этот сундук!");
        config.set("messages.rarity-not-found", "{prefix}&cРедкость &4{rarity} &cне найдена!");

        List<String> usageMsg = new ArrayList<>();
        usageMsg.add("{prefix}&#BE44FC&lИспользование команд:");
        usageMsg.add("");
        usageMsg.add("  &#9C50F6/myst reload &7- Перезагрузить конфигурацию");
        usageMsg.add("  &#9C50F6/myst spawn &7- Создать сундук");
        usageMsg.add("  &#9C50F6/myst remove &7- Удалить сундук");
        usageMsg.add("  &#9C50F6/myst admin inventory <редкость> &7- Редактор предметов");
        usageMsg.add("");
        config.set("commands.usage", usageMsg);

        try {
            config.save(mystDesignFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultMystSpawnConfig() {
        if (mystSpawnFile.exists()) return;

        YamlConfiguration config = new YamlConfiguration();

        config.set("world", "world");
        config.set("bounds.min-x", -1000);
        config.set("bounds.max-x", 1000);
        config.set("bounds.min-z", -1000);
        config.set("bounds.max-z", 1000);

        config.set("region.enabled", true);
        config.set("region.radius", 7);

        config.set("slots", 27);

        config.set("open-timer", 30);
        config.set("item-count", 9);
        config.set("despawn-time", 300);

        config.set("item-appearance.items-per-tick.min", 1);
        config.set("item-appearance.items-per-tick.max", 3);
        config.set("item-appearance.delay.min", 20);
        config.set("item-appearance.delay.max", 40);
        config.set("item-appearance.sound", "ENTITY_ITEM_PICKUP");

        try {
            config.save(mystSpawnFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultMystRaritiesConfig() {
        if (mystRaritiesFile.exists()) return;

        YamlConfiguration config = new YamlConfiguration();

        config.set("common.display-name", "&f&lОбычный");
        config.set("common.chance", 50.0);

        config.set("uncommon.display-name", "&a&lНеобычный");
        config.set("uncommon.chance", 25.0);

        config.set("rare.display-name", "&9&lРедкий");
        config.set("rare.chance", 15.0);

        config.set("epic.display-name", "&5&lЭпический");
        config.set("epic.chance", 7.0);

        config.set("legendary.display-name", "&6&lЛегендарный");
        config.set("legendary.chance", 3.0);

        try {
            config.save(mystRaritiesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultMystObfConfig() {
        if (mystObfFile.exists()) return;

        YamlConfiguration config = new YamlConfiguration();

        config.set("enabled", true);
        config.set("material", "PURPLE_STAINED_GLASS_PANE");

        List<String> lore = new ArrayList<>();
        lore.add("&7Редкость: {rarity}");
        lore.add("");
        lore.add("&#BE44FC► &fНажмите, чтобы раскрыть");
        config.set("lore", lore);

        try {
            config.save(mystObfFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultCastleConfig() {
        if (castleConfigFile.exists()) return;

        YamlConfiguration config = new YamlConfiguration();

        config.set("spawn.world", "world");
        config.set("spawn.x", 0.0);
        config.set("spawn.y", 0.0);
        config.set("spawn.z", 0.0);

        List<String> shulkerSpots = new ArrayList<>();
        shulkerSpots.add("spot1:23,78,-12");
        shulkerSpots.add("spot2:29,89,-15");
        shulkerSpots.add("spot3:35,90,-8");
        shulkerSpots.add("spot4:35,90,-32");
        shulkerSpots.add("spot5:28,89,-25");
        shulkerSpots.add("spot6:18,89,-25");
        shulkerSpots.add("spot7:11,90,-32");
        shulkerSpots.add("spot8:11,90,-8");
        shulkerSpots.add("spot9:6,70,-14");
        shulkerSpots.add("spot10:6,70,-26");
        shulkerSpots.add("spot11:40,70,-26");
        shulkerSpots.add("spot12:40,70,-14");
        config.set("shulker-spots", shulkerSpots);

        config.set("duration", 600);
        config.set("min-players", 3);

        config.set("prefix", "&#FF6B6B&lЗ&#FF5A5A&lА&#FF4949&lМ&#FF3838&lО&#FF2727&lК &8›&r ");

        List<String> spawnMsg = new ArrayList<>();
        spawnMsg.add("[message] &f");
        spawnMsg.add("[message]      {prefix}");
        spawnMsg.add("[message] &f");
        spawnMsg.add("[message]   &#FF6B6B✦ &fЗамок появился в мире!");
        spawnMsg.add("[message]   &fКоординаты: &#FF4949X: {x} Y: {y} Z: {z}");
        spawnMsg.add("[message]   &fУ вас есть &#FF6B6B10 минут &fна зачистку!");
        spawnMsg.add("[message] &f");
        spawnMsg.add("[sound] ENTITY_WITHER_SPAWN");
        config.set("messages.spawn", spawnMsg);

        List<String> despawnMsg = new ArrayList<>();
        despawnMsg.add("[message] {prefix}&cЗамок исчез, время вышло!");
        despawnMsg.add("[sound] ENTITY_WITHER_DEATH");
        config.set("messages.despawn", despawnMsg);

        config.set("messages.no-permission", "{prefix}&cУ вас нет прав для выполнения этой команды!");
        config.set("messages.config-reloaded", "{prefix}&aКонфигурация перезагружена!");
        config.set("messages.castle-spawning", "{prefix}&aЗамок спавнится...");
        config.set("messages.castle-already-exists", "{prefix}&cЗамок уже активен!");
        config.set("messages.no-castle-active", "{prefix}&cНет активного замка!");
        config.set("messages.castle-removed", "{prefix}&aЗамок удалён!");
        config.set("messages.rarity-not-found", "{prefix}&cРедкость &4{rarity} &cне найдена!");
        config.set("messages.shulker-broken", "{prefix}&aШалкер сломан! Осталось ударов: {hits}");
        config.set("messages.shulker-opened", "{prefix}&6Шалкер открыт! Забирайте добычу!");

        List<String> usageMsg = new ArrayList<>();
        usageMsg.add("{prefix}&#FF6B6B&lИспользование команд:");
        usageMsg.add("");
        usageMsg.add("  &#FF4949/castle reload &7- Перезагрузить конфигурацию");
        usageMsg.add("  &#FF4949/castle spawn &7- Создать замок");
        usageMsg.add("  &#FF4949/castle remove &7- Удалить замок");
        usageMsg.add("  &#FF4949/castle admin inventory <редкость> &7- Редактор предметов");
        usageMsg.add("");
        config.set("commands.usage", usageMsg);

        try {
            config.save(castleConfigFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultCastleRaritiesConfig() {
        if (castleRaritiesFile.exists()) return;

        YamlConfiguration config = new YamlConfiguration();

        config.set("common.display-name", "&f&lОбычный");
        config.set("common.color", "WHITE");
        config.set("common.chance", 40.0);
        config.set("common.shulker-hits", 3);

        config.set("uncommon.display-name", "&a&lНеобычный");
        config.set("uncommon.color", "GREEN");
        config.set("uncommon.chance", 30.0);
        config.set("uncommon.shulker-hits", 4);

        config.set("rare.display-name", "&9&lРедкий");
        config.set("rare.color", "BLUE");
        config.set("rare.chance", 18.0);
        config.set("rare.shulker-hits", 5);

        config.set("epic.display-name", "&5&lЭпический");
        config.set("epic.color", "PURPLE");
        config.set("epic.chance", 9.0);
        config.set("epic.shulker-hits", 7);

        config.set("legendary.display-name", "&6&lЛегендарный");
        config.set("legendary.color", "GOLD");
        config.set("legendary.chance", 3.0);
        config.set("legendary.shulker-hits", 10);

        try {
            config.save(castleRaritiesFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createDefaultEventConfig() {
        if (eventFile.exists()) return;

        YamlConfiguration config = new YamlConfiguration();

        config.set("prefix", "&#FFD700&lС&#FFC800&lО&#FFB900&lБ&#FFAA00&lЫ&#FF9B00&lТ&#FF8C00&lИ&#FF7D00&lЯ &8›&r ");

        config.set("auto-spawn.enabled", true);
        config.set("auto-spawn.interval", 3600);
        config.set("min-online-to-spawn", 1);

        config.set("events.bosses.enabled", true);
        config.set("events.myst.enabled", true);
        config.set("events.castle.enabled", true);

        List<String> delayWaiting = new ArrayList<>();
        delayWaiting.add("&f");
        delayWaiting.add("     &#FFD700&l⏰ СОБЫТИЯ ⏰");
        delayWaiting.add("&f");
        delayWaiting.add("  &fСейчас нет активных событий");
        delayWaiting.add("&f");
        delayWaiting.add("  &fДо следующего события: &#FFD700{time-left}");
        delayWaiting.add("&f");
        config.set("messages.delay-waiting", delayWaiting);

        List<String> delayMystActive = new ArrayList<>();
        delayMystActive.add("&f");
        delayMystActive.add("     &#BE44FC&l✦ МИСТИЧЕСКИЙ СУНДУК ✦");
        delayMystActive.add("&f");
        delayMystActive.add("  &fСундук активен!");
        delayMystActive.add("  &fКоординаты: &#9C50F6X: {x} Y: {y} Z: {z}");
        delayMystActive.add("  &fВремя до исчезновения: &#BE44FC{despawn-time}");
        delayMystActive.add("&f");
        delayMystActive.add("  &fИспользуйте &#BE44FC/event gps &fдля навигации");
        delayMystActive.add("&f");
        config.set("messages.delay-myst-active", delayMystActive);

        List<String> delayBossActive = new ArrayList<>();
        delayBossActive.add("&f");
        delayBossActive.add("     &#FF6B6B&l⚔ БОСС ⚔");
        delayBossActive.add("&f");
        delayBossActive.add("  &fБосс: {name}");
        delayBossActive.add("  &fВремя до исчезновения: &#FF6B6B{despawn-time}");
        delayBossActive.add("&f");
        delayBossActive.add("  &fОтправляйтесь на арену: &#FF6B6B/warp boss");
        delayBossActive.add("&f");
        config.set("messages.delay-boss-active", delayBossActive);

        List<String> delayCastleActive = new ArrayList<>();
        delayCastleActive.add("&f");
        delayCastleActive.add("     &#FF6B6B&l🏰 ЗАМОК 🏰");
        delayCastleActive.add("&f");
        delayCastleActive.add("  &fЗамок активен!");
        delayCastleActive.add("  &fКоординаты: &#FF4949X: {x} Y: {y} Z: {z}");
        delayCastleActive.add("  &fВремя до исчезновения: &#FF6B6B{despawn-time}");
        delayCastleActive.add("&f");
        config.set("messages.delay-castle-active", delayCastleActive);

        config.set("messages.no-myst-active", "{prefix}&cСейчас нет активного мистического сундука!");
        config.set("messages.no-compass", "{prefix}&cУ вас нет компаса в инвентаре!");
        config.set("messages.gps-activated", "{prefix}&aКомпас теперь указывает на сундук!");

        List<String> usageMsg = new ArrayList<>();
        usageMsg.add("{prefix}&#FFD700&lИспользование команд:");
        usageMsg.add("");
        usageMsg.add("  &#FFD700/event delay &7- Информация о событиях");
        usageMsg.add("  &#FFD700/event gps &7- Навигация к сундуку");
        usageMsg.add("");
        config.set("commands.usage", usageMsg);

        try {
            config.save(eventFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfigs() {
        bossesConfig = YamlConfiguration.loadConfiguration(bossesFile);
        designConfig = YamlConfiguration.loadConfiguration(designFile);
        spawnConfig = YamlConfiguration.loadConfiguration(spawnFile);
        rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);
        bossbarConfig = YamlConfiguration.loadConfiguration(bossbarFile);

        mystDesignConfig = YamlConfiguration.loadConfiguration(mystDesignFile);
        mystSpawnConfig = YamlConfiguration.loadConfiguration(mystSpawnFile);
        mystRaritiesConfig = YamlConfiguration.loadConfiguration(mystRaritiesFile);
        mystObfConfig = YamlConfiguration.loadConfiguration(mystObfFile);

        castleConfig = YamlConfiguration.loadConfiguration(castleConfigFile);
        castleRaritiesConfig = YamlConfiguration.loadConfiguration(castleRaritiesFile);

        eventConfig = YamlConfiguration.loadConfiguration(eventFile);
    }

    public FileConfiguration getBossesConfig() {
        return bossesConfig;
    }

    public FileConfiguration getDesignConfig() {
        return designConfig;
    }

    public FileConfiguration getSpawnConfig() {
        return spawnConfig;
    }

    public FileConfiguration getRewardsConfig() {
        return rewardsConfig;
    }

    public FileConfiguration getBossbarConfig() {
        return bossbarConfig;
    }

    public FileConfiguration getMystDesignConfig() {
        return mystDesignConfig;
    }

    public FileConfiguration getMystSpawnConfig() {
        return mystSpawnConfig;
    }

    public FileConfiguration getMystRaritiesConfig() {
        return mystRaritiesConfig;
    }

    public FileConfiguration getMystObfConfig() {
        return mystObfConfig;
    }

    public FileConfiguration getCastleConfig() {
        return castleConfig;
    }

    public FileConfiguration getCastleRaritiesConfig() {
        return castleRaritiesConfig;
    }

    public FileConfiguration getEventConfig() {
        return eventConfig;
    }

    public String getPrefix() {
        return designConfig.getString("prefix", "&6&lБОССЫ &8›&r ");
    }

    public String getMystPrefix() {
        return mystDesignConfig.getString("prefix", "&#BE44FC&lСУНДУК &8›&r ");
    }

    public String getCastlePrefix() {
        return castleConfig.getString("prefix", "&#FF6B6B&lЗАМОК &8›&r ");
    }

    public String getEventPrefix() {
        return eventConfig.getString("prefix", "&#FFD700&lСОБЫТИЯ &8›&r ");
    }

    public String getMessage(String path) {
        String message = designConfig.getString("messages." + path, "");
        return message.replace("{prefix}", getPrefix());
    }

    public String getMystMessage(String path) {
        String message = mystDesignConfig.getString("messages." + path, "");
        return message.replace("{prefix}", getMystPrefix());
    }

    public String getCastleMessage(String path) {
        String message = castleConfig.getString("messages." + path, "");
        return message.replace("{prefix}", getCastlePrefix());
    }

    public String getEventMessage(String path) {
        String message = eventConfig.getString("messages." + path, "");
        return message.replace("{prefix}", getEventPrefix());
    }

    public List<String> getMessageList(String path) {
        List<String> messages = designConfig.getStringList("messages." + path);
        List<String> result = new ArrayList<>();
        String prefix = getPrefix();
        for (String msg : messages) {
            result.add(msg.replace("{prefix}", prefix));
        }
        return result;
    }

    public List<String> getMystMessageList(String path) {
        List<String> messages = mystDesignConfig.getStringList("messages." + path);
        List<String> result = new ArrayList<>();
        String prefix = getMystPrefix();
        for (String msg : messages) {
            result.add(msg.replace("{prefix}", prefix));
        }
        return result;
    }

    public List<String> getCastleMessageList(String path) {
        List<String> messages = castleConfig.getStringList("messages." + path);
        List<String> result = new ArrayList<>();
        String prefix = getCastlePrefix();
        for (String msg : messages) {
            result.add(msg.replace("{prefix}", prefix));
        }
        return result;
    }

    public List<String> getEventMessageList(String path) {
        List<String> messages = eventConfig.getStringList("messages." + path);
        List<String> result = new ArrayList<>();
        String prefix = getEventPrefix();
        for (String msg : messages) {
            result.add(msg.replace("{prefix}", prefix));
        }
        return result;
    }

    public List<String> getCommandUsage() {
        List<String> usage = designConfig.getStringList("commands.usage");
        List<String> result = new ArrayList<>();
        String prefix = getPrefix();
        for (String msg : usage) {
            result.add(msg.replace("{prefix}", prefix));
        }
        return result;
    }

    public List<String> getMystCommandUsage() {
        List<String> usage = mystDesignConfig.getStringList("commands.usage");
        List<String> result = new ArrayList<>();
        String prefix = getMystPrefix();
        for (String msg : usage) {
            result.add(msg.replace("{prefix}", prefix));
        }
        return result;
    }

    public List<String> getCastleCommandUsage() {
        List<String> usage = castleConfig.getStringList("commands.usage");
        List<String> result = new ArrayList<>();
        String prefix = getCastlePrefix();
        for (String msg : usage) {
            result.add(msg.replace("{prefix}", prefix));
        }
        return result;
    }

    public List<String> getEventCommandUsage() {
        List<String> usage = eventConfig.getStringList("commands.usage");
        List<String> result = new ArrayList<>();
        String prefix = getEventPrefix();
        for (String msg : usage) {
            result.add(msg.replace("{prefix}", prefix));
        }
        return result;
    }
}