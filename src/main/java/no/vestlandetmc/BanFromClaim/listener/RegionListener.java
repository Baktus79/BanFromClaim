package no.vestlandetmc.BanFromClaim.listener;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.ParticleHandler;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import no.vestlandetmc.BanFromClaim.utils.LocationFinder;
import no.vestlandetmc.BanFromClaim.utils.PlayerRidePlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.UUID;

public class RegionListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onPlayerEnterClaim(PlayerMoveEvent e) {
		final ClaimData claimData = new ClaimData();
		final Location locFrom = e.getFrom();
		final Location locTo = e.getTo();

		if (locFrom.getBlock().equals(locTo.getBlock())) {
			return;
		}

		final Player player = e.getPlayer();
		final RegionHook region = BfcPlugin.getHookManager().getActiveRegionHook();
		final String regionID = region.getRegionID(locTo);
		final ParticleHandler ph = new ParticleHandler(e.getTo());

		if (regionID != null) {
			final UUID ownerUUID = region.getOwnerID(regionID);
			final Player target = PlayerRidePlayer.getPassenger(player);
			boolean hasAttacked = false;

			if (target != null && (claimData.isAllBanned(regionID) || playerBanned(target, regionID) || playerBanned(player, regionID))) {
				target.teleport(player.getLocation().add(0, 4, 0));
			}

			if (CombatMode.attackerContains(player.getUniqueId()))
				hasAttacked = CombatMode.getAttacker(player.getUniqueId()).equals(ownerUUID);

			if (player.hasPermission("bfc.bypass") || player.getGameMode().equals(GameMode.SPECTATOR)) {
				return;
			}

			if ((claimData.isAllBanned(regionID) || playerBanned(player, regionID)) && !hasAttacked && !region.hasTrust(player, regionID)) {
				final String regionIdFrom = region.getRegionID(locFrom);
				
				if (regionIdFrom != null && regionIdFrom.equals(regionID)) {
					if (playerBanned(player, regionID) || claimData.isAllBanned(regionID)) {
						final int sizeRadius = region.sizeRadius(regionID);
						final Location greaterBoundaryCorner = region.getGreaterBoundaryCorner(regionID);
						final Location lesserBoundaryCorner = region.getLesserBoundaryCorner(regionID);

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
						final Location tpLoc = player.getLocation().add(e.getFrom().toVector().subtract(e.getTo().toVector()).normalize().multiply(3));

						if (tpLoc.getBlock().getType().equals(Material.AIR)) {
							player.teleport(tpLoc);
						} else {
							final Location safeLoc = tpLoc.getWorld().getHighestBlockAt(tpLoc).getLocation().add(0D, 1D, 0D);
							player.teleport(safeLoc);
						}

						ph.drawCircle(1, e.getTo().getBlockX() == e.getFrom().getBlockX());
					}

				} else {
					final Location tpLoc = player.getLocation().add(e.getFrom().toVector().subtract(e.getTo().toVector()).normalize().multiply(3));
					if (tpLoc.getBlock().getType().equals(Material.AIR)) {
						player.teleport(tpLoc);
					} else {
						final Location safeLoc = tpLoc.getWorld().getHighestBlockAt(tpLoc).getLocation().add(0D, 1D, 0D);
						player.teleport(safeLoc);
					}

					ph.drawCircle(1, e.getTo().getBlockX() == e.getFrom().getBlockX());
				}

				if (!MessageHandler.spamMessageClaim.contains(player.getUniqueId().toString())) {
					MessageHandler.sendTitle(player, Messages.TITLE_MESSAGE, Messages.SUBTITLE_MESSAGE);
					MessageHandler.spamMessageClaim.add(player.getUniqueId().toString());

					Bukkit.getScheduler().runTaskLater(BfcPlugin.getPlugin(), () -> MessageHandler.spamMessageClaim.remove(player.getUniqueId().toString()), 5L * 20L);
				}
			}

		}

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
