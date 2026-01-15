package no.vestlandetmc.BanFromClaim.config;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public final class Messages {

	private static File file;
	private static FileConfiguration cfg;

	public static String
			UNVALID_PLAYERNAME,
			OUTSIDE_CLAIM,
			NO_ARGUMENTS,
			BAN_SELF,
			BAN_OWNER,
			KICK_SELF,
			KICK_OWNER,
			PROTECTED,
			NO_ACCESS,
			BANNED,
			BANNED_TARGET,
			ALREADY_BANNED,
			KICKED,
			KICKED_TARGET,
			UNBANNED,
			UNBANNED_TARGET,
			NOT_BANNED,
			UNVALID_NUMBER,
			LIST_HEADER,
			LIST_EMPTY,
			TITLE_MESSAGE,
			SUBTITLE_MESSAGE,
			BAN_ALL,
			UNBAN_ALL,
			LIST_BAN_ALL;

	private Messages() {}

	public static void initialize() {
		loadFromDisk();
		applyValues();
	}

	public static void reload() {
		loadFromDisk();
		applyValues();
	}

	private static void loadFromDisk() {
		if (!BfcPlugin.getPlugin().getDataFolder().exists()) {
			//noinspection ResultOfMethodCallIgnored
			BfcPlugin.getPlugin().getDataFolder().mkdirs();
		}

		file = new File(BfcPlugin.getPlugin().getDataFolder(), "messages.yml");

		// If missing, copy default from jar
		if (!file.exists()) {
			BfcPlugin.getPlugin().saveResource("messages.yml", false);
		}

		cfg = YamlConfiguration.loadConfiguration(file);
	}

	private static void applyValues() {
		UNVALID_PLAYERNAME = getString("unvalid-playername");
		OUTSIDE_CLAIM = getString("outside-claim");
		NO_ARGUMENTS = getString("no-arguments");
		BAN_SELF = getString("ban-self");
		BAN_OWNER = getString("ban-owner");
		KICK_SELF = getString("kick-self");
		KICK_OWNER = getString("kick-owner");
		PROTECTED = getString("protected");
		NO_ACCESS = getString("no-access");
		BANNED = getString("banned");
		BANNED_TARGET = getString("banned-target");
		KICKED = getString("kicked");
		KICKED_TARGET = getString("kicked-target");
		ALREADY_BANNED = getString("already-banned");
		UNBANNED = getString("unbanned");
		UNBANNED_TARGET = getString("unbanned-target");
		NOT_BANNED = getString("not-banned");
		UNVALID_NUMBER = getString("unvalid-number");
		LIST_HEADER = getString("list-header");
		LIST_EMPTY = getString("list-empty");
		TITLE_MESSAGE = getString("title-message");
		SUBTITLE_MESSAGE = getString("subtitle-message");
		BAN_ALL = getString("ban-all");
		UNBAN_ALL = getString("unban-all");
		LIST_BAN_ALL = getString("list-ban-all");
	}

	private static String getString(String path) {
		if (cfg == null) {
			return "&c(messages.yml not loaded) " + path;
		}
		final String val = cfg.getString(path);
		return (val == null) ? ("&cMissing message: " + path) : val;
	}

	public static String placeholders(String message, String target, String source, String claimowner) {
		if (message == null) return "";
		return message
				.replace("%target%", target == null ? "" : target)
				.replace("%source%", source == null ? "" : source)
				.replace("%claimowner%", claimowner == null ? "" : claimowner);
	}
}