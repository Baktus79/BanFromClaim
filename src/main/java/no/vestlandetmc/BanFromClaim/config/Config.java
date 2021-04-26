package no.vestlandetmc.BanFromClaim.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Config extends ConfigHandler {

	private Config(String fileName) {
		super(fileName);
	}

	public static long
	COMBAT_TIME;

	public static boolean
	COMBAT_ENABLED,
	TIMER_ENABLED;

	public static Location
	SAFE_LOCATION;

	private void onLoad() {

		double locX;
		double locY;
		double locZ;
		float yaw;
		String worldName;
		World world;

		if(contains("teleport.safelocation")) {
			locX = getDouble("teleport.safelocation.x");
			locY = getDouble("teleport.safelocation.y");
			locZ = getDouble("teleport.safelocation.z");
			yaw = getInt("teleport.safelocation.yaw");
			worldName = getString("teleport.safelocation.world");
			world = Bukkit.getWorld(worldName);

			SAFE_LOCATION = new Location(world, locX, locY, locZ, yaw, 0F);

		} else {
			SAFE_LOCATION = null;
		}

		COMBAT_TIME = getLong("combatmode.time");
		COMBAT_ENABLED = getBoolean("combatmode.enabled");
		TIMER_ENABLED = getBoolean("combatmode.timer-enabled");

	}

	public static void initialize() {
		new Config("config.yml").onLoad();
	}

	public static void setSafespot(Location loc) {
		final ConfigHandler cfg = new ConfigHandler("config.yml");

		final double locX = loc.getX();
		final double locY = loc.getY();
		final double locZ = loc.getZ();
		final float yaw = loc.getYaw();
		final World world = loc.getWorld();

		cfg.set("teleport.safelocation.x", locX);
		cfg.set("teleport.safelocation.y", locY);
		cfg.set("teleport.safelocation.z", locZ);
		cfg.set("teleport.safelocation.yaw", yaw);
		cfg.set("teleport.safelocation.world", world.getName());
		cfg.saveConfig();

		SAFE_LOCATION = new Location(world, locX, locY, locZ, yaw, 0F);

	}
}
