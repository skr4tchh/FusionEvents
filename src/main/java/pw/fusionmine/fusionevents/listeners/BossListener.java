package pw.fusionmine.fusionevents.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import pw.fusionmine.fusionevents.FusionEvents;

public class BossListener implements Listener {

    private final FusionEvents plugin;

    public BossListener(FusionEvents plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.getBossManager().hasActiveBoss()) {
            plugin.getBossManager().addPlayerToBossBar(event.getPlayer());
        }
    }

    @EventHandler
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;

        LivingEntity boss = plugin.getBossManager().getCurrentBoss();
        if (boss == null || !event.getEntity().equals(boss)) return;

        Player damager = null;

        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else if (event.getDamager() instanceof org.bukkit.entity.Projectile) {
            org.bukkit.entity.Projectile projectile = (org.bukkit.entity.Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                damager = (Player) projectile.getShooter();
            }
        }

        if (damager != null) {
            plugin.getBossManager().addDamage(damager.getUniqueId(), event.getFinalDamage());
        }

        plugin.getBossManager().updateBossBar();
    }

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        LivingEntity boss = plugin.getBossManager().getCurrentBoss();
        if (boss == null || !event.getEntity().equals(boss)) return;

        plugin.getBossManager().broadcastDeathMessage();
        plugin.getBossManager().clearBoss();
    }

}