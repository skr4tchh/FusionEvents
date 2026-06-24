package pw.fusionmine.fusionevents.utils;

import de.oliver.fancyholograms.api.FancyHologramsPlugin;
import de.oliver.fancyholograms.api.HologramManager;
import de.oliver.fancyholograms.api.data.TextHologramData;
import de.oliver.fancyholograms.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class HologramUtils {

    private static HologramManager hologramManager = FancyHologramsPlugin.get().getHologramManager();

    public static String createHologram(Location location, List<String> lines) {
        String hologramHash = "FusionEvents_" + UUID.randomUUID().toString().substring(0, 8);
        createHologram(hologramHash, location, lines);
        return hologramHash;
    }

    public static void createHologram(String name, Location location, List<String> lines) {
        TextHologramData data = new TextHologramData(name, location);

        data.setText(lines);
        data.setTextShadow(true);
        data.setPersistent(false);
        data.setBillboard(Display.Billboard.VERTICAL);
        data.setBackground(Color.fromARGB(0, 0, 0, 0));

        Hologram hologram = hologramManager.create(data);

        hologramManager.addHologram(hologram);
        hologram.createHologram();
    }

    public static void removeHologram(String name) {
        Hologram hologram = hologramManager.getHologram(name).orElse(null);
        if (hologram != null) {
            hologramManager.removeHologram(hologram);
            var viewers = hologram.getViewers()
                    .stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .toList();
            viewers.forEach(p -> hologram.hideHologram(p));
        }
    }

    public static void updateHologramText(String name, List<String> newLines) {
        Hologram hologram = hologramManager.getHologram(name).orElse(null);

        if (hologram != null && hologram.getData() instanceof TextHologramData textData) {
            textData.setText(newLines);
            hologram.forceUpdate();
        }
    }

}
