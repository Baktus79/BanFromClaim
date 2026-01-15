package no.vestlandetmc.BanFromClaim.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Central config values (loaded from config.yml).
 *
 * IMPORTANT:
 * This project uses ConfigHandler defaults-from-file behavior.
 * Do NOT pass non-null defaults into getString/getInt/getBoolean/etc.
 */
public class Config extends ConfigHandler {

	private static final String CONFIG_FILE = "config.yml";

	private Config(String fileName) {
		super(fileName);
	}

	public enum TeleportMode {
		SAFE_LOCATION,
		NEAREST_SAFE,
		PUSH_BACK;

		public static TeleportMode fromString(String raw) {
			if (raw == null) return SAFE_LOCATION;
			try {
				return TeleportMode.valueOf(raw.trim().toUpperCase());
			} catch (IllegalArgumentException ex) {
				return SAFE_LOCATION;
			}
		}
	}

	// Existing settings (used elsewhere)
	public static long COMBAT_TIME;

	public static boolean
			COMBAT_ENABLED,
			TIMER_ENABLED,
			KICKMODE;

	// New settings
	public static TeleportMode TELEPORT_MODE;

	/** Re-check interval for "standing still" enforcement (ticks). */
	public static int ENFORCE_INTERVAL_TICKS;

	/** Set via /bfcsafespot or config. */
	public static Location SAFE_LOCATION;

	private void onLoad() {

		// Existing settings (fallbacks applied in Java, NOT passed into getX())
		COMBAT_TIME = Math.max(1L, readLong("combatmode.time", 15L));
		COMBAT_ENABLED = readBoolean("combatmode.enabled", false);
		TIMER_ENABLED = readBoolean("combatmode.timer-enabled", true);
		KICKMODE = readBoolean("kickmode", true);

		// New settings (safe fallbacks)
		TELEPORT_MODE = TeleportMode.fromString(readString("teleport.mode", TeleportMode.SAFE_LOCATION.name()));
		ENFORCE_INTERVAL_TICKS = Math.max(1, readInt("teleport.enforce-interval-ticks", 20));

		loadSafeLocation();
	}

	private void loadSafeLocation() {
		if (!contains("teleport.safelocation")) {
			SAFE_LOCATION = null;
			return;
		}

		final String worldName = readString("teleport.safelocation.world", null);
		final World world = (worldName == null) ? null : Bukkit.getWorld(worldName);

		if (world == null) {
			Bukkit.getLogger().warning("[BanFromClaim] teleport.safelocation.world is not loaded/found: " + worldName);
			SAFE_LOCATION = null;
			return;
		}

		final double x = readDouble("teleport.safelocation.x", world.getSpawnLocation().getX());
		final double y = readDouble("teleport.safelocation.y", world.getSpawnLocation().getY());
		final double z = readDouble("teleport.safelocation.z", world.getSpawnLocation().getZ());

		final float yaw = (float) readDouble("teleport.safelocation.yaw", 0D);
		final float pitch = (float) readDouble("teleport.safelocation.pitch", 0D);

		SAFE_LOCATION = new Location(world, x, y, z, yaw, pitch);
	}

	public static void initialize() {
		new Config(CONFIG_FILE).onLoad();
	}

	/**
	 * Returns configured safelocation if set, otherwise world spawn.
	 * Always returns a clone to avoid accidental mutation.
	 */
	public static Location getBannedTeleportTarget(World fallbackWorld) {
		final World world = (fallbackWorld != null) ? fallbackWorld : Bukkit.getWorlds().get(0);

		if (SAFE_LOCATION == null) {
			return world.getSpawnLocation().clone();
		}

		return SAFE_LOCATION.clone();
	}

	public static void setSafespot(Location loc) {
		if (loc == null || loc.getWorld() == null) return;

		final ConfigHandler cfg = new ConfigHandler(CONFIG_FILE);

		cfg.set("teleport.safelocation.x", loc.getX());
		cfg.set("teleport.safelocation.y", loc.getY());
		cfg.set("teleport.safelocation.z", loc.getZ());
		cfg.set("teleport.safelocation.yaw", loc.getYaw());
		cfg.set("teleport.safelocation.pitch", loc.getPitch());
		cfg.set("teleport.safelocation.world", loc.getWorld().getName());
		cfg.saveConfig();

		SAFE_LOCATION = loc.clone();
	}

	// -------------------------
	// Safe readers (no defaults passed into ConfigHandler getters)
	// -------------------------

	private String readString(String path, String fallback) {
		if (!contains(path)) return fallback;
		final String val = getString(path);
		return (val == null || val.isBlank()) ? fallback : val;
	}

	private boolean readBoolean(String path, boolean fallback) {
		if (!contains(path)) return fallback;
		return getBoolean(path);
	}

	private int readInt(String path, int fallback) {
		if (!contains(path)) return fallback;
		return getInt(path);
	}

	private long readLong(String path, long fallback) {
		if (!contains(path)) return fallback;
		return getLong(path);
	}

	private double readDouble(String path, double fallback) {
		if (!contains(path)) return fallback;
		return getDouble(path);
	}
}