package no.vestlandetmc.BanFromClaim.commands.griefdefender;

import org.bukkit.Location;
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

public class BfcAllCommandGD implements CommandExecutor {

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

		final boolean isManager = claim.getUserTrusts(TrustTypes.MANAGER).contains(player.getUniqueId());
		final boolean isOwner = claim.getOwnerUniqueId().equals(player.getUniqueId());
		boolean allowBan = false;

		if(claim.isWilderness()) {
			MessageHandler.sendMessage(player, Messages.OUTSIDE_CLAIM);
			return true;
		}

		if(isOwner || isManager) { allowBan = true; }
		else if(player.hasPermission("bfc.admin")) { allowBan = true; }

		if(allowBan) {
			claimData.banAll(claim.getUniqueId().toString());

			if(claimData.isAllBanned(claim.getUniqueId().toString())) {
				MessageHandler.sendMessage(player, Messages.BAN_ALL);
			} else { MessageHandler.sendMessage(player, Messages.UNBAN_ALL); }

		} else {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;
		}

		return true;
	}
}
