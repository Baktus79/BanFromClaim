package no.vestlandetmc.BanFromClaim.commands.regiondefence;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.LocationFinder;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.rd.handler.Region;
import no.vestlandetmc.rd.handler.RegionManager;

public class KfcCommandRD implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			MessageHandler.sendConsole("&cThis command can only be used in-game.");
			return true;
		}

		final Player player = (Player) sender;
		final Location loc = player.getLocation();
		final Region rg = RegionManager.getRegion(loc);

		if(args.length == 0) {
			MessageHandler.sendMessage(player, Messages.NO_ARGUMENTS);
			return true;
		}

		if(rg == null) {
			MessageHandler.sendMessage(player, Messages.OUTSIDE_CLAIM);
			return true;
		}

		final Player kickedPlayer = Bukkit.getPlayer(args[0]);
		final boolean isManager = rg.hasManagerTrust(player.getUniqueId());
		final boolean isOwner = rg.isOwner(player.getUniqueId());
		final OfflinePlayer owner = Bukkit.getOfflinePlayer(rg.getOwnerUUID());
		boolean allowKick = false;

		if(isOwner || isManager) { allowKick = true; }
		else if(player.hasPermission("bfc.admin")) { allowKick = true; }

		if(kickedPlayer == null) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.UNVALID_PLAYERNAME, args[0], player.getDisplayName(), null));
			return true;
		} else if(kickedPlayer == player) {
			MessageHandler.sendMessage(player, Messages.KICK_SELF);
			return true;
		} else if(kickedPlayer.getName().equals(owner.getName())) {
			MessageHandler.sendMessage(player, Messages.KICK_OWNER);
			return true;
		}

		if(kickedPlayer.hasPermission("bfc.bypass")) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.PROTECTED, kickedPlayer.getDisplayName(), null, null));
			return true;
		}

		if(!allowKick) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;
		} else {
			final int sizeRadius = (int) Math.max(rg.getLength(), rg.getWidth());

			final Location greaterCorner = new Location(
					rg.getWorld(),
					rg.getGreaterBoundary().getX(),
					64D,
					rg.getGreaterBoundary().getZ());

			final Location lesserCorner = new Location(
					rg.getWorld(),
					rg.getLesserBoundary().getX(),
					64D,
					rg.getLesserBoundary().getZ());

			final Location kickedLoc = kickedPlayer.getLocation();

			if(rg.contains(kickedLoc)) {
				final LocationFinder lf = new LocationFinder(greaterCorner, lesserCorner, rg.getWorld().getUID(), sizeRadius);

				Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getInstance(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
					if(randomCircumferenceRadiusLoc == null) {
						if(Config.SAFE_LOCATION == null) { kickedPlayer.teleport(kickedLoc.getWorld().getSpawnLocation()); }
						else { kickedPlayer.teleport(Config.SAFE_LOCATION); }
					}
					else { kickedPlayer.teleport(randomCircumferenceRadiusLoc);	}

					MessageHandler.sendMessage(kickedPlayer, Messages.placeholders(Messages.KICKED_TARGET, kickedPlayer.getName(), player.getDisplayName(), owner.getName()));

				}));
			}
		}

		MessageHandler.sendMessage(player, Messages.placeholders(Messages.KICKED, kickedPlayer.getName(), null, null));

		return true;
	}

}
