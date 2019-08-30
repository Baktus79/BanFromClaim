package no.vestlandetmc.BanFromClaim.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import no.vestlandetmc.BanFromClaim.BfcPlugin;

public class ClaimData {

	private static File file;
	private final FileConfiguration cfg = BfcPlugin.getInstance().getDataFile();
	private final String prefix = "bfc_claim_data";

	public ClaimData() {

	}

	public boolean setClaimData(Player player, String claimID, String bannedUUID, boolean add) {
		if(add) {
			if(!existData(claimID, bannedUUID)) {
				addData(claimID, bannedUUID);
				return true;
			} else { return false; }
		} else {
			if(existData(claimID, bannedUUID)) {
				removeData(claimID, bannedUUID);
				return true;
			} else { return false; }
		}
	}

	private void addData(String claimID, String bannedUUID) {
		final List<String> uuid = new ArrayList<>();

		if(!cfg.contains(prefix + "." + claimID)) { cfg.createSection(prefix + "." + claimID); }

		if(cfg.getStringList(prefix + "." + claimID).isEmpty()) {
			uuid.add(bannedUUID);
			cfg.set(prefix + "." + claimID, uuid);
			saveDatafile();
		} else {
			uuid.addAll(cfg.getStringList(prefix + "." + claimID));
			uuid.add(bannedUUID);
			cfg.set(prefix + "." + claimID, uuid);
			saveDatafile();
		}
	}

	private void removeData(String claimID, String bannedUUID) {
		final List<String> uuid = new ArrayList<>();

		if(cfg.getStringList(prefix + "." + claimID).isEmpty()) {
			cfg.set(claimID, null);
			saveDatafile();
		} else {
			uuid.addAll(cfg.getStringList(prefix + "." + claimID));
			if(uuid.contains(bannedUUID)) {
				uuid.remove(bannedUUID);
				cfg.set(prefix + "." + claimID, uuid);
				saveDatafile();
			}
		}
	}

	private boolean existData(String claimID, String bannedUUID) {
		final List<String> uuid = new ArrayList<>();

		if(cfg.contains(prefix + "." + claimID)) {
			if(cfg.getStringList(prefix + "." + claimID).isEmpty()) {
				return false;

			} else {
				uuid.addAll(cfg.getStringList(prefix + "." + claimID));
				if(uuid.contains(bannedUUID)) {
					return true;
				} else {
					return false;
				}
			}
		}

		return false;
	}

	public boolean checkClaim(String claimID) {
		if(cfg.contains(prefix + "." + claimID)) {
			return true;
		} else {
			return false;
		}
	}

	public List<String> bannedPlayers(String claimID) {
		if(cfg.contains(prefix + "." + claimID)) {
			if(!cfg.getStringList(prefix + "." + claimID).isEmpty()) {
				return cfg.getStringList(prefix + "." + claimID);
			}
		}

		return null;
	}

	private static void saveDatafile() {
		try {
			file = new File(BfcPlugin.getInstance().getDataFolder(), "data.dat");
			BfcPlugin.getInstance().getDataFile().save(file);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static void createSection() {
		if(BfcPlugin.getInstance().getDataFile().getKeys(false).isEmpty()) {
			BfcPlugin.getInstance().getDataFile().createSection("bfc_claim_data");
			saveDatafile();
		}
	}
}
