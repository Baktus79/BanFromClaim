package no.vestlandetmc.BanFromClaim.hooks;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface RegionHook {

	boolean isInsideRegion(Player player);

	boolean isInsideRegion(Player player, String regionID);

	boolean isManager(OfflinePlayer player, String regionID);

	String getRegionID(Player player);

	String getRegionID(Location location);

	Location getGreaterBoundaryCorner(String regionID);

	Location getLesserBoundaryCorner(String regionID);

	int sizeRadius(String regionID);

	boolean isOwner(OfflinePlayer player, String claimID);

	UUID getOwnerID(String regionID);

	String getClaimOwnerName(String regionID);

	boolean hasTrust(OfflinePlayer player, String regionID);

	boolean regionExist(String regionID);

}
