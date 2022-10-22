package no.vestlandetmc.BanFromClaim.commands.griefdefender;

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

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.LocationFinder;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;

public class BfcCommandGD implements CommandExecutor {

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


		@SuppressWarnings("deprecation")
		final OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(args[0]);
		final boolean isManager = claim.getUserTrusts(TrustTypes.MANAGER).contains(player.getUniqueId());
		final boolean isOwner = claim.getOwnerUniqueId().equals(player.getUniqueId());
		boolean allowBan = false;

		if(isOwner || isManager) { allowBan = true; }
		else if(player.hasPermission("bfc.admin")) { allowBan = true; }

		if(!bannedPlayer.isOnline()) {
			if(!bannedPlayer.hasPlayedBefore()) {
				MessageHandler.sendMessage(player, Messages.placeholders(Messages.UNVALID_PLAYERNAME, args[0], player.getDisplayName(), null));
				return true;
			} else if(bannedPlayer == player) {
				MessageHandler.sendMessage(player, Messages.BAN_SELF);
				return true;
			} else if(bannedPlayer.getName().equals(claim.getOwnerName())) {
				MessageHandler.sendMessage(player, Messages.BAN_OWNER);
				return true;
			}
		} else {
			if(bannedPlayer.getPlayer().hasPermission("bfc.bypass")) {
				MessageHandler.sendMessage(player, Messages.placeholders(Messages.PROTECTED, bannedPlayer.getPlayer().getDisplayName(), null, null));
				return true;
			}
		}

		if(!allowBan) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;
		} else {
			final String claimOwner = claim.getOwnerName();
			final int sizeRadius = Math.max(claim.getLength(), claim.getWidth());

			final Location greaterCorner = new Location(
					Bukkit.getWorld(claim.getWorldUniqueId()),
					claim.getGreaterBoundaryCorner().getX(),
					64D,
					claim.getGreaterBoundaryCorner().getZ());

			final Location lesserCorner = new Location(
					Bukkit.getWorld(claim.getWorldUniqueId()),
					claim.getLesserBoundaryCorner().getX(),
					64D,
					claim.getLesserBoundaryCorner().getZ());

			if(setClaimData(player, claim.getUniqueId().toString(), bannedPlayer.getUniqueId().toString(), true)) {
				if(bannedPlayer.isOnline()) {
					final Location bannedLoc = bannedPlayer.getPlayer().getLocation();
					final Vector3i bannedVec = Vector3i.from(bannedLoc.getBlockX(), bannedLoc.getBlockY(), bannedLoc.getBlockZ());
					if(claim.contains(bannedVec)) {
						final LocationFinder lf = new LocationFinder(greaterCorner, lesserCorner, claim.getWorldUniqueId(), sizeRadius);

						Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getInstance(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
							if(randomCircumferenceRadiusLoc == null) {
								if(Config.SAFE_LOCATION == null) { bannedPlayer.getPlayer().teleport(bannedLoc.getWorld().getSpawnLocation()); }
								else { bannedPlayer.getPlayer().teleport(Config.SAFE_LOCATION); }
							}
							else { bannedPlayer.getPlayer().teleport(randomCircumferenceRadiusLoc);	}

							MessageHandler.sendMessage(bannedPlayer.getPlayer(), Messages.placeholders(Messages.BANNED_TARGET, bannedPlayer.getName(), player.getDisplayName(), claimOwner));

						}));
					}
				}

				MessageHandler.sendMessage(player, Messages.placeholders(Messages.BANNED, bannedPlayer.getName(), null, null));

			} else { MessageHandler.sendMessage(player, Messages.ALREADY_BANNED); }

		}
		return true;
	}

	private boolean setClaimData(Player player, String claimID, String bannedUUID, boolean add) {
		final ClaimData claimData = new ClaimData();

		return claimData.setClaimData(player, claimID, bannedUUID, add);
	}

}
