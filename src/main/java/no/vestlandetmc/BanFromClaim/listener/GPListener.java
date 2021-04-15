package no.vestlandetmc.BanFromClaim.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;

public class GPListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onPlayerEnterClaim(PlayerMoveEvent e) {
		final Location locFrom = e.getFrom();
		final Location locTo = e.getTo();

		if(locFrom.getBlock().equals(locTo.getBlock())) { return; }

		final Player player = e.getPlayer();

		final Claim claim = GriefPrevention.instance.dataStore.getClaimAt(locTo, true, null);
		final UUID ownerUUID =  claim.ownerID;
		boolean hasAttacked = false;

		if(CombatMode.ATTACKER.containsKey(player.getUniqueId()))
			hasAttacked = CombatMode.ATTACKER.get(player.getUniqueId()).equals(ownerUUID);

		if(player.hasPermission("bfc.bypass")) { return; }

		if(claim != null) {
			final String claimID = claim.getID().toString();
			if(playerBanned(player, claim, claimID) && !hasAttacked) {
				GriefPrevention.instance.ejectPlayer(player);

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

	private boolean playerBanned(Player player, Claim claim, String claimID) {
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
}
