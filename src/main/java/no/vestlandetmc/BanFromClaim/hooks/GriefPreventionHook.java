package no.vestlandetmc.BanFromClaim.hooks;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Supplier;

public class GriefPreventionHook implements RegionHook {

	@Override
	public boolean isInsideRegion(Player player) {
		final Location loc = player.getLocation();
		final Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);
		return claim != null;
	}

	@Override
	public boolean isInsideRegion(Player player, String regionID) {
		final Claim claim = GriefPrevention.instance.dataStore.getClaim(Long.parseLong(regionID));
		return claim.contains(player.getLocation(), true, false);
	}

	@Override
	public boolean isManager(OfflinePlayer player, String regionID) {
		final Claim claim = GriefPrevention.instance.dataStore.getClaim(Long.parseLong(regionID));
		final Supplier<String> isManager = claim.checkPermission(player.getUniqueId(), ClaimPermission.Manage, null);
		return isManager == null;
	}

	@Override
	public String getRegionID(Player player) {
		return getRegionID(player.getLocation());
	}

	@Override
	public String getRegionID(Location location) {
		final Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);
		return claim != null ? claim.getID().toString() : null;
	}

	@Override
	public Location getGreaterBoundaryCorner(String regionID) {
		final Claim claim = GriefPrevention.instance.dataStore.getClaim(Long.parseLong(regionID));
		return claim.getGreaterBoundaryCorner();
	}

	@Override
	public Location getLesserBoundaryCorner(String regionID) {
		final Claim claim = GriefPrevention.instance.dataStore.getClaim(Long.parseLong(regionID));
		return claim.getLesserBoundaryCorner();
	}

	@Override
	public int sizeRadius(String regionID) {
		final Claim claim = GriefPrevention.instance.dataStore.getClaim(Long.parseLong(regionID));
		return Math.max(claim.getHeight(), claim.getWidth());
	}

	@Override
	public boolean isOwner(OfflinePlayer player, String claimID) {
		final Claim claim = GriefPrevention.instance.dataStore.getClaim(Long.parseLong(claimID));
		return player.getUniqueId().toString().equals(claim.getOwnerID().toString());
	}

	@Override
	public UUID getOwnerID(String regionID) {
		final Claim claim = GriefPrevention.instance.dataStore.getClaim(Long.parseLong(regionID));
		return claim.getOwnerID();
	}

	@Override
	public String getClaimOwnerName(String regionID) {
		final Claim claim = GriefPrevention.instance.dataStore.getClaim(Long.parseLong(regionID));
		return claim.getOwnerName();
	}

	@Override
	public boolean hasTrust(OfflinePlayer player, String regionID) {
		final Claim claim = GriefPrevention.instance.dataStore.getClaim(Long.parseLong(regionID));

		for (ClaimPermission claimPermission : ClaimPermission.values()) {
			if (claimPermission != ClaimPermission.Edit) {
				if (claim.checkPermission(player.getUniqueId(), claimPermission, null) == null) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean regionExist(String regionID) {
		final Claim claim = GriefPrevention.instance.dataStore.getClaim(Long.parseLong(regionID));
		return claim != null;
	}
}
