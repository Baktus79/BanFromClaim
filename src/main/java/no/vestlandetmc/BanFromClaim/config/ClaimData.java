package no.vestlandetmc.BanFromClaim.config;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stores per-claim ban lists and "ban all" flags in plugins/BanFromClaim/claimdata.yml
 *
 * Fixes:
 * - No reliance on BfcPlugin.getDataFile() (which was null -> NPE spam)
 * - Loads YAML lazily and safely
 * - Saves only after modifications
 */
public class ClaimData {

	private static final String FILE_NAME = "claimdata.yml";
	private static final String PREFIX = "bfc_claim_data";
	private static final String BAN_ALL_PREFIX = "claims-ban-all";

	private static File file;
	private static FileConfiguration cfg;

	public ClaimData() {
		ensureLoaded();
	}

	// -------------------------
	// Public API (same behavior)
	// -------------------------

	public boolean setClaimData(String claimID, String bannedUUID, boolean add) {
		if (claimID == null || claimID.isBlank() || bannedUUID == null || bannedUUID.isBlank()) return false;
		ensureLoaded();

		if (add) {
			if (existData(claimID, bannedUUID)) return false;
			addData(claimID, bannedUUID);
			return true;
		} else {
			if (!existData(claimID, bannedUUID)) return false;
			removeData(claimID, bannedUUID);
			return true;
		}
	}

	public void changeRegionID(String oldID, String newID) {
		if (oldID == null || oldID.isBlank() || newID == null || newID.isBlank()) return;
		ensureLoaded();

		final String oldPath = claimPath(oldID);
		if (!cfg.contains(oldPath)) return;

		final List<String> oldList = cfg.getStringList(oldPath);
		final boolean banAll = isAllBanned(oldID);

		// If old list is empty AND no ban-all flag, just clean up
		if ((oldList == null || oldList.isEmpty()) && !banAll) {
			cfg.set(oldPath, null);
			cfg.set(banAllPath(oldID), null);
			saveDatafile();
			return;
		}

		// Move list
		final String newPath = claimPath(newID);
		if (oldList != null && !oldList.isEmpty()) {
			cfg.set(newPath, new ArrayList<>(oldList));
		} else {
			cfg.set(newPath, null);
		}

		// Move ban-all
		cfg.set(banAllValuePath(newID), banAll);

		// Clear old
		cfg.set(oldPath, null);
		cfg.set(banAllPath(oldID), null);

		saveDatafile();
	}

	public void banAll(String claimID) {
		if (claimID == null || claimID.isBlank()) return;
		ensureLoaded();

		final boolean current = isAllBanned(claimID);
		cfg.set(banAllValuePath(claimID), !current);
		saveDatafile();
	}

	public boolean isAllBanned(String claimID) {
		if (claimID == null || claimID.isBlank()) return false;
		ensureLoaded();

		final String path = banAllValuePath(claimID);
		return cfg.contains(path) && cfg.getBoolean(path);
	}

	public boolean checkClaim(String claimID) {
		if (claimID == null || claimID.isBlank()) return false;
		ensureLoaded();
		return cfg.contains(claimPath(claimID));
	}

	/**
	 * Returns the list of banned UUIDs for this claim, or null if none exists (keeps your old behavior).
	 */
	public List<String> bannedPlayers(String claimID) {
		if (claimID == null || claimID.isBlank()) return null;
		ensureLoaded();

		final String path = claimPath(claimID);
		if (!cfg.contains(path)) return null;

		final List<String> list = cfg.getStringList(path);
		if (list == null || list.isEmpty()) return null;

		// Return immutable copy to prevent accidental mutation without saving
		return Collections.unmodifiableList(new ArrayList<>(list));
	}

	// -------------------------
	// Internals
	// -------------------------

	private void addData(String claimID, String bannedUUID) {
		ensureLoaded();

		final String path = claimPath(claimID);
		final List<String> uuid = new ArrayList<>(cfg.getStringList(path));

		uuid.add(bannedUUID);
		cfg.set(path, uuid);

		saveDatafile();
	}

	private void removeData(String claimID, String bannedUUID) {
		ensureLoaded();

		final String path = claimPath(claimID);
		final List<String> uuid = new ArrayList<>(cfg.getStringList(path));

		if (!uuid.remove(bannedUUID)) return;

		if (uuid.isEmpty()) {
			cfg.set(path, null);
		} else {
			cfg.set(path, uuid);
		}

		saveDatafile();
	}

	private boolean existData(String claimID, String bannedUUID) {
		ensureLoaded();

		final String path = claimPath(claimID);
		if (!cfg.contains(path)) return false;

		final List<String> list = cfg.getStringList(path);
		return list != null && !list.isEmpty() && list.contains(bannedUUID);
	}

	private static String claimPath(String claimID) {
		return PREFIX + "." + claimID;
	}

	private static String banAllPath(String claimID) {
		return BAN_ALL_PREFIX + "." + claimID;
	}

	private static String banAllValuePath(String claimID) {
		return BAN_ALL_PREFIX + "." + claimID + ".ban-all";
	}

	// -------------------------
	// File handling
	// -------------------------

	private static synchronized void ensureLoaded() {
		if (cfg != null) return;

		final File dataFolder = BfcPlugin.getPlugin().getDataFolder();
		if (!dataFolder.exists() && !dataFolder.mkdirs()) {
			BfcPlugin.getPlugin().getLogger().severe("Could not create plugin data folder!");
		}

		file = new File(dataFolder, FILE_NAME);

		if (!file.exists()) {
			try {
				if (file.createNewFile()) {
					// create baseline structure
					cfg = YamlConfiguration.loadConfiguration(file);
					createSection(); // ensures root sections
				}
			} catch (IOException e) {
				BfcPlugin.getPlugin().getLogger().severe("Failed to create " + FILE_NAME + ": " + e.getMessage());
			}
		}

		cfg = YamlConfiguration.loadConfiguration(file);

		// Ensure base sections always exist
		if (!cfg.contains(PREFIX)) cfg.createSection(PREFIX);
		if (!cfg.contains(BAN_ALL_PREFIX)) cfg.createSection(BAN_ALL_PREFIX);

		saveDatafile(); // safe (will write only if file exists)
	}

	public static synchronized void saveDatafile() {
		if (cfg == null || file == null) return;
		try {
			cfg.save(file);
		} catch (IOException e) {
			BfcPlugin.getPlugin().getLogger().severe("Failed to save " + FILE_NAME + ": " + e.getMessage());
		}
	}

	public static synchronized void createSection() {
		ensureLoaded();
		if (!cfg.contains(PREFIX)) cfg.createSection(PREFIX);
		if (!cfg.contains(BAN_ALL_PREFIX)) cfg.createSection(BAN_ALL_PREFIX);
		saveDatafile();
	}

	public static synchronized void reloadDatafile() {
		if (file == null) return;
		cfg = YamlConfiguration.loadConfiguration(file);
	}
}