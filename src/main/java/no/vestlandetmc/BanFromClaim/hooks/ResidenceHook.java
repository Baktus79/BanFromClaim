package no.vestlandetmc.BanFromClaim.hooks;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.api.ResidenceInterface;
import com.bekvon.bukkit.residence.event.ResidenceDeleteEvent;
import com.bekvon.bukkit.residence.event.ResidenceRenameEvent;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.UUID;

@SuppressWarnings("deprecation")
public class ResidenceHook implements RegionHook, Listener {

	@Override
	public boolean isInsideRegion(Player player) {
		final ResidenceInterface res = Residence.getInstance().getResidenceManagerAPI();
		final ClaimedResidence region = res.getByLoc(player.getLocation());
		return region != null;
	}

	@Override
	public boolean isInsideRegion(Player player, String regionID) {
		final ResidenceInterface res = Residence.getInstance().getResidenceManagerAPI();
		final ClaimedResidence region = res.getByName(regionID);
		return region != null && region.containsLoc(player.getLocation());
	}

	@Override
	public boolean isManager(OfflinePlayer player, String regionID) {
		return false;
	}

	@Override
	public String getRegionID(Player player) {
		return getRegionID(player.getLocation());
	}

	@Override
	public String getRegionID(Location location) {
		final ResidenceInterface res = Residence.getInstance().getResidenceManagerAPI();
		final ClaimedResidence region = res.getByLoc(location);
		return region != null ? region.getResidenceName() : null;
	}

	@Override
	public Object getRegion(Player player) {
		return getRegion(player.getLocation());
	}

	@Override
	public Object getRegion(Location location) {
		final ResidenceInterface res = Residence.getInstance().getResidenceManagerAPI();
		return res.getByLoc(location);
	}

	@Override
	public Location getGreaterBoundaryCorner(String regionID) {
		final ResidenceInterface res = Residence.getInstance().getResidenceManagerAPI();
		final ClaimedResidence region = res.getByName(regionID);
		return region.getMainArea().getHighLocation();
	}

	@Override
	public Location getLesserBoundaryCorner(String regionID) {
		final ResidenceInterface res = Residence.getInstance().getResidenceManagerAPI();
		final ClaimedResidence region = res.getByName(regionID);
		return region.getMainArea().getLowLocation();
	}

	@Override
	public int sizeRadius(String regionID) {
		final ResidenceInterface res = Residence.getInstance().getResidenceManagerAPI();
		final ClaimedResidence region = res.getByName(regionID);
		return Math.max(region.getMainArea().getXSize(), region.getMainArea().getZSize());
	}

	@Override
	public boolean isOwner(OfflinePlayer player, String claimID) {
		final ResidenceInterface res = Residence.getInstance().getResidenceManagerAPI();
		final ClaimedResidence region = res.getByName(claimID);
		return region.isOwner(player.getUniqueId());
	}

	@Override
	public UUID getOwnerID(String regionID) {
		final ResidenceInterface res = Residence.getInstance().getResidenceManagerAPI();
		final ClaimedResidence region = res.getByName(regionID);
		return region.getOwnerUUID();
	}

	@Override
	public String getClaimOwnerName(String regionID) {
		final ResidenceInterface res = Residence.getInstance().getResidenceManagerAPI();
		final ClaimedResidence region = res.getByName(regionID);
		return region.getOwner();
	}

	@Override
	public boolean hasTrust(OfflinePlayer player, String regionID) {
		final ResidenceInterface res = Residence.getInstance().getResidenceManagerAPI();
		final ClaimedResidence region = res.getByName(regionID);
		return region.isTrusted(player.getName());
	}

	@Override
	public boolean regionExist(String regionID) {
		final ResidenceInterface res = Residence.getInstance().getResidenceManagerAPI();
		final ClaimedResidence region = res.getByName(regionID);
		return region != null;
	}

	@EventHandler
	public void residenceRename(ResidenceRenameEvent e) {
		final ClaimData claimData = new ClaimData();
		claimData.changeRegionID(e.getOldResidenceName(), e.getNewResidenceName());
	}

	@EventHandler
	public void deleteClaim(ResidenceDeleteEvent e) {
		final FileConfiguration data = BfcPlugin.getDataFile();
		boolean dataChange = false;

		if (data.contains("bfc_claim_data." + e.getResidence().getResidenceName())) {
			data.set("bfc_claim_data." + e.getResidence().getResidenceName(), null);
			dataChange = true;
		}

		if (data.contains("claims-ban-all." + e.getResidence().getResidenceName())) {
			data.set("claims-ban-all." + e.getResidence().getResidenceName(), null);
			dataChange = true;
		}

		if (dataChange) {
			ClaimData.saveDatafile();
		}

	}
}
