package pw.fusionmine.fusionevents.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Arrays;
import java.util.HashSet;

public class WorldGuardUtils {

    public static void createMystRegion(Location center, int radius, String regionName) {
        try {
            World world = center.getWorld();
            if (world == null) return;

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(world));

            if (regionManager == null) return;

            int minX = center.getBlockX() - radius;
            int minY = 0;
            int minZ = center.getBlockZ() - radius;
            int maxX = center.getBlockX() + radius;
            int maxY = 255;
            int maxZ = center.getBlockZ() + radius;

            BlockVector3 min = BlockVector3.at(minX, minY, minZ);
            BlockVector3 max = BlockVector3.at(maxX, maxY, maxZ);

            ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionName, min, max);

            region.setFlag(Flags.PVP, StateFlag.State.ALLOW);
            region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
            region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
            region.setFlag(Flags.TNT, StateFlag.State.DENY);
            region.setFlag(Flags.CREEPER_EXPLOSION, StateFlag.State.DENY);
            region.setFlag(Flags.ENDER_BUILD, StateFlag.State.DENY);
            region.setFlag(Flags.FIRE_SPREAD, StateFlag.State.DENY);
            region.setFlag(Flags.LAVA_FIRE, StateFlag.State.DENY);
            region.setFlag(Flags.MOB_SPAWNING, StateFlag.State.DENY);
            region.setFlag(Flags.INVINCIBILITY, StateFlag.State.DENY);
            region.setFlag(Flags.BLOCKED_CMDS, new HashSet<>(Arrays.asList(
                    "/fly"
            )));

            Flag<?> flyFlag = WorldGuard.getInstance().getFlagRegistry().get("fly");
            if (flyFlag instanceof StateFlag) {
                region.setFlag((StateFlag) flyFlag, StateFlag.State.DENY);
            }

            region.setPriority(100);

            regionManager.addRegion(region);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeMystRegion(World world, String regionName) {

        try {
            if (world == null) return;

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionManager regionManager = container.get(BukkitAdapter.adapt(world));

            if (regionManager == null) return;

            ProtectedRegion region = regionManager.getRegion(regionName);
            if (region != null) {
                regionManager.removeRegion(regionName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}