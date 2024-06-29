package no.vestlandetmc.BanFromClaim.commands;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class UnbfcCommand implements CommandExecutor {

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

		final boolean allowBan = player.hasPermission("bfc.admin") || region.isOwner(player, regionID) || region.isManager(player, regionID);

		OfflinePlayer bPlayer = null;

		if (!allowBan) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;

		} else {
			final String claimOwner = region.getClaimOwnerName(regionID);

			if (listPlayers(regionID) != null) {
				for (final String bp : listPlayers(regionID)) {
					final OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(bp));
					if (bannedPlayer.getName().equalsIgnoreCase(args[0])) {
						bPlayer = bannedPlayer;
						if (setClaimData(regionID, bp, false)) {
							MessageHandler.sendMessage(player, Messages.placeholders(Messages.UNBANNED, bannedPlayer.getName(), player.getDisplayName(), claimOwner));
							if (bannedPlayer.isOnline()) {
								MessageHandler.sendMessage(bannedPlayer.getPlayer(), Messages.placeholders(Messages.UNBANNED_TARGET, bannedPlayer.getName(), player.getDisplayName(), claimOwner));
							}
							return true;
						}
					}
				}
			}
		}

		if (bPlayer == null) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.NOT_BANNED, args[0], player.getDisplayName(), null));
		}

		return true;
	}

	private List<String> listPlayers(String claimID) {
		final ClaimData claimData = new ClaimData();

		return claimData.bannedPlayers(claimID);
	}

	private boolean setClaimData(String claimID, String bannedUUID, boolean add) {
		final ClaimData claimData = new ClaimData();

		return claimData.setClaimData(claimID, bannedUUID, add);
	}

}
