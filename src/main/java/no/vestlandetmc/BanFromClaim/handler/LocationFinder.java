package no.vestlandetmc.BanFromClaim.handler;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;

import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
public class LocationFinder {

	private final Location circumferenceCenter;
	private final UUID circumferenceWorldUUID;
	private int circumferenceRadius;

	public LocationFinder(Location loc1, Location loc2, UUID circumferenceWorldUUID, int circumferenceRadius) {
		this.circumferenceCenter = findCenter(loc1, loc2);
		this.circumferenceWorldUUID = circumferenceWorldUUID;
		this.circumferenceRadius = circumferenceRadius;
	}

	/**[Run asynchronously] Callback returns "safe" location outside a claim if found, if not found returns null (Uses expanding iterating circumferences method)*/
	public void IterateCircumferences(CallbackReturnLocation callback) {
		final World circumferenceWorld = Bukkit.getWorld(this.circumferenceWorldUUID);
		final BfcPlugin plugin = BfcPlugin.getInstance();
		Location randomCircumferenceRadiusLoc = null;

		final int maxCircleIterations = 10;
		final int checkLocationsPerCircumference = 4;
		final int maxSafeLocationFailures = 5;
		int safeLocationChecks = 0;

		outer: for(int i = 0; i < maxCircleIterations; i++) { //Circle radius iteration
			circumferenceRadius *= 2;

			for(int j = 0; j < checkLocationsPerCircumference; j++) { //Circumference position + check within claim
				randomCircumferenceRadiusLoc = GetRandomCircumferenceLoc(this.circumferenceCenter, circumferenceRadius, circumferenceWorld);
				if(!hasClaim(randomCircumferenceRadiusLoc)) {
					safeLocationChecks++;

					final Block highestBlock = circumferenceWorld.getHighestBlockAt(randomCircumferenceRadiusLoc);

					if(SafeLocationCheck.BlockSafetyCheck(highestBlock)) {
						randomCircumferenceRadiusLoc = new Location(circumferenceWorld, highestBlock.getX() + 0.5, highestBlock.getY() + 1, highestBlock.getZ() + 0.5);
						break outer;
					}

					else if(!(safeLocationChecks >= maxSafeLocationFailures)) j = 0; //Reset circumference position search unless it's the last safe check
				}
			}

			if(i == maxCircleIterations - 1) randomCircumferenceRadiusLoc = null; //Last iteration and no appropriate position found
		}

		final Location finalRandomCircumferenceRadiusLoc = randomCircumferenceRadiusLoc;
		Bukkit.getScheduler().runTask(plugin, (Runnable) () -> callback.onDone(finalRandomCircumferenceRadiusLoc));
	}

	/**Returns a random Location from a circumference of circumferenceRadius and circunferenceCenter*/
	private Location GetRandomCircumferenceLoc(Location circumferenceCenter, int circumferenceRadius, World circumferenceWorld) {
		final double randomAngle = Math.random()*Math.PI*2;
		return new Location(circumferenceWorld,
				circumferenceCenter.getX() + Math.cos(randomAngle) * circumferenceRadius,
				120,
				circumferenceCenter.getZ() + Math.sin(randomAngle) * circumferenceRadius
				);
	}

	private Location findCenter(Location loc1, Location loc2) {
		final int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
		final int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
		final int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
		final int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());

		return new Location(loc1.getWorld(), minX + (maxX - minX) / 2D, 64D, minZ + (maxZ - minZ) / 2D);
	}

	private boolean hasClaim(Location loc) {
		if(Hooks.gpEnabled()) {
			final DataStore griefPreventionCore = GriefPrevention.instance.dataStore;
			return griefPreventionCore.getClaimAt(loc, true, null) != null;
		}

		else if(Hooks.gdEnabled()) {
			final Core griefDefenderCore = GriefDefender.getCore();
			return !griefDefenderCore.getClaimAt(loc).isWilderness();
		}
		else {
			return false;
		}
	}

}
