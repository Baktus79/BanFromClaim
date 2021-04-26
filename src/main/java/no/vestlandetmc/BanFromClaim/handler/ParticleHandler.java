package no.vestlandetmc.BanFromClaim.handler;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;

public class ParticleHandler {

	private final Location loc;

	public ParticleHandler(Location loc) {
		this.loc = loc;
	}

	public void drawCircle(float radius, boolean isX) {
		if(isX) {
			for(float r = 0; r <= radius; r += 0.2) {
				for(double t = 0; t < 50; t += 0.2) {
					final float x = r * (float) Math.sin(t);
					final float y = r * (float) Math.cos(t);

					final Particle redstone = Particle.REDSTONE;
					final DustOptions dust = new DustOptions(Color.fromRGB(100, 0, 0), 0.5F);

					loc.getWorld().spawnParticle(redstone, loc.getX() + x, loc.getY() + 1D + y, loc.getZ(), 1, dust);
				}
			}
		}
		else {
			for(float r = 0; r <= radius; r += 0.2) {
				for(double t = 0; t < 50; t += 0.2) {
					final float z = r * (float) Math.sin(t);
					final float y = r * (float) Math.cos(t);

					final Particle redstone = Particle.REDSTONE;
					final DustOptions dust = new DustOptions(Color.fromRGB(100, 0, 0), 0.5F);

					loc.getWorld().spawnParticle(redstone, loc.getX(), loc.getY() + 1D + y, loc.getZ() + z, 1, dust);
				}
			}
		}

	}

}
