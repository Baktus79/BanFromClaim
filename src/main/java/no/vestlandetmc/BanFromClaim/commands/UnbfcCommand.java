package no.vestlandetmc.BanFromClaim.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;

public class UnbfcCommand implements CommandExecutor {

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
			MessageHandler.sendMessage(player, "&cYou have to type in a player name.");
			return true;
		}

		if(claim == null) {
			MessageHandler.sendMessage(player, "&cPlease make sure you are standing inside your claim.");
			return true;
		}

		final String accessDenied = claim.allowGrantPermission(player);
		boolean allowBan = false;

		if(accessDenied == null) { allowBan = true; }
		if(player.hasPermission("bfc.admin")) { allowBan = true; }

		OfflinePlayer bPlayer = null;

		if(!allowBan) {
			MessageHandler.sendMessage(player, "&cThis is not your claim or you do not have PermissionTrust.");
			return true;

		} else {
			final String claimOwner = claim.getOwnerName();
			final String claimID = claim.getID().toString();

			if(listPlayers(claim.getID().toString()) != null) {
				for(final String bp : listPlayers(claim.getID().toString())) {
					final OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(bp));
					if(bannedPlayer.getName().equalsIgnoreCase(args[0])) {
						bPlayer = bannedPlayer;
						if(setClaimData(player, claimID, bp, false)) {
							MessageHandler.sendMessage(player, "&6" + bannedPlayer.getName() + " &ehas been unbanned from your claim!");
							if(bannedPlayer.isOnline()) {
								MessageHandler.sendMessage(bannedPlayer.getPlayer(), "&eYou have been unbanned from &6" + claimOwner + "'s &eclaim by &6" + player.getName() + "&e.");
							}
							return true;
						}
					}
				}
			}
		}

		if(bPlayer == null) { MessageHandler.sendMessage(player, "&4" + args[0] + " &cis not a valid player name or not banned at your claim."); }

		return true;
	}

	private List<String> listPlayers(String claimID) {
		final ClaimData claimData = new ClaimData();

		return claimData.bannedPlayers(claimID);
	}

	private boolean setClaimData(Player player, String claimID, String bannedUUID, boolean add) {
		final ClaimData claimData = new ClaimData();

		return claimData.setClaimData(player, claimID, bannedUUID, add);
	}

}
