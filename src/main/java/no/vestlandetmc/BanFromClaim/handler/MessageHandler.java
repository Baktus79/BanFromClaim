package no.vestlandetmc.BanFromClaim.handler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MessageHandler {

	public static ArrayList<String> spamMessageClaim = new ArrayList<>();

	public static void sendAction(Player player, String message) {
		final Component text = colorize(message);
		player.sendActionBar(text);
	}

	public static void sendTitle(Player player, String maintitle, String subtitle) {
		final Component main = colorize(maintitle);
		final Component sub = colorize(subtitle);
		Title title = Title.title(main, sub);
		player.showTitle(title);
	}

	public static void sendMessage(Player player, String message) {
		final Component text = colorize(message);
		player.sendMessage(text);
	}

	public static void sendConsole(String... messages) {
		for (final String message : messages) {
			BfcPlugin.getPlugin().getServer().getConsoleSender().sendMessage(colorize(message));
		}
	}

	public static @NotNull TextComponent colorize(String message) {
		return LegacyComponentSerializer.legacy('&').deserialize(message);
	}

	public static String compToString(Component component) {
		return PlainTextComponentSerializer.plainText().serialize(component);
	}

	public Component miniMessage(String minimessage) {
		final MiniMessage miniMessage = MiniMessage.miniMessage();
		return miniMessage.deserialize(minimessage);
	}
}
