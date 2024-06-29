package no.vestlandetmc.BanFromClaim.commands;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import no.vestlandetmc.BanFromClaim.utils.LocationFinder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KfcCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			MessageHandler.sendConsole("&cThis command can only be used in-game.");
			return true;
		}

		final RegionHook region = BfcPlugin.getHookManager().getActiveRegionHook();
		final String regionID = region.getRegionID(player);

		if (args.length == 0) {
			MessageHandler.sendMessage(player, Messages.NO_ARGUMENTS);
			return true;
		}

		if (regionID == null) {
			MessageHandler.sendMessage(player, Messages.OUTSIDE_CLAIM);
			return true;
		}

		final Player kickedPlayer = Bukkit.getPlayer(args[0]);
		final boolean allowBan = player.hasPermission("bfc.admin") || region.isOwner(player, regionID) || region.isManager(player, regionID);

		if (kickedPlayer == null) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.UNVALID_PLAYERNAME, args[0], player.getDisplayName(), null));
			return true;
		} else if (kickedPlayer == player) {
			MessageHandler.sendMessage(player, Messages.KICK_SELF);
			return true;
		} else if (region.isOwner(kickedPlayer, regionID)) {
			MessageHandler.sendMessage(player, Messages.KICK_OWNER);
			return true;
		}

		if (kickedPlayer.hasPermission("bfc.bypass")) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.PROTECTED, kickedPlayer.getDisplayName(), null, null));
			return true;
		}

		if (!allowBan) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;
		} else {
			final String claimOwner = region.getClaimOwnerName(regionID);

			final int sizeRadius = region.sizeRadius(regionID);
			final Location greaterCorner = region.getGreaterBoundaryCorner(regionID);
			final Location lesserCorner = region.getLesserBoundaryCorner(regionID);

			if (region.isInsideRegion(kickedPlayer, regionID)) {
				final Location bannedLoc = kickedPlayer.getLocation();
				final LocationFinder lf = new LocationFinder(greaterCorner, lesserCorner, bannedLoc.getWorld().getUID(), sizeRadius);

				Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getPlugin(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
					if (randomCircumferenceRadiusLoc == null) {
						if (Config.SAFE_LOCATION == null) {
							kickedPlayer.teleport(bannedLoc.getWorld().getSpawnLocation());
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
