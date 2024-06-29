package no.vestlandetmc.BanFromClaim.handler;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class MessageHandler {

	public static ArrayList<String> spamMessageClaim = new ArrayList<>();

	public static void sendAction(Player player, String message) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(colorize(message)));
	}

	public static void sendTitle(Player player, String title, String subtitle) {
		player.sendTitle(colorize(title), colorize(subtitle), 20, 3 * 20, 10);
	}

	public static void sendTitle(Player player, String title, String subtitle, int stay) {
		player.sendTitle(colorize(title), colorize(subtitle), 20, stay * 20, 10);
	}

	public static void sendMessage(Player player, String... messages) {
		for (final String message : messages) {
			player.sendMessage(colorize(message));
		}
	}

	public static void sendAnnounce(String... messages) {
		for (final Player player : Bukkit.getOnlinePlayers()) {
			for (final String message : messages) {
				player.sendMessage(colorize(message));
			}
		}
	}

	public static void sendConsole(String... messages) {
		for (final String message : messages) {
			BfcPlugin.getPlugin().getServer().getConsoleSender().sendMessage(colorize(message));
		}
	}

	public static String colorize(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public static String placeholders(String message, String time, String tpName, String tp1, String tp2) {

		return message.
				replaceAll("%time%", time).
				replaceAll("%tpname%", tpName).
				replaceAll("%teleport1%", tp1).
				replaceAll("%teleport2%", tp2);

	}

}
