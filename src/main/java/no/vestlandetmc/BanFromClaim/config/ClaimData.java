package no.vestlandetmc.BanFromClaim.config;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClaimData {

	private final FileConfiguration cfg = BfcPlugin.getDataFile();
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

	public static void saveDatafile() {
		try {
			File file = new File(BfcPlugin.getPlugin().getDataFolder(), "data.dat");
			BfcPlugin.getDataFile().save(file);
		} catch (final IOException e) {
			BfcPlugin.getPlugin().getLogger().severe(e.getMessage());
		}
	}

	public static void createSection() {
		if (!BfcPlugin.getDataFile().contains("bfc_claim_data")) {
			BfcPlugin.getDataFile().createSection("bfc_claim_data");
		}
		if (!BfcPlugin.getDataFile().contains("claims-ban-all")) {
			BfcPlugin.getDataFile().createSection("claims-ban-all");
		}
		saveDatafile();
	}
}
