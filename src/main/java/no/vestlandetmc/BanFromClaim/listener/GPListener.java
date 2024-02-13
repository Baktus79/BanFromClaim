package no.vestlandetmc.BanFromClaim.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dev.geco.gsit.api.GSitAPI;
import dev.geco.gsit.objects.GetUpReason;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
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
import no.vestlandetmc.BanFromClaim.handler.LocationFinder;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.ParticleHandler;

import static org.bukkit.Bukkit.getServer;

public class GPListener implements Listener {
	// check for depend
	private boolean found_GSit = false;
	public GPListener (){
		found_GSit = (getServer().getPluginManager().getPlugin("GSit") != null);
	}
	// ===============
	@EventHandler(ignoreCancelled = true)
	public void onPlayerEnterClaim(PlayerMoveEvent e) {
		final ClaimData claimData = new ClaimData();
		final Location locFrom = e.getFrom();
		final Location locTo = e.getTo();

		if(locFrom.getBlock().equals(locTo.getBlock())) { return; }

		final Player player = e.getPlayer();
		final Claim claim = GriefPrevention.instance.dataStore.getClaimAt(locTo, true, null);
		final ParticleHandler ph = new ParticleHandler(e.getTo());

		if(player.hasPermission("bfc.bypass") || player.getGameMode().equals(GameMode.SPECTATOR)) { return; }

		if(claim != null) {
			final UUID ownerUUID =  claim.ownerID;
			final String claimID = claim.getID().toString();
			boolean hasAttacked = false;

			if(CombatMode.attackerContains(player.getUniqueId()))
				hasAttacked = CombatMode.getAttacker(player.getUniqueId()).equals(ownerUUID);

			if((claimData.isAllBanned(claimID) || playerBanned(player, claimID)) && !hasAttacked && !hasTrust(player, claim)) {
				// if GSit plugin is found, then enable the fix for https://github.com/Baktus79/BanFromClaim/issues/35
				if (found_GSit) {
					// check if the player,s top has any player
					for (Player p : getTopPlayers(player)) {
						// check if any player is sitting on the player get banned (for g-sit is AREA_EFFECT_CLOUD)
						if (p.getVehicle() == null) {
							// maby player just fly on the same location?
							continue;
						}
						if (p.getVehicle().getType().equals(EntityType.AREA_EFFECT_CLOUD)) {
							// unsit the players, so the plugin can normaly ban and teleport player out of the claim
							GSitAPI.stopPlayerSit(p, GetUpReason.PLUGIN);
						}
					}
				}

				if(claim.contains(locFrom, true, false)) {
					if(playerBanned(player, claimID) || claimData.isAllBanned(claimID)) {
						final int sizeRadius = Math.max(claim.getHeight(), claim.getWidth());

						final LocationFinder lf = new LocationFinder(claim.getGreaterBoundaryCorner(), claim.getLesserBoundaryCorner(), player.getWorld().getUID(), sizeRadius);
						Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getInstance(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
							if(randomCircumferenceRadiusLoc == null) {
								if(Config.SAFE_LOCATION == null) {
									player.teleport(player.getWorld().getSpawnLocation());
								}
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
	// get the player that have the same X,Z (Maybe sitting on the target)
	private List<Player> getTopPlayers(Player target) {
		int x = target.getLocation().getBlockX();
		int z = target.getLocation().getBlockZ();
		int y = target.getLocation().getBlockY();
		List<Player> nearbyPlayers = new ArrayList<>();
		for (Player onlinePlayer : getServer().getOnlinePlayers()) {
			Location playerLOC = onlinePlayer.getLocation();
			if (playerLOC.getBlockX() == x && playerLOC.getBlockZ() == z && playerLOC.getBlockY() > y) {
				nearbyPlayers.add(onlinePlayer);
			}
		}
		return nearbyPlayers;
	}

	@SuppressWarnings("deprecation")
	private boolean hasTrust(Player player, Claim claim) {
		final String accessDenied = claim.allowGrantPermission(player);
		final String buildDenied = claim.allowBuild(player, Material.DIRT);

		if(accessDenied == null || buildDenied == null || player.getUniqueId().equals(claim.getOwnerID())) { return true; }
		else { return false; }
	}
}
