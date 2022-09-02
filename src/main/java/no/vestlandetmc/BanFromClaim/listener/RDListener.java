package no.vestlandetmc.BanFromClaim.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.LocationFinder;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.ParticleHandler;
import no.vestlandetmc.rd.handler.Region;
import no.vestlandetmc.rd.handler.RegionManager;

public class RDListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onPlayerEnterClaim(PlayerMoveEvent e) {
		final ClaimData claimData = new ClaimData();
		final Location locFrom = e.getFrom();
		final Location locTo = e.getTo();

		if(locFrom.getBlock().equals(locTo.getBlock())) { return; }

		final Player player = e.getPlayer();
		final Region rg = RegionManager.getRegion(locTo);
		final ParticleHandler ph = new ParticleHandler(e.getTo());

		if(player.hasPermission("bfc.bypass") || player.getGameMode().equals(GameMode.SPECTATOR)) { return; }

		if(rg != null) {
			final UUID ownerUUID =  rg.getOwnerUUID();
			final String claimID = rg.getRegionID().toString();
			boolean hasAttacked = false;

			if(CombatMode.attackerContains(player.getUniqueId()))
				hasAttacked = CombatMode.getAttacker(player.getUniqueId()).equals(ownerUUID);

			if((claimData.isAllBanned(claimID) || playerBanned(player, claimID)) && !hasAttacked && !hasTrust(player, rg)) {
				if(rg.contains(locFrom)) {
					if(playerBanned(player, claimID) || claimData.isAllBanned(claimID)) {
						final int sizeRadius = (int) Math.max(rg.getLength(), rg.getWidth());

						final LocationFinder lf = new LocationFinder(rg.getGreaterBoundary(), rg.getLesserBoundary(), rg.getWorld().getUID(), sizeRadius);
						Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getInstance(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
							if(randomCircumferenceRadiusLoc == null) {
								if(Config.SAFE_LOCATION == null) { player.teleport(player.getWorld().getSpawnLocation()); }
								else { player.teleport(Config.SAFE_LOCATION); }
							}
							else { player.teleport(randomCircumferenceRadiusLoc);	}

						}));

					} else {
						final Location tpLoc = player.getLocation().add(e.getFrom().toVector().subtract(e.getTo().toVector()).normalize().multiply(3));

						if(tpLoc.getBlock().getType().equals(Material.AIR)) {
							player.teleport(tpLoc);
						}
						else {
							final Location safeLoc = tpLoc.getWorld().getHighestBlockAt(tpLoc).getLocation().add(0D, 1D, 0D);
							player.teleport(safeLoc);
						}

						if(e.getTo().getBlockX() == e.getFrom().getBlockX()) { ph.drawCircle(1, true); }
						else { ph.drawCircle(1, false); }
					}

				} else {
					final Location tpLoc = player.getLocation().add(e.getFrom().toVector().subtract(e.getTo().toVector()).normalize().multiply(3));
					if(tpLoc.getBlock().getType().equals(Material.AIR)) { player.teleport(tpLoc); }
					else {
						final Location safeLoc = tpLoc.getWorld().getHighestBlockAt(tpLoc).getLocation().add(0D, 1D, 0D);
						player.teleport(safeLoc);
					}

					if(e.getTo().getBlockX() == e.getFrom().getBlockX()) { ph.drawCircle(1, true); }
					else { ph.drawCircle(1, false); }
				}

				if(!MessageHandler.spamMessageClaim.contains(player.getUniqueId().toString())) {
					MessageHandler.sendTitle(player, Messages.TITLE_MESSAGE, Messages.SUBTITLE_MESSAGE);
					MessageHandler.spamMessageClaim.add(player.getUniqueId().toString());

					Bukkit.getScheduler().runTaskLater(BfcPlugin.getInstance(), () -> {
						MessageHandler.spamMessageClaim.remove(player.getUniqueId().toString());
					}, 5L * 20L);
				}
			}

		}

	}

	private boolean playerBanned(Player player, String claimID) {
		final ClaimData claimData = new ClaimData();
		if(claimData.checkClaim(claimID)) {
			if(claimData.bannedPlayers(claimID) != null) {
				for(final String bp : claimData.bannedPlayers(claimID)) {
					if(bp.equals(player.getUniqueId().toString())) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean hasTrust(Player player, Region rg) {
		if(player.getUniqueId().equals(rg.getOwnerUUID())) { return true; }
		else if(rg.hasManagerTrust(player.getUniqueId())) { return true; }
		else if(rg.hasBuilderTrust(player.getUniqueId())) { return true; }
		else { return false; }
	}
}
