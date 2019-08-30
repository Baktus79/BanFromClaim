package no.vestlandetmc.BanFromClaim.handler;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import no.vestlandetmc.BanFromClaim.BfcPlugin;

public class MessageHandler {

	public static void sendAction(Player player, String message) {
		player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(colorize(message)));
	}

	public static void sendMessage(Player player, String message) {
		player.sendMessage(colorize(message));
	}

	public static void sendConsole(String message) {
		BfcPlugin.getInstance().getServer().getConsoleSender().sendMessage(colorize(message));
	}

	public static String colorize(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public static String placeholders(String message, String time, String tpName, String tp1, String tp2) {
		final String converted = message.
				replaceAll("%time%", time).
				replaceAll("%tpname%", tpName).
				replaceAll("%teleport1%", tp1).
				replaceAll("%teleport2%", tp2);

		return converted;

	}

}
