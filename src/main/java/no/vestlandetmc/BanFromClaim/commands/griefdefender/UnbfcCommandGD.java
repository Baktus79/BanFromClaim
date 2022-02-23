package no.vestlandetmc.BanFromClaim.commands.griefdefender;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;

import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;

public class UnbfcCommandGD implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			MessageHandler.sendConsole("&cThis command can only be used in-game.");
			return true;
		}

		final Player player = (Player) sender;
		final Location loc = player.getLocation();
		final Vector3i vector = Vector3i.from(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		final Core gd = GriefDefender.getCore();
		final Claim claim = gd.getClaimManager(loc.getWorld().getUID()).getClaimAt(vector);

		if(args.length == 0) {
			MessageHandler.sendMessage(player, Messages.NO_ARGUMENTS);
			return true;
		}

		if(claim.isWilderness()) {
			MessageHandler.sendMessage(player, Messages.OUTSIDE_CLAIM);
			return true;
		}

		final boolean isManager = claim.getUserTrusts(TrustTypes.MANAGER).contains(player.getUniqueId());
		final boolean isOwner = claim.getOwnerUniqueId().equals(player.getUniqueId());
		boolean allowBan = false;

		if(isOwner || isManager) { allowBan = true; }
		else if(player.hasPermission("bfc.admin")) { allowBan = true; }

		OfflinePlayer bPlayer = null;

		if(!allowBan) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;

		} else {
			final String claimOwner = claim.getOwnerName();
			final String claimID = claim.getUniqueId().toString();

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
