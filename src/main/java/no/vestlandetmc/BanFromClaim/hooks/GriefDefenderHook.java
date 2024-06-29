package no.vestlandetmc.BanFromClaim.hooks;

import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class GriefDefenderHook implements RegionHook {

	@Override
	public boolean isInsideRegion(Player player) {
		final Location loc = player.getLocation();
		final Vector3i vector = Vector3i.from(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaimManager(loc.getWorld().getUID()).getClaimAt(vector);
		return !claim.isWilderness();
	}

	@Override
	public boolean isInsideRegion(Player player, String regionID) {
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaim(UUID.fromString(regionID));
		return claim.contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
	}

	@Override
	public boolean isManager(OfflinePlayer player, String regionID) {
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaim(UUID.fromString(regionID));
		return claim.getUserTrusts(TrustTypes.MANAGER).contains(player.getUniqueId());
	}

	@Override
	public String getRegionID(Player player) {
		return getRegionID(player.getLocation());
	}

	@Override
	public String getRegionID(Location location) {
		final Vector3i vector = Vector3i.from(location.getBlockX(), location.getBlockY(), location.getBlockZ());
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaimManager(location.getWorld().getUID()).getClaimAt(vector);
		return !claim.isWilderness() ? claim.getUniqueId().toString() : null;
	}

	@Override
	public Location getGreaterBoundaryCorner(String regionID) {
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaim(UUID.fromString(regionID));
		return new Location(Bukkit.getWorld(claim.getWorldUniqueId()), claim.getGreaterBoundaryCorner().getX(), 64D, claim.getGreaterBoundaryCorner().getZ());
	}

	@Override
	public Location getLesserBoundaryCorner(String regionID) {
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaim(UUID.fromString(regionID));
		return new Location(Bukkit.getWorld(claim.getWorldUniqueId()), claim.getLesserBoundaryCorner().getX(), 64D, claim.getLesserBoundaryCorner().getZ());
	}

	@Override
	public int sizeRadius(String regionID) {
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaim(UUID.fromString(regionID));
		return Math.max(claim.getLength(), claim.getWidth());
	}

	@Override
	public boolean isOwner(OfflinePlayer player, String claimID) {
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaim(UUID.fromString(claimID));
		return player.getUniqueId().toString().equals(claim.getOwnerUniqueId().toString());
	}

	@Override
	public UUID getOwnerID(String regionID) {
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaim(UUID.fromString(regionID));
		return claim.getOwnerUniqueId();
	}

	@Override
	public String getClaimOwnerName(String regionID) {
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaim(UUID.fromString(regionID));
		return claim.getOwnerName();
	}

	@Override
	public boolean hasTrust(OfflinePlayer player, String regionID) {
		return false;
	}

	@Override
	public boolean regionExist(String regionID) {
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaim(UUID.fromString(regionID));
		return !claim.isWilderness();
	}
}
