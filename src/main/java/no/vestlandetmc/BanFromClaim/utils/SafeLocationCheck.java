package no.vestlandetmc.BanFromClaim.utils;

import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class SafeLocationCheck {

	/**
	 * Checks if a block is safe to be on (solid with 2 breathable blocks above)
	 *
	 * @param block Location to check
	 * @return True if block is safe
	 */
	public static boolean BlockSafetyCheck(Block block) {
		if (!isSolid(block)) return false; //Base block isn't solid
		final Block feet = block.getRelative(BlockFace.UP);
		if (isSolid(feet)) return false; //Solid feet (may suffocate)
		final Block head = feet.getRelative(BlockFace.UP);
		if (isSolid(head)) return false; //Solid head (may suffocate)
		if (!isSolid(block.getRelative(BlockFace.DOWN)))
			return false; //Base block is floating or maybe even tree branch

		//Final check, inside world border? + return
		final WorldBorder worldBorder = block.getWorld().getWorldBorder();
		return worldBorder.isInside(block.getLocation());
	}

	private static boolean isSolid(Block block) {
		if (block.isLiquid()) return false;
		else return !block.isEmpty();
	}
}
