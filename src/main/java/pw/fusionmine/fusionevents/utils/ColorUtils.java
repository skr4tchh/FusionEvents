package pw.fusionmine.fusionevents.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern HEX_PATTERN_2 = Pattern.compile("§x(§[A-Fa-f0-9]){6}");
    private static final Pattern HEX_PATTERN_3 = Pattern.compile("&x(&[A-Fa-f0-9]){6}");

    public static String colorize(String message) {
        if (message == null) return "";

        Matcher matcher3 = HEX_PATTERN_3.matcher(message);
        StringBuffer buffer3 = new StringBuffer();
        while (matcher3.find()) {
            String hex = matcher3.group().replace("&x", "").replace("&", "");
            matcher3.appendReplacement(buffer3, ChatColor.of("#" + hex).toString());
        }
        message = matcher3.appendTail(buffer3).toString();

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + group).toString());
        }
        message = matcher.appendTail(buffer).toString();

        message = message.replace("§x", "&x");
        Matcher matcher4 = Pattern.compile("§x(§[A-Fa-f0-9]){6}").matcher(message);
        StringBuffer buffer4 = new StringBuffer();
        while (matcher4.find()) {
            String hex = matcher4.group().replace("§x", "").replace("§", "");
            matcher4.appendReplacement(buffer4, ChatColor.of("#" + hex).toString());
        }
        message = matcher4.appendTail(buffer4).toString();

        return ChatColor.translateAlternateColorCodes('&', message);
    }
}