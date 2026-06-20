package pw.fusionmine.fusionevents;

import org.bukkit.plugin.java.JavaPlugin;
import pw.fusionmine.fusionevents.commands.BossesCommand;
import pw.fusionmine.fusionevents.commands.CastleCommand;
import pw.fusionmine.fusionevents.commands.EventCommand;
import pw.fusionmine.fusionevents.commands.MystCommand;
import pw.fusionmine.fusionevents.listeners.BossListener;
import pw.fusionmine.fusionevents.listeners.CastleListener;
import pw.fusionmine.fusionevents.listeners.MystListener;
import pw.fusionmine.fusionevents.managers.BossManager;
import pw.fusionmine.fusionevents.managers.CastleManager;
import pw.fusionmine.fusionevents.managers.ConfigManager;
import pw.fusionmine.fusionevents.managers.EventManager;
import pw.fusionmine.fusionevents.managers.MystManager;

public final class FusionEvents extends JavaPlugin {

    private ConfigManager configManager;
    private BossManager bossManager;
    private MystManager mystManager;
    private CastleManager castleManager;
    private EventManager eventManager;

    @Override
    public void onEnable() {

        configManager = new ConfigManager(this);
        configManager.loadConfigs();

        bossManager = new BossManager(this);
        mystManager = new MystManager(this);
        castleManager = new CastleManager(this);
        eventManager = new EventManager(this);

        getCommand("bosses").setExecutor(new BossesCommand(this));
        getCommand("myst").setExecutor(new MystCommand(this));
        getCommand("castle").setExecutor(new CastleCommand(this));
        getCommand("event").setExecutor(new EventCommand(this));

        getServer().getPluginManager().registerEvents(new BossListener(this), this);
        getServer().getPluginManager().registerEvents(new MystListener(this), this);
        getServer().getPluginManager().registerEvents(new CastleListener(this), this);

        bossManager.startAuraTasks();
        mystManager.startGpsTasks();
        eventManager.startAutoSpawn();
    }

    @Override
    public void onDisable() {
        if (eventManager != null) {
            eventManager.stopAutoSpawn();
        }
        if (bossManager != null) {
            bossManager.stopTasks();
            bossManager.removeAllBosses();
        }
        if (mystManager != null) {
            mystManager.stopTasks();
            mystManager.removeChest();
        }
        if (castleManager != null) {
            castleManager.removeCastle();
        }
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public BossManager getBossManager() {
        return bossManager;
    }

    public MystManager getMystManager() {
        return mystManager;
    }

    public CastleManager getCastleManager() {
        return castleManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

}