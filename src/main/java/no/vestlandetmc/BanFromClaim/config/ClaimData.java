package no.vestlandetmc.BanFromClaim.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;

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

		if(!cfg.getStringList(prefix + "." + claimID).isEmpty()) {
			uuid.addAll(cfg.getStringList(prefix + "." + claimID));
			if(uuid.contains(bannedUUID)) {
				uuid.remove(bannedUUID);
				cfg.set(prefix + "." + claimID, uuid);

				if(cfg.getStringList(prefix + "." + claimID).isEmpty()) { cfg.set(prefix + "." + claimID, null); }
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

	public static void cleanDatafile() {
		boolean clean = false;
		final String prefix = BfcPlugin.getInstance().getDescription().getPrefix();


		if(!BfcPlugin.getInstance().getDataFile().getKeys(false).isEmpty()) {
			if(!BfcPlugin.getInstance().getDataFile().getConfigurationSection("bfc_claim_data").getKeys(false).isEmpty()) {
				for(final String claimID : BfcPlugin.getInstance().getDataFile().getConfigurationSection("bfc_claim_data").getKeys(false)) {
					if(BfcPlugin.getInstance().getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
						if(GriefPrevention.instance.dataStore.getClaim(Long.parseLong(claimID)) == null) {
							BfcPlugin.getInstance().getDataFile().set("bfc_claim_data." + claimID, null);
							clean = true;
						}
					}

					else if(BfcPlugin.getInstance().getServer().getPluginManager().getPlugin("GriefDefender") != null) {
						final Core gd = GriefDefender.getCore();
						final UUID uuid = UUID.fromString(claimID);
						final Claim claim = gd.getClaim(uuid);

						if(claim == null) {
							BfcPlugin.getInstance().getDataFile().set("bfc_claim_data." + claimID, null);
							clean = true;
						}
					}
				}
			}
		}

		if(clean) {
			saveDatafile();
			MessageHandler.sendConsole("&2[" + prefix + "] &eData storage has been cleared of old removed claims...");
		}
	}
}
