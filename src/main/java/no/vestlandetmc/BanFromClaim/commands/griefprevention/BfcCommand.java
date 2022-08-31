package no.vestlandetmc.BanFromClaim.commands.griefprevention;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.LocationFinder;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;

public class BfcCommand implements CommandExecutor {

	@SuppressWarnings("deprecation")
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

		final OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(args[0]);
		final String accessDenied = claim.allowGrantPermission(player);
		boolean allowBan = false;

		if(accessDenied == null) { allowBan = true; }
		if(player.hasPermission("bfc.admin")) { allowBan = true; }

		if(!bannedPlayer.hasPlayedBefore()) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.UNVALID_PLAYERNAME, args[0], player.getDisplayName(), null));
			return true;
		} else if(bannedPlayer == player) {
			MessageHandler.sendMessage(player, Messages.BAN_SELF);
			return true;
		} else if(bannedPlayer.getName().equals(claim.getOwnerName())) {
			MessageHandler.sendMessage(player, Messages.BAN_OWNER);
			return true;
		}

		if(bannedPlayer.isOnline()) {
			if(bannedPlayer.getPlayer().hasPermission("bfc.bypass")) {
				MessageHandler.sendMessage(player, Messages.placeholders(Messages.PROTECTED, bannedPlayer.getPlayer().getDisplayName(), null, null));
				return true;
			}
		}

		if(!allowBan) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;
		} else {
			final String claimOwner = claim.getOwnerName();

			final int sizeRadius = Math.max(claim.getHeight(), claim.getWidth());
			final Location greaterCorner = claim.getGreaterBoundaryCorner();
			final Location lesserCorner = claim.getLesserBoundaryCorner();

			if(setClaimData(player, claim.getID().toString(), bannedPlayer.getUniqueId().toString(), true)) {
				if(bannedPlayer.isOnline()) {
					if(GriefPrevention.instance.dataStore.getClaimAt(bannedPlayer.getPlayer().getLocation(), true, claim) != null) {
						if(GriefPrevention.instance.dataStore.getClaimAt(bannedPlayer.getPlayer().getLocation(), true, claim) == claim) {
							final Location bannedLoc = bannedPlayer.getPlayer().getLocation();
							final LocationFinder lf = new LocationFinder(greaterCorner, lesserCorner, bannedLoc.getWorld().getUID(), sizeRadius);

							Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getInstance(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
								if(randomCircumferenceRadiusLoc == null) {
									if(Config.SAFE_LOCATION == null) { bannedPlayer.getPlayer().teleport(bannedLoc.getWorld().getSpawnLocation()); }
									else { bannedPlayer.getPlayer().teleport(Config.SAFE_LOCATION); }
								}
								else { bannedPlayer.getPlayer().teleport(randomCircumferenceRadiusLoc);	}

								MessageHandler.sendMessage(bannedPlayer.getPlayer(), Messages.placeholders(Messages.BANNED_TARGET, bannedPlayer.getName(), player.getDisplayName(), claimOwner));

							}));
						}
					}
				}

				MessageHandler.sendMessage(player, Messages.placeholders(Messages.BANNED, bannedPlayer.getName(), null, null));

			} else {
				MessageHandler.sendMessage(player, Messages.ALREADY_BANNED);
			}
		}
		return true;
	}

	private boolean setClaimData(Player player, String claimID, String bannedUUID, boolean add) {
		final ClaimData claimData = new ClaimData();

		return claimData.setClaimData(player, claimID, bannedUUID, add);
	}

}
