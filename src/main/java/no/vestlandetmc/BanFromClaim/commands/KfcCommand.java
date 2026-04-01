package no.vestlandetmc.BanFromClaim.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.Permissions;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import no.vestlandetmc.BanFromClaim.utils.LocationFinder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@NullMarked
public class KfcCommand implements BasicCommand {

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

		final Player kickedPlayer = Bukkit.getPlayer(args[0]);
		final boolean allowKick = player.hasPermission("bfc.admin") || region.isOwner(player, regionID) || region.isManager(player, regionID);

		if (kickedPlayer == null) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.UNVALID_PLAYERNAME, args[0], MessageHandler.compToString(player.displayName()), null));
			return;
		} else if (kickedPlayer == player) {
			MessageHandler.sendMessage(player, Messages.KICK_SELF);
			return;
		} else if (region.isOwner(kickedPlayer, regionID)) {
			MessageHandler.sendMessage(player, Messages.KICK_OWNER);
			return;
		}

		if (kickedPlayer.hasPermission("bfc.bypass")) {
			MessageHandler.sendMessage(player, Messages.placeholders(Messages.PROTECTED, MessageHandler.compToString(kickedPlayer.displayName()), null, null));
			return;
		}

		if (!allowKick) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
		} else {
			final String claimOwner = region.getClaimOwnerName(regionID);

			final int sizeRadius = region.sizeRadius(regionID);
			final Location greaterCorner = region.getGreaterBoundaryCorner(regionID);
			final Location lesserCorner = region.getLesserBoundaryCorner(regionID);

			if (region.isInsideRegion(kickedPlayer, regionID)) {
				final Location bannedLoc = kickedPlayer.getLocation();
				final LocationFinder lf = new LocationFinder(greaterCorner, lesserCorner, bannedLoc.getWorld().getUID(), sizeRadius);

				Bukkit.getScheduler().runTaskAsynchronously(BfcPlugin.getPlugin(), () -> lf.IterateCircumferences(randomCircumferenceRadiusLoc -> {
					if (randomCircumferenceRadiusLoc == null) {
						if (Config.SAFE_LOCATION == null) {
							kickedPlayer.teleport(bannedLoc.getWorld().getSpawnLocation());
						} else {
							kickedPlayer.teleport(Config.SAFE_LOCATION);
						}
					} else {
						kickedPlayer.teleport(randomCircumferenceRadiusLoc);
					}

					MessageHandler.sendMessage(kickedPlayer, Messages.placeholders(Messages.KICKED_TARGET, kickedPlayer.getName(), MessageHandler.compToString(player.displayName()), claimOwner));
				}));
			}
		}

		MessageHandler.sendMessage(player, Messages.placeholders(Messages.KICKED, kickedPlayer.getName(), null, null));
	}

	@Override
	public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
		return Bukkit.getOnlinePlayers().stream()
				.map(Player::getName)
				.filter(name -> name.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
				.toList();
	}

	@Override
	public boolean canUse(CommandSender sender) {
		return BasicCommand.super.canUse(sender);
	}

	@Override
	public @Nullable String permission() {
		return Permissions.KICK.getName();
	}
}
