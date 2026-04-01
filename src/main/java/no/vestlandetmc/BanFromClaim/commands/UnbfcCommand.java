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
public class UnbfcCommand implements BasicCommand {

	@Override
	public void execute(CommandSourceStack commandSourceStack, String[] args) {
		if (!(commandSourceStack.getSender() instanceof Player player)) {
			MessageHandler.sendConsole("&cThis command can only be used in-game.");
			return;
		}

		final RegionHook region = BfcPlugin.getHookManager().getActiveRegionHook();
		final String regionID = region.getRegionID(player);

		if (args.length == 0) {
			MessageHandler.sendMessage(player, Messages.NO_ARGUMENTS);
			return;
		}

		if (regionID == null) {
			MessageHandler.sendMessage(player, Messages.OUTSIDE_CLAIM);
			return;
		}

		final boolean allowBan = player.hasPermission("bfc.admin") || region.isOwner(player, regionID) || region.isManager(player, regionID);

		OfflinePlayer bPlayer = null;

		if (!allowBan) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return;

		} else {
			final String claimOwner = region.getClaimOwnerName(regionID);

			if (listPlayers(regionID) != null) {
				for (final String bp : listPlayers(regionID)) {
					final OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(UUID.fromString(bp));
					final String bannedPlayerName = bannedPlayer.getName();

					if (bannedPlayerName == null) {
						setClaimData(regionID, bp, false);
					} else if (bannedPlayerName.equalsIgnoreCase(args[0])) {
						bPlayer = bannedPlayer;
						if (setClaimData(regionID, bp, false)) {
							MessageHandler.sendMessage(player, Messages.placeholders(Messages.UNBANNED, bannedPlayer.getName(), MessageHandler.compToString(player.displayName()), claimOwner));
							if (bannedPlayer.isOnline()) {
								MessageHandler.sendMessage(bannedPlayer.getPlayer(), Messages.placeholders(Messages.UNBANNED_TARGET, bannedPlayer.getName(), MessageHandler.compToString(player.displayName()), claimOwner));
							}
							return;
						}
					}
				}
			}
		}

		if (bPlayer == null) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.NOT_BANNED, args[0], MessageHandler.compToString(player.displayName()), null));
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
		return Permissions.UNBAN.getName();
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
