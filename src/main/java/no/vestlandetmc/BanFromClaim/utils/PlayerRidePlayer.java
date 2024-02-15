package no.vestlandetmc.BanFromClaim.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerRidePlayer {

	public static Player getPassenger(Player player) {
		for (Player target : Bukkit.getOnlinePlayers()) {
			if (target.getUniqueId().equals(player.getUniqueId())) {
				continue;
			}

			final int xTarget = target.getLocation().getBlockX();
			final int zTarget = target.getLocation().getBlockZ();
			final int yTarget = target.getLocation().getBlockY();
			final int xPlayer = player.getLocation().getBlockX();
			final int zPlayer = player.getLocation().getBlockZ();
			final int yPlayer = player.getLocation().getBlockY();

			if (xTarget == xPlayer && zTarget == zPlayer && (yTarget > yPlayer && yTarget < (yPlayer + 4))) {
				return target;
			}
		}

		return null;
	}
}
