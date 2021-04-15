package no.vestlandetmc.BanFromClaim.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.flowpowered.math.vector.Vector3i;
import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;

public class GDListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onPlayerEnterClaim(PlayerMoveEvent e) {
		final Location locFrom = e.getFrom();
		final Location locTo = e.getTo();
		if(locFrom.getBlock().equals(locTo.getBlock())) { return; }
		final Player player = e.getPlayer();
		final Core gd = GriefDefender.getCore();
		final Vector3i vectorTo = Vector3i.from(locTo.getBlockX(), locTo.getBlockY(), locTo.getBlockZ());
		final Vector3i vectorFrom = Vector3i.from(locFrom.getBlockX(), locFrom.getBlockY(), locFrom.getBlockZ());
		final Claim claimTo = gd.getClaimManager(locTo.getWorld().getUID()).getClaimAt(vectorTo);
		final Claim claimFrom = gd.getClaimManager(locFrom.getWorld().getUID()).getClaimAt(vectorFrom);
		final UUID ownerUUID = claimTo.getOwnerUniqueId();
		boolean hasAttacked = false;

		if(CombatMode.ATTACKER.containsKey(player.getUniqueId()))
			hasAttacked = CombatMode.ATTACKER.get(player.getUniqueId()).equals(ownerUUID);

		if(locFrom.getBlockX() != locTo.getBlockX() || locFrom.getBlockZ() != locTo.getBlockZ()) {
			if(player.hasPermission("bfc.bypass")) { return; }

			if(!claimTo.isWilderness()) {
				if(playerBanned(player, claimTo) && !hasAttacked) {
					if(!claimFrom.isWilderness()) {
						if(playerBanned(player, claimFrom)) {
							final World world = Bukkit.getWorld(claimFrom.getWorldUniqueId());
							final int x = claimFrom.getGreaterBoundaryCorner().getX();
							final int z = claimFrom.getGreaterBoundaryCorner().getZ() + claimFrom.getWidth() / 2;
							final int y = world.getHighestBlockAt(x, z).getY();
							final Location tpLoc = new Location(world, x, y, z).add(0D, 1D, 0D);

							player.teleport(tpLoc);
						} else { player.teleport(locFrom); }
					} else { player.teleport(locFrom); }

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
	}


	private boolean playerBanned(Player player, Claim claim) {
		final ClaimData claimData = new ClaimData();
		final String claimID = claim.getUniqueId().toString();
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

}
