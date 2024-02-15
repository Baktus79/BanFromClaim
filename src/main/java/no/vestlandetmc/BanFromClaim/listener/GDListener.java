package no.vestlandetmc.BanFromClaim.listener;

import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.ParticleHandler;
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

public class GDListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onPlayerEnterClaim(PlayerMoveEvent e) {
		final ClaimData claimData = new ClaimData();
		final Location locFrom = e.getFrom();
		final Location locTo = e.getTo();

		if (locFrom.getBlock().equals(locTo.getBlock())) {
			return;
		}

		final Player player = e.getPlayer();
		final Core gd = GriefDefender.getCore();
		final Vector3i vectorTo = Vector3i.from(locTo.getBlockX(), locTo.getBlockY(), locTo.getBlockZ());
		final Vector3i vectorFrom = Vector3i.from(locFrom.getBlockX(), locFrom.getBlockY(), locFrom.getBlockZ());
		final Claim claimTo = gd.getClaimManager(locTo.getWorld().getUID()).getClaimAt(vectorTo);
		final Claim claimFrom = gd.getClaimManager(locFrom.getWorld().getUID()).getClaimAt(vectorFrom);
		final ParticleHandler ph = new ParticleHandler(e.getTo());

		if (locFrom.getBlockX() != locTo.getBlockX() || locFrom.getBlockZ() != locTo.getBlockZ()) {
			if (!claimTo.isWilderness()) {
				final UUID ownerUUID = claimTo.getOwnerUniqueId();
				final Player target = PlayerRidePlayer.getPassenger(player);
				boolean hasAttacked = false;

				if (target != null && (claimData.isAllBanned(claimTo.getUniqueId().toString()) || playerBanned(target, claimTo) || playerBanned(player, claimTo))) {
					target.teleport(player.getLocation().add(0, 4, 0));
				}

				if (CombatMode.attackerContains(player.getUniqueId()))
					hasAttacked = CombatMode.getAttacker(player.getUniqueId()).equals(ownerUUID);

				if (player.hasPermission("bfc.bypass") || player.getGameMode().equals(GameMode.SPECTATOR)) {
					return;
				}

				if ((claimData.isAllBanned(claimTo.getUniqueId().toString()) || playerBanned(player, claimTo)) && !hasAttacked && !hasTrust(player.getUniqueId(), claimTo)) {
					if (!claimFrom.isWilderness()) {
						if (playerBanned(player, claimFrom)) {
							final int sizeRadius = Math.max(claimTo.getLength(), claimTo.getWidth());

							final Location greaterCorner = new Location(
									Bukkit.getWorld(claimTo.getWorldUniqueId()),
									claimTo.getGreaterBoundaryCorner().getX(),
									64D,
									claimTo.getGreaterBoundaryCorner().getZ());

							final Location lesserCorner = new Location(
									Bukkit.getWorld(claimTo.getWorldUniqueId()),
									claimTo.getLesserBoundaryCorner().getX(),
									64D,
									claimTo.getLesserBoundaryCorner().getZ());

							final LocationFinder lf = new LocationFinder(greaterCorner, lesserCorner, player.getWorld().getUID(), sizeRadius);
							Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getInstance(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
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

						Bukkit.getScheduler().runTaskLater(BfcPlugin.getInstance(), () -> MessageHandler.spamMessageClaim.remove(player.getUniqueId().toString()), 5L * 20L);
					}
				}
			}
		}
	}

	private boolean playerBanned(Player player, Claim claim) {
		final ClaimData claimData = new ClaimData();
		final String claimID = claim.getUniqueId().toString();
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

	private boolean hasTrust(UUID player, Claim claim) {
		if (player.equals(claim.getOwnerUniqueId())) {
			return true;
		} else if (claim.isUserTrusted(player, TrustTypes.MANAGER)) {
			return true;
		} else return claim.isUserTrusted(player, TrustTypes.BUILDER);
	}

}
