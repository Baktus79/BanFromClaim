package no.vestlandetmc.BanFromClaim.handler;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Particles {

	public static void wall(Location loc) {
		final Particle.DustOptions options = new Particle.DustOptions(Color.RED, 1.0F);

		for (double x = 0.0D; x <= 4.0D; x+= 0.5) {
			for (double y = 0.0D; y <= 2.0D; y+= 0.5) {
				final Location posLoc = loc.add(x, y, 0.0D);
				final Location negLoc = loc.add(-x, y, 0.0D);
				loc.getWorld().spawnParticle(Particle.REDSTONE, posLoc, 0, 0, 1, 0, options);
				loc.getWorld().spawnParticle(Particle.REDSTONE, negLoc, 0, 0, 1, 0, options);
			}
		}
	}

}
