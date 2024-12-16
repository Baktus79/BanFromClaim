package no.vestlandetmc.BanFromClaim.commands;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import no.vestlandetmc.BanFromClaim.utils.LocationFinder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BfcCommand implements CommandExecutor {

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
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

		final OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(args[0]);
		boolean allowBan = player.hasPermission("bfc.admin") || region.isOwner(player, regionID) || region.isManager(player, regionID);

		if (!bannedPlayer.isOnline()) {
			if (!bannedPlayer.hasPlayedBefore()) {
				MessageHandler.sendMessage(player, Messages.placeholders(Messages.UNVALID_PLAYERNAME, args[0], player.getDisplayName(), null));
				return true;
			} else if (bannedPlayer == player) {
				MessageHandler.sendMessage(player, Messages.BAN_SELF);
				return true;
			} else if (region.isOwner(bannedPlayer, regionID)) {
				MessageHandler.sendMessage(player, Messages.BAN_OWNER);
				return true;
			}
		} else {
			if (bannedPlayer.getPlayer().hasPermission("bfc.bypass")) {
				MessageHandler.sendMessage(player, Messages.placeholders(Messages.PROTECTED, bannedPlayer.getPlayer().getDisplayName(), null, null));
				return true;
			}
		}

		if (!allowBan) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return true;
		} else {
			final String claimOwner = region.getClaimOwnerName(regionID);

			final int sizeRadius = region.sizeRadius(regionID);
			final Location greaterCorner = region.getGreaterBoundaryCorner(regionID);
			final Location lesserCorner = region.getLesserBoundaryCorner(regionID);

			if (setClaimData(regionID, bannedPlayer.getUniqueId().toString(), true)) {
				if (bannedPlayer.isOnline()) {
					if (region.isInsideRegion(bannedPlayer.getPlayer(), regionID)) {
						final Location bannedLoc = bannedPlayer.getPlayer().getLocation();
						final LocationFinder lf = new LocationFinder(greaterCorner, lesserCorner, bannedLoc.getWorld().getUID(), sizeRadius);

						Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getPlugin(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
							if (randomCircumferenceRadiusLoc == null) {
								if (Config.SAFE_LOCATION == null) {
									bannedPlayer.getPlayer().teleport(bannedLoc.getWorld().getSpawnLocation());
								} else {
									bannedPlayer.getPlayer().teleport(Config.SAFE_LOCATION);
								}
							} else {
								bannedPlayer.getPlayer().teleport(randomCircumferenceRadiusLoc);
							}

							MessageHandler.sendMessage(bannedPlayer.getPlayer(), Messages.placeholders(Messages.BANNED_TARGET, bannedPlayer.getName(), player.getDisplayName(), claimOwner));

						}));
					}
				}

				MessageHandler.sendMessage(player, Messages.placeholders(Messages.BANNED, bannedPlayer.getName(), null, null));

			} else {
				MessageHandler.sendMessage(player, Messages.ALREADY_BANNED);
			}
		}
		return true;
	}

	private boolean setClaimData(String claimID, String bannedUUID, boolean add) {
		final ClaimData claimData = new ClaimData();
		return claimData.setClaimData(claimID, bannedUUID, add);
	}

}
