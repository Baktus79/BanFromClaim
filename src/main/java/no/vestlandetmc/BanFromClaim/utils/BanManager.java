package no.vestlandetmc.BanFromClaim.utils;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.ParticleHandler;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import no.vestlandetmc.BanFromClaim.listener.CombatMode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class BanManager {

	public void enforceBan(Player player, Location locTo, Location locFrom) {
		final ClaimData claimData = new ClaimData();
		final RegionHook regionHook = BfcPlugin.getHookManager().getActiveRegionHook();
		final String regionID = regionHook.getRegionID(locTo);
		final ParticleHandler ph = new ParticleHandler(locTo);

		if (regionID == null) return;

		final UUID ownerUUID = regionHook.getOwnerID(regionID);
		if (ownerUUID == null) return;

		final Player target = PlayerRidePlayer.getPassenger(player);
		boolean hasAttacked = false;

		if (target != null && !regionHook.hasTrust(target, regionID) && !canBypass(target)
				&& (claimData.isAllBanned(regionID) || playerBanned(target, regionID) || playerBanned(player, regionID))) {
			target.teleport(player.getLocation().add(0, 4, 0));
		}

		if (CombatMode.attackerContains(player.getUniqueId()))
			hasAttacked = CombatMode.getAttacker(player.getUniqueId()).equals(ownerUUID);

		if (canBypass(player)) return;

		if ((claimData.isAllBanned(regionID) || playerBanned(player, regionID)) && !hasAttacked && !regionHook.hasTrust(player, regionID)) {
			final String regionIdFrom = regionHook.getRegionID(locFrom);

			if (regionIdFrom != null && regionIdFrom.equals(regionID)) {
				if (playerBanned(player, regionID) || claimData.isAllBanned(regionID)) {
					final int sizeRadius = regionHook.sizeRadius(regionID);
					final Location greaterBoundaryCorner = regionHook.getGreaterBoundaryCorner(regionID);
					final Location lesserBoundaryCorner = regionHook.getLesserBoundaryCorner(regionID);

					final LocationFinder lf = new LocationFinder(greaterBoundaryCorner, lesserBoundaryCorner, player.getWorld().getUID(), sizeRadius);
					Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getPlugin(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
						if (randomCircumferenceRadiusLoc == null) {
							if (Config.SAFE_LOCATION == null) {
								player.teleport(player.getWorld().getSpawnLocation());
							} else {
								player.teleport(Config.SAFE_LOCATION);
							}
						} else {
							player.teleport(randomCircumferenceRadiusLoc);
						}

					}));

				} else {
					final Location tpLoc = player.getLocation().add(locFrom.toVector().subtract(locTo.toVector()).normalize().multiply(3));

					if (tpLoc.getBlock().getType().equals(Material.AIR)) {
						player.teleport(tpLoc);
					} else {
						final Location safeLoc = tpLoc.getWorld().getHighestBlockAt(tpLoc).getLocation().add(0D, 1D, 0D);
						player.teleport(safeLoc);
					}

					ph.drawCircle(1, locTo.getBlockX() == locFrom.getBlockX());
				}

			} else {
				final Location tpLoc = player.getLocation().add(locFrom.toVector().subtract(locTo.toVector()).normalize().multiply(3));
				if (tpLoc.getBlock().getType().equals(Material.AIR)) {
					player.teleport(tpLoc);
				} else {
					final Location safeLoc = tpLoc.getWorld().getHighestBlockAt(tpLoc).getLocation().add(0D, 1D, 0D);
					player.teleport(safeLoc);
				}

				ph.drawCircle(1, locTo.getBlockX() == locFrom.getBlockX());
			}

			if (!MessageHandler.spamMessageClaim.contains(player.getUniqueId().toString())) {
				MessageHandler.sendTitle(player, Messages.TITLE_MESSAGE, Messages.SUBTITLE_MESSAGE);
				MessageHandler.spamMessageClaim.add(player.getUniqueId().toString());

				Bukkit.getScheduler().runTaskLater(BfcPlugin.getPlugin(), () -> MessageHandler.spamMessageClaim.remove(player.getUniqueId().toString()), 5L * 20L);
			}
		}
	}

	public void kickPlayer(Player player) {
		final ClaimData claimData = new ClaimData();
		final RegionHook regionHook = BfcPlugin.getHookManager().getActiveRegionHook();
		final Location location = player.getLocation();
		final String regionID = regionHook.getRegionID(location);

		if (regionID == null) return;

		final UUID ownerUUID = regionHook.getOwnerID(regionID);
		if (ownerUUID == null) return;

		if (canBypass(player)) return;

		if ((claimData.isAllBanned(regionID) || playerBanned(player, regionID)) && !regionHook.hasTrust(player, regionID)) {
			if (playerBanned(player, regionID) || claimData.isAllBanned(regionID)) {
				final int sizeRadius = regionHook.sizeRadius(regionID);
				final Location greaterBoundaryCorner = regionHook.getGreaterBoundaryCorner(regionID);
				final Location lesserBoundaryCorner = regionHook.getLesserBoundaryCorner(regionID);

				final LocationFinder lf = new LocationFinder(greaterBoundaryCorner, lesserBoundaryCorner, player.getWorld().getUID(), sizeRadius);
				Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getPlugin(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
					if (randomCircumferenceRadiusLoc == null) {
						if (Config.SAFE_LOCATION == null) player.teleport(player.getWorld().getSpawnLocation());
						else player.teleport(Config.SAFE_LOCATION);
					} else {
						player.teleport(randomCircumferenceRadiusLoc);
					}
				}));
			}
		}
	}

	private boolean canBypass(Player player) {
		return player.hasPermission("bfc.bypass") || player.getGameMode().equals(GameMode.SPECTATOR);
	}

	private boolean playerBanned(Player player, String claimID) {
		final ClaimData claimData = new ClaimData();
		if (claimData.checkClaim(claimID)) {
			if (claimData.bannedPlayers(claimID) != null) {
				for (final String bp : claimData.bannedPlayers(claimID)) {
					if (bp.equals(player.getUniqueId().toString())) {
						return true;
					}
				}
			}
		}

		return false;
	}
}
