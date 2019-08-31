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
	PROTECTED,
	NO_ACCESS,
	BANNED,
	BANNED_TARGET,
	ALREADY_BANNED,
	UNBANNED,
	UNBANNED_TARGET,
	NOT_BANNED,
	UNVALID_NUMBER,
	LIST_HEADER,
	LIST_EMPTY;

	private void onLoad() {

		UNVALID_PLAYERNAME = getString("unvalid-playername");
		OUTSIDE_CLAIM = getString("outside-claim");
		NO_ARGUMENTS = getString("no-arguments");
		BAN_SELF = getString("ban-self");
		PROTECTED = getString("protected");
		NO_ACCESS = getString("no-access");
		BANNED = getString("banned");
		BANNED_TARGET = getString("banned-target");
		ALREADY_BANNED = getString("already-banned");
		UNBANNED = getString("unbanned");
		UNBANNED_TARGET = getString("unbanned-target");
		NOT_BANNED = getString("not-banned");
		UNVALID_NUMBER = getString("unvalid-number");
		LIST_HEADER = getString("list-header");
		LIST_EMPTY = getString("list-empty");

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
