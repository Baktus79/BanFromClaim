package no.vestlandetmc.BanFromClaim.commands.griefprevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.LocationFinder;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;

public class KfcCommandGP implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			MessageHandler.sendConsole("&cThis command can only be used in-game.");
			return true;
		}

		final Player player = (Player) sender;
		final Location loc = player.getLocation();
		final Claim claim = GriefPrevention.instance.dataStore.getClaimAt(loc, true, null);

		if(args.length == 0) {
			MessageHandler.sendMessage(player, Messages.NO_ARGUMENTS);
			return true;
		}

		if(claim == null) {
			MessageHandler.sendMessage(player, Messages.OUTSIDE_CLAIM);
			return true;
		}

		final Player kickedPlayer = Bukkit.getPlayer(args[0]);
		final String accessDenied = claim.allowGrantPermission(player);
		boolean allowBan = false;

		if(accessDenied == null) { allowBan = true; }
		if(player.hasPermission("bfc.admin")) { allowBan = true; }

		if(kickedPlayer == null) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.UNVALID_PLAYERNAME, args[0], player.getDisplayName(), null));
			return true;
		} else if(kickedPlayer == player) {
			MessageHandler.sendMessage(player, Messages.KICK_SELF);
			return true;
		} else if(kickedPlayer.getName().equals(claim.getOwnerName())) {
			MessageHandler.sendMessage(player, Messages.KICK_OWNER);
			return true;
		}

		if(kickedPlayer.hasPermission("bfc.bypass")) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.PROTECTED, kickedPlayer.getDisplayName(), null, null));
			return true;
		}

		if(!allowBan) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;
		} else {
			final String claimOwner = claim.getOwnerName();

			final int sizeRadius = Math.max(claim.getHeight(), claim.getWidth());
			final Location greaterCorner = claim.getGreaterBoundaryCorner();
			final Location lesserCorner = claim.getLesserBoundaryCorner();

			if(GriefPrevention.instance.dataStore.getClaimAt(kickedPlayer.getLocation(), true, claim) != null) {
				if(GriefPrevention.instance.dataStore.getClaimAt(kickedPlayer.getLocation(), true, claim) == claim) {
					final Location bannedLoc = kickedPlayer.getLocation();
					final LocationFinder lf = new LocationFinder(greaterCorner, lesserCorner, bannedLoc.getWorld().getUID(), sizeRadius);

					Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getInstance(), () -> lf.IterateCircumferencesGP(randomCircumferenceRadiusLoc -> {
						if(randomCircumferenceRadiusLoc == null) {
							if(Config.SAFE_LOCATION == null) { kickedPlayer.teleport(bannedLoc.getWorld().getSpawnLocation()); }
							else { kickedPlayer.teleport(Config.SAFE_LOCATION); }
						}
						else { kickedPlayer.teleport(randomCircumferenceRadiusLoc);	}

						MessageHandler.sendMessage(kickedPlayer, Messages.placeholders(Messages.KICKED_TARGET, kickedPlayer.getName(), player.getDisplayName(), claimOwner));

					}));
				}
			}
		}

		MessageHandler.sendMessage(player, Messages.placeholders(Messages.KICKED, kickedPlayer.getName(), null, null));
		return true;

	}

}
