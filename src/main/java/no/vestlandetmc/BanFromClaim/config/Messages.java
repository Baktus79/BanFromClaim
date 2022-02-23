package no.vestlandetmc.BanFromClaim.config;

public class Messages extends ConfigHandler {

	private Messages(String fileName) {
		super(fileName);
	}

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

	private void onLoad() {

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

	public static void initialize() {
		new Messages("messages.yml").onLoad();
	}

	public static String placeholders(String message, String target, String source, String claimowner) {
		final String converted = message.
				replaceAll("%target%", target).
				replaceAll("%source%", source).
				replaceAll("%claimowner%", claimowner);

		return converted;

	}

}
