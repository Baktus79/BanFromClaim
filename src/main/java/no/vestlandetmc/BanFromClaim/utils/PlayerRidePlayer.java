package no.vestlandetmc.BanFromClaim.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlayerRidePlayer {

	public static Player getPassenger(Player player) {
		for (Entity entity : player.getPassengers()) {
			if (entity instanceof Player passenger) {
				return passenger;
			}
		}

		return null;
	}
}
