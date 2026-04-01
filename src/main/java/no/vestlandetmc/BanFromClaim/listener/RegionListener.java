package no.vestlandetmc.BanFromClaim.listener;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class RegionListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onPlayerEnterClaim(PlayerMoveEvent e) {
		final Location locFrom = e.getFrom();
		final Location locTo = e.getTo();
		final Player player = e.getPlayer();

		if (!locFrom.getBlock().equals(locTo.getBlock())) BfcPlugin.getBanManager().enforceBan(player, locTo, locFrom);
	}
}