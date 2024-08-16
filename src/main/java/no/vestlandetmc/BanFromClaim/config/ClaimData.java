package no.vestlandetmc.BanFromClaim.config;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClaimData {

	private final FileConfiguration cfg = BfcPlugin.getPlugin().getDataFile();
	private final String prefix = "bfc_claim_data";

	public ClaimData() {

	}

	public boolean setClaimData(String claimID, String bannedUUID, boolean add) {
		if (add) {
			if (!existData(claimID, bannedUUID)) {
				addData(claimID, bannedUUID);
				return true;
			} else {
				return false;
			}
		} else {
			if (existData(claimID, bannedUUID)) {
				removeData(claimID, bannedUUID);
				return true;
			} else {
				return false;
			}
		}
	}

	public void changeRegionID(String oldID, String newID) {
		if (cfg.contains(prefix + "." + oldID)) {
			if (cfg.getStringList(prefix + "." + oldID).isEmpty()) {
				cfg.set(prefix + "." + oldID, null);
			} else {
				final List<String> uuid = bannedPlayers(oldID);
				boolean banAll = false;

				if (cfg.contains("claims-ban-all" + "." + oldID + ".ban-all")) {
					banAll = cfg.getBoolean("claims-ban-all" + "." + oldID + ".ban-all");
				}

				cfg.createSection(prefix + "." + newID);
				cfg.set("claims-ban-all" + "." + newID + ".ban-all", banAll);

				if (uuid != null && !uuid.isEmpty()) {
					cfg.set(prefix + "." + newID, uuid);
				}

				cfg.set("claims-ban-all" + "." + oldID, null);
				cfg.set(prefix + "." + oldID, null);
				saveDatafile();
			}
		}
	}

	private void addData(String claimID, String bannedUUID) {
		final List<String> uuid = new ArrayList<>();

		if (!cfg.contains(prefix + "." + claimID)) {
			cfg.createSection(prefix + "." + claimID);
		}

		if (!cfg.getStringList(prefix + "." + claimID).isEmpty()) {
			uuid.addAll(cfg.getStringList(prefix + "." + claimID));
		}

		uuid.add(bannedUUID);
		cfg.set(prefix + "." + claimID, uuid);
		saveDatafile();
	}

	public void banAll(String claimID) {
		if (cfg.contains("claims-ban-all" + "." + claimID + ".ban-all")) {
			if (cfg.getBoolean("claims-ban-all" + "." + claimID + ".ban-all")) {
				cfg.set("claims-ban-all" + "." + claimID + ".ban-all", false);
			} else {
				cfg.set("claims-ban-all" + "." + claimID + ".ban-all", true);
			}
		} else {
			cfg.set("claims-ban-all" + "." + claimID + ".ban-all", true);
		}

		saveDatafile();
	}

	public boolean isAllBanned(String claimID) {
		if (cfg.contains("claims-ban-all" + "." + claimID + ".ban-all")) {
			return cfg.getBoolean("claims-ban-all" + "." + claimID + ".ban-all");
		} else {
			return false;
		}
	}

	private void removeData(String claimID, String bannedUUID) {
		if (!cfg.getStringList(prefix + "." + claimID).isEmpty()) {
			final List<String> uuid = new ArrayList<>(cfg.getStringList(prefix + "." + claimID));
			if (uuid.contains(bannedUUID)) {
				uuid.remove(bannedUUID);
				cfg.set(prefix + "." + claimID, uuid);

				if (cfg.getStringList(prefix + "." + claimID).isEmpty()) {
					cfg.set(prefix + "." + claimID, null);
				}
				saveDatafile();
			}
		}
	}

	private boolean existData(String claimID, String bannedUUID) {
		if (cfg.contains(prefix + "." + claimID)) {
			if (cfg.getStringList(prefix + "." + claimID).isEmpty()) {
				return false;

			} else {
				final List<String> uuid = new ArrayList<>(cfg.getStringList(prefix + "." + claimID));
				return uuid.contains(bannedUUID);
			}
		}

		return false;
	}

	public boolean checkClaim(String claimID) {
		return cfg.contains(prefix + "." + claimID);
	}

	public List<String> bannedPlayers(String claimID) {
		if (cfg.contains(prefix + "." + claimID)) {
			if (!cfg.getStringList(prefix + "." + claimID).isEmpty()) {
				return cfg.getStringList(prefix + "." + claimID);
			}
		}

		return null;
	}

	private static void saveDatafile() {
		try {
			File file = new File(BfcPlugin.getPlugin().getDataFolder(), "data.dat");
			BfcPlugin.getPlugin().getDataFile().save(file);
		} catch (final IOException e) {
			BfcPlugin.getPlugin().getLogger().severe(e.getMessage());
		}
	}

	public static void createSection() {
		if (!BfcPlugin.getPlugin().getDataFile().contains("bfc_claim_data")) {
			BfcPlugin.getPlugin().getDataFile().createSection("bfc_claim_data");
		}
		if (!BfcPlugin.getPlugin().getDataFile().contains("claims-ban-all")) {
			BfcPlugin.getPlugin().getDataFile().createSection("claims-ban-all");
		}
		saveDatafile();
	}

	public static void cleanDatafile() {
		boolean clean = false;
		final RegionHook region = BfcPlugin.getHookManager().getActiveRegionHook();

		if (!BfcPlugin.getPlugin().getDataFile().getKeys(false).isEmpty()) {
			if (!BfcPlugin.getPlugin().getDataFile().getConfigurationSection("bfc_claim_data").getKeys(false).isEmpty()) {
				for (final String regionID : BfcPlugin.getPlugin().getDataFile().getConfigurationSection("bfc_claim_data").getKeys(false)) {
					if (!region.regionExist(regionID)) {
						BfcPlugin.getPlugin().getDataFile().set("bfc_claim_data." + regionID, null);
						clean = true;
					}
				}
			}

			if (!BfcPlugin.getPlugin().getDataFile().getConfigurationSection("claims-ban-all").getKeys(false).isEmpty()) {
				for (final String regionID : BfcPlugin.getPlugin().getDataFile().getConfigurationSection("bfc_claim_data").getKeys(false)) {
					if (region.regionExist(regionID)) {
						BfcPlugin.getPlugin().getDataFile().set("claims-ban-all." + regionID, null);
						clean = true;
					}
				}
			}
		}

		if (clean) {
			saveDatafile();
			BfcPlugin.getPlugin().getLogger().info("The database has been purged of expired regions...");
		}
	}
}
