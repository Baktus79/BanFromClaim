package no.vestlandetmc.BanFromClaim.listener;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;

public class BfcListener implements Listener {

	@EventHandler
	public void onPlayerEnterClaim(PlayerMoveEvent e) {
		final Player player = e.getPlayer();
		final Location loc = e.getTo();
		final Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);

		if(player.hasPermission("bfc.bypass")) { return; }

		if(claim != null) {
			final String claimID = claim.getID().toString();
			if(playerBanned(player, claim, claimID)) {
				GriefPrevention.instance.ejectPlayer(player);

				if(!MessageHandler.spamMessageClaim.contains(player.getUniqueId().toString())) {
					MessageHandler.sendTitle(player, "&4BANNED", "&CYou are banned from this claim");
					MessageHandler.spamMessageClaim.add(player.getUniqueId().toString());


					new BukkitRunnable() {
						@Override
						public void run() {
							MessageHandler.spamMessageClaim.remove(player.getUniqueId().toString());
						}

					}.runTaskLater(BfcPlugin.getInstance(), (5 * 20L));
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
