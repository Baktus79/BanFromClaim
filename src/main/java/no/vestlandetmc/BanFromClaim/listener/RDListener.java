package no.vestlandetmc.BanFromClaim.listener;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.ParticleHandler;
import no.vestlandetmc.rd.api.RegionEnterEvent;
import no.vestlandetmc.rd.handler.Region;

public class RDListener implements Listener {

	@EventHandler
	public void onPlayerEnterRegion(RegionEnterEvent e) {
		final ClaimData claimData = new ClaimData();
		final Player player = e.getPlayer();
		final Region rg = e.getRegion();
		final ParticleHandler ph = new ParticleHandler(player.getLocation());

		if(player.hasPermission("bfc.bypass") || player.getGameMode().equals(GameMode.SPECTATOR)) { return; }

		final UUID ownerUUID =  rg.getOwnerUUID();
		final String regionID = rg.getRegionID().toString();
		boolean hasAttacked = false;

		if(CombatMode.attackerContains(player.getUniqueId()))
			hasAttacked = CombatMode.getAttacker(player.getUniqueId()).equals(ownerUUID);

		final Location tpLoc = player.getLocation().add(e.getLastLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(3));

		if(!hasAttacked && !hasTrust(player, rg)) {
			if(playerBanned(player, regionID) || claimData.isAllBanned(regionID)) {
				if(tpLoc.getBlock().isEmpty()) {
					if(Config.SAFE_LOCATION != null) {
						player.teleport(Config.SAFE_LOCATION);
					}
				}

				if(!tpLoc.getBlock().isEmpty()) { tpLoc.setY(tpLoc.getWorld().getHighestBlockYAt(tpLoc) + 1); }

				player.teleport(tpLoc);
				if(player.getLocation().getBlockX() == e.getLastLocation().getBlockX()) { ph.drawCircle(1, true); }
				else { ph.drawCircle(1, false); }

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
