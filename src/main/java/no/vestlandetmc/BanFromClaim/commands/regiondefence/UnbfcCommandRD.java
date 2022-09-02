package no.vestlandetmc.BanFromClaim.commands.regiondefence;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.rd.handler.Region;
import no.vestlandetmc.rd.handler.RegionManager;

public class UnbfcCommandRD implements CommandExecutor {

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

		final boolean isManager = rg.hasManagerTrust(player.getUniqueId());
		final boolean isOwner = rg.isOwner(player.getUniqueId());
		final OfflinePlayer owner = Bukkit.getOfflinePlayer(rg.getOwnerUUID());
		boolean allowBan = false;

		if(isOwner || isManager) { allowBan = true; }
		else if(player.hasPermission("bfc.admin")) { allowBan = true; }

		OfflinePlayer bPlayer = null;

		if(!allowBan) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;

		} else {
			final String claimOwner = owner.getName();
			final String claimID = rg.getRegionID().toString();

			if(listPlayers(claimID) != null) {
				for(final String bp : listPlayers(claimID)) {
					final OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(bp));
					if(bannedPlayer.getName().equalsIgnoreCase(args[0])) {
						bPlayer = bannedPlayer;
						if(setClaimData(player, claimID, bp, false)) {
							MessageHandler.sendMessage(player, Messages.placeholders(Messages.UNBANNED, bannedPlayer.getName(), player.getDisplayName(), claimOwner));
							if(bannedPlayer.isOnline()) {
								MessageHandler.sendMessage(bannedPlayer.getPlayer(), Messages.placeholders(Messages.UNBANNED_TARGET, bannedPlayer.getName(), player.getDisplayName(), claimOwner));
							}
							return true;
						}
					}
				}
			}
		}

		if(bPlayer == null) { MessageHandler.sendMessage(player, Messages.placeholders(Messages.NOT_BANNED, args[0], player.getDisplayName(), null)); }

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
