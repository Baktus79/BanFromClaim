package no.vestlandetmc.BanFromClaim.config;

public class Config extends ConfigHandler {

	private Config(String fileName) {
		super(fileName);
	}

	public static long
	COMBAT_TIME;

	public static boolean
	COMBAT_ENABLED,
	TIMER_ENABLED;

	private void onLoad() {

		COMBAT_TIME = getLong("combatmode.time");
		COMBAT_ENABLED = getBoolean("combatmode.enabled");
		TIMER_ENABLED = getBoolean("combatmode.timer-enabled");

	}

	public static void initialize() {
		new Config("config.yml").onLoad();
	}
}
