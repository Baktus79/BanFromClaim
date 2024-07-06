package no.vestlandetmc.BanFromClaim.handler;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
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
		final Particle dustParticle = BfcPlugin.getVersionManager().getVersionHandler().getParticle();
		final DustOptions dust = new DustOptions(Color.fromRGB(100, 0, 0), 0.5F);

		if (isX) {
			for (float r = 0; r <= radius; r += 0.2F) {
				for (double t = 0; t < 50; t += 0.2) {
					final float x = r * (float) Math.sin(t);
					final float y = r * (float) Math.cos(t);
					loc.getWorld().spawnParticle(dustParticle, loc.getX() + x, loc.getY() + 1D + y, loc.getZ(), 1, dust);
				}
			}
		} else {
			for (float r = 0; r <= radius; r += 0.2F) {
				for (double t = 0; t < 50; t += 0.2) {
					final float z = r * (float) Math.sin(t);
					final float y = r * (float) Math.cos(t);
					loc.getWorld().spawnParticle(dustParticle, loc.getX(), loc.getY() + 1D + y, loc.getZ() + z, 1, dust);
				}
			}
		}

	}

}
