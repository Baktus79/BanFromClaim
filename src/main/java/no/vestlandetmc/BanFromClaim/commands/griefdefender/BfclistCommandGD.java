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

import com.flowpowered.math.vector.Vector3i;
import com.griefdefender.api.Core;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;

import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;

public class BfclistCommandGD implements CommandExecutor {

	int countTo = 5;
	int countFrom = 0;
	int number = 1;

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
		final ClaimData claimData = new ClaimData();

		if(args.length != 0) {
			if(isInt(args[0])) {
				this.number = Integer.parseInt(args[0]);
				this.countTo = 5 * number;
				this.countFrom = 5 * number - 5;
			}
			else {
				MessageHandler.sendMessage(player, Messages.UNVALID_NUMBER);
				return true;
			}
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

		int totalPage = 1;

		if(!allowBan) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;

		} else {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.LIST_HEADER, null, player.getDisplayName(), claim.getOwnerName()));

			if(claimData.isAllBanned(claim.getUniqueId().toString())) {
				MessageHandler.sendMessage(player, Messages.LIST_BAN_ALL);
				return true;
			}

			if(listPlayers(claim.getUniqueId().toString()) == null) {
				MessageHandler.sendMessage(player, Messages.placeholders(Messages.LIST_EMPTY, null, player.getDisplayName(), claim.getOwnerName()));
				return true;
			} else {
				totalPage = listPlayers(claim.getUniqueId().toString()).size() / 5 + 1;
				for(int i = 0; i < listPlayers(claim.getUniqueId().toString()).toArray().length; i++) {
					if(this.number > totalPage || this.number == 0) {
						this.countTo = 5 * totalPage;
						this.countFrom = 5 * totalPage - 5;
						this.number = totalPage;
					}

					if(i >= this.countFrom) {
						final String bp = (String) listPlayers(claim.getUniqueId().toString()).toArray()[i];
						final OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(bp));
						MessageHandler.sendMessage(player, "&6" + bannedPlayer.getName());

						if(i == this.countTo) {
							MessageHandler.sendMessage(player, "");
							MessageHandler.sendMessage(player, "&e<--- [&6" + this.number + "\\" + totalPage + "&e] --->");
							break;
						}

						continue;
					}
				}

				if(this.number == totalPage) {
					MessageHandler.sendMessage(player, "");
					MessageHandler.sendMessage(player, "&e<--- [&6" + totalPage + "\\" + totalPage + "&e] --->");
				}
			}
		}

		return true;
	}

	private List<String> listPlayers(String claimID) {
		final ClaimData claimData = new ClaimData();

		return claimData.bannedPlayers(claimID);
	}

	private boolean isInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}

}
