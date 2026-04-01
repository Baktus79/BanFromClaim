package no.vestlandetmc.BanFromClaim.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.Permissions;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@NullMarked
public class BfclistCommand implements BasicCommand {

	private int countTo = 5;
	private int countFrom = 0;
	private int number = 1;

	@Override
	public void execute(CommandSourceStack commandSourceStack, String[] args) {
		if (!(commandSourceStack.getSender() instanceof Player player)) {
			MessageHandler.sendConsole("&cThis command can only be used in-game.");
			return;
		}

		final ClaimData claimData = new ClaimData();
		final RegionHook region = BfcPlugin.getHookManager().getActiveRegionHook();
		final String regionID = region.getRegionID(player);

		if (args.length != 0) {
			if (isInt(args[0])) {
				this.number = Integer.parseInt(args[0]);
				this.countTo = 5 * number;
				this.countFrom = 5 * number - 5;
			} else {
				MessageHandler.sendMessage(player, Messages.UNVALID_NUMBER);
				return;
			}
		}

		if (regionID == null) {
			MessageHandler.sendMessage(player, Messages.OUTSIDE_CLAIM);
			return;
		}

		boolean allowBan = player.hasPermission("bfc.admin") || region.isOwner(player, regionID) || region.isManager(player, regionID);

		int totalPage;

		if (!allowBan) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
		} else {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.LIST_HEADER, null, MessageHandler.compToString(player.displayName()), region.getClaimOwnerName(regionID)));

			if (claimData.isAllBanned(regionID)) {
				MessageHandler.sendMessage(player, Messages.LIST_BAN_ALL);
				return;
			}

			if (listPlayers(regionID) == null) {
				MessageHandler.sendMessage(player, Messages.placeholders(Messages.LIST_EMPTY, null, MessageHandler.compToString(player.displayName()), region.getClaimOwnerName(regionID)));
			} else {
				totalPage = listPlayers(regionID).size() / 5 + 1;
				for (int i = 0; i < listPlayers(regionID).toArray().length; i++) {
					if (this.number > totalPage || this.number == 0) {
						this.countTo = 5 * totalPage;
						this.countFrom = 5 * totalPage - 5;
						this.number = totalPage;
					}

					if (i >= this.countFrom) {
						final String bp = (String) listPlayers(regionID).toArray()[i];
						final OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(bp));
						final String bannedPlayerName = bannedPlayer.getName() == null ? bp : bannedPlayer.getName();
						MessageHandler.sendMessage(player, "&6" + bannedPlayerName);

						if (i == this.countTo) {
							MessageHandler.sendMessage(player, "");
							MessageHandler.sendMessage(player, "&e<--- [&6" + this.number + "\\" + totalPage + "&e] --->");
							break;
						}
					}
				}

				if (this.number == totalPage) {
					MessageHandler.sendMessage(player, "");
					MessageHandler.sendMessage(player, "&e<--- [&6" + totalPage + "\\" + totalPage + "&e] --->");
				}
			}
		}
	}

	@Override
	public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
		return BasicCommand.super.suggest(commandSourceStack, args);
	}

	@Override
	public boolean canUse(CommandSender sender) {
		return BasicCommand.super.canUse(sender);
	}

	@Override
	public @Nullable String permission() {
		return Permissions.LIST.getName();
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
