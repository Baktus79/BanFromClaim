package no.vestlandetmc.BanFromClaim.commands.griefprevention;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;

public class BfcAllCommand implements CommandExecutor {

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
		final ClaimData claimData = new ClaimData();

		if(claim == null) {
			MessageHandler.sendMessage(player, Messages.OUTSIDE_CLAIM);
			return true;
		}

		final String accessDenied = claim.allowGrantPermission(player);
		boolean allowBan = false;

		if(accessDenied == null) { allowBan = true; }
		else if(player.hasPermission("bfc.admin")) { allowBan = true; }

		if(allowBan) {
			claimData.banAll(claim.getID().toString());

			if(claimData.isAllBanned(claim.getID().toString())) {
				MessageHandler.sendMessage(player, Messages.BAN_ALL);
			} else { MessageHandler.sendMessage(player, Messages.UNBAN_ALL); }

		} else {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;
		}

		return true;
	}
}
