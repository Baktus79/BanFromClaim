package no.vestlandetmc.BanFromClaim.apiversions;

import org.bukkit.Particle;

public class APIVersion_v1_21 implements VersionHandler {

	@Override
	public Particle getParticle() {
		return Particle.valueOf("DUST");
	}
}
