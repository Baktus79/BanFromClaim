package no.vestlandetmc.BanFromClaim.commands;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BfcAllCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if (!(sender instanceof Player player)) {
			MessageHandler.sendConsole("&cThis command can only be used in-game.");
			return true;
		}

		final ClaimData claimData = new ClaimData();
		final RegionHook region = BfcPlugin.getHookManager().getActiveRegionHook();
		final String regionID = region.getRegionID(player);

		if (regionID == null) {
			MessageHandler.sendMessage(player, Messages.OUTSIDE_CLAIM);
			return true;
		}

		final boolean allowBan = player.hasPermission("bfc.admin") || region.isOwner(player, regionID) || region.isManager(player, regionID);

		if (allowBan) {
			claimData.banAll(regionID);

			if (claimData.isAllBanned(regionID)) {
				MessageHandler.sendMessage(player, Messages.BAN_ALL);
			} else {
				MessageHandler.sendMessage(player, Messages.UNBAN_ALL);
			}

		} else {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;
		}

		return true;
	}
}
