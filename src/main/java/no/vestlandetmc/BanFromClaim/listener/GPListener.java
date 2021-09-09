package no.vestlandetmc.BanFromClaim.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.ParticleHandler;

public class GPListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onPlayerEnterClaim(PlayerMoveEvent e) {
		final ClaimData claimData = new ClaimData();
		final Location locFrom = e.getFrom();
		final Location locTo = e.getTo();

		if(locFrom.getBlock().equals(locTo.getBlock())) { return; }

		final Player player = e.getPlayer();
		final Claim claim = GriefPrevention.instance.dataStore.getClaimAt(locTo, true, null);
		final ParticleHandler ph = new ParticleHandler(e.getTo());

		if(player.hasPermission("bfc.bypass")) { return; }

		if(claim != null) {
			final UUID ownerUUID =  claim.ownerID;
			final String claimID = claim.getID().toString();
			boolean hasAttacked = false;

			if(CombatMode.ATTACKER.containsKey(player.getUniqueId()))
				hasAttacked = CombatMode.ATTACKER.get(player.getUniqueId()).equals(ownerUUID);

			if((claimData.isAllBanned(claimID) || playerBanned(player, claimID)) && !hasAttacked && !hasTrust(player, claim)) {
				if(claim.contains(locFrom, true, false)) {
					if(playerBanned(player, claimID) || claimData.isAllBanned(claimID)) {
						final World world = claim.getGreaterBoundaryCorner().getWorld();
						final int x = claim.getGreaterBoundaryCorner().getBlockX();
						final int z = claim.getGreaterBoundaryCorner().getBlockZ();
						final int y = world.getHighestBlockAt(x, z).getY();
						final Location tpLoc = new Location(world, x, y, z).add(0D, 1D, 0D);

						if(tpLoc.getBlock().getType().equals(Material.AIR)) {
							if(Config.SAFE_LOCATION != null) {
								player.teleport(Config.SAFE_LOCATION);
							} else { player.teleport(tpLoc.add(0D, 1D, 0D)); }
						} else { player.teleport(tpLoc.add(0D, 1D, 0D)); }

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

	private boolean hasTrust(Player player, Claim claim) {
		final String accessDenied = claim.allowGrantPermission(player);

		if(accessDenied != null || player.getUniqueId().equals(claim.getOwnerID())) { return true; }
		else { return false; }
	}
}
