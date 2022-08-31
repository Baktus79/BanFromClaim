package no.vestlandetmc.BanFromClaim.commands.regiondefence;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.rd.handler.Region;
import no.vestlandetmc.rd.handler.RegionManager;

public class BfcAllCommandRD implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			MessageHandler.sendConsole("&cThis command can only be used in-game.");
			return true;
		}

		final Player player = (Player) sender;
		final Location loc = player.getLocation();
		final Region rg = RegionManager.getRegion(loc);
		final ClaimData claimData = new ClaimData();

		if(rg == null) {
			MessageHandler.sendMessage(player, Messages.OUTSIDE_CLAIM);
			return true;
		}

		final boolean isManager = rg.hasManagerTrust(player.getUniqueId());
		final boolean isOwner = rg.isOwner(player.getUniqueId());
		boolean allowBan = false;

		if(isOwner || isManager) { allowBan = true; }
		else if(player.hasPermission("bfc.admin")) { allowBan = true; }

		if(allowBan) {
			claimData.banAll(rg.getRegionID().toString());

			if(claimData.isAllBanned(rg.getRegionID().toString())) {
				MessageHandler.sendMessage(player, Messages.BAN_ALL);
			} else { MessageHandler.sendMessage(player, Messages.UNBAN_ALL); }

		} else {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;
		}

		return true;
	}
}
