package no.vestlandetmc.BanFromClaim.apiversions;

import lombok.Getter;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import org.bukkit.Bukkit;

@Getter
public class VersionManager {

	private VersionHandler versionHandler;

	public VersionManager() {
		final String bukkitVersion = Bukkit.getBukkitVersion();
		final String version = bukkitVersion.split("-")[0];
		BfcPlugin.getPlugin().getLogger().info("Detected " + bukkitVersion + ".");

		if (version.length() >= 4) {
			switch (version.substring(0, 4)) {
				case "1.13", "1.14", "1.15", "1.16", "1.17", "1.18", "1.19", "1.20":
					versionHandler = new APIVersion_v1_13();
					BfcPlugin.getPlugin().getLogger().info("Implements API 1.13 - 1.20.");
					break;
				default:
					versionHandler = new APIVersion_v1_21();
					BfcPlugin.getPlugin().getLogger().info("Implements API 1.21+.");
					break;
			}
		} else {
			BfcPlugin.getPlugin().getLogger().warning("Unsupported Bukkit version: " + bukkitVersion);
		}
	}

}
