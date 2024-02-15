package no.vestlandetmc.BanFromClaim.commands.griefdefender;

import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.utils.LocationFinder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KfcCommandGD implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			MessageHandler.sendConsole("&cThis command can only be used in-game.");
			return true;
		}

		final Location loc = player.getLocation();
		final Vector3i vector = Vector3i.from(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaimManager(loc.getWorld().getUID()).getClaimAt(vector);

		if (args.length == 0) {
			MessageHandler.sendMessage(player, Messages.NO_ARGUMENTS);
			return true;
		}

		if (claim.isWilderness()) {
			MessageHandler.sendMessage(player, Messages.OUTSIDE_CLAIM);
			return true;
		}

		final Player kickedPlayer = Bukkit.getPlayer(args[0]);
		final boolean isManager = claim.getUserTrusts(TrustTypes.MANAGER).contains(player.getUniqueId());
		final boolean isOwner = claim.getOwnerUniqueId().equals(player.getUniqueId());
		boolean allowKick = false;

		if (isOwner || isManager) {
			allowKick = true;
		} else if (player.hasPermission("bfc.admin")) {
			allowKick = true;
		}

		if (kickedPlayer == null) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.UNVALID_PLAYERNAME, args[0], player.getDisplayName(), null));
			return true;
		} else if (kickedPlayer == player) {
			MessageHandler.sendMessage(player, Messages.KICK_SELF);
			return true;
		} else if (kickedPlayer.getName().equals(claim.getOwnerName())) {
			MessageHandler.sendMessage(player, Messages.KICK_OWNER);
			return true;
		}

		if (kickedPlayer.hasPermission("bfc.bypass")) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.PROTECTED, kickedPlayer.getDisplayName(), null, null));
			return true;
		}

		if (!allowKick) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;
		} else {
			final int sizeRadius = Math.max(claim.getLength(), claim.getWidth());

			final Location greaterCorner = new Location(
					Bukkit.getWorld(claim.getWorldUniqueId()),
					claim.getGreaterBoundaryCorner().getX(),
					64D,
					claim.getGreaterBoundaryCorner().getZ());

			final Location lesserCorner = new Location(
					Bukkit.getWorld(claim.getWorldUniqueId()),
					claim.getLesserBoundaryCorner().getX(),
					64D,
					claim.getLesserBoundaryCorner().getZ());

			final String claimOwner = claim.getOwnerName();
			final Location kickedLoc = kickedPlayer.getLocation();
			final Vector3i kickedVec = Vector3i.from(kickedLoc.getBlockX(), kickedLoc.getBlockY(), kickedLoc.getBlockZ());

			if (claim.contains(kickedVec)) {
				final LocationFinder lf = new LocationFinder(greaterCorner, lesserCorner, claim.getWorldUniqueId(), sizeRadius);

				Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getInstance(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
					if (randomCircumferenceRadiusLoc == null) {
						if (Config.SAFE_LOCATION == null) {
							kickedPlayer.teleport(kickedLoc.getWorld().getSpawnLocation());
						} else {
							kickedPlayer.teleport(Config.SAFE_LOCATION);
						}
					} else {
						kickedPlayer.teleport(randomCircumferenceRadiusLoc);
					}

					MessageHandler.sendMessage(kickedPlayer, Messages.placeholders(Messages.KICKED_TARGET, kickedPlayer.getName(), player.getDisplayName(), claimOwner));

				}));
			}
		}

		MessageHandler.sendMessage(player, Messages.placeholders(Messages.KICKED, kickedPlayer.getName(), null, null));

		return true;
	}

}
