package no.vestlandetmc.BanFromClaim.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.Permissions;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@NullMarked
@SuppressWarnings({"deprecation", "UnstableApiUsage"})
public class BfcCommand implements BasicCommand {

	@Override
	public void execute(CommandSourceStack stack, String[] args) {
		final CommandSender sender = stack.getSender();

		// /bfc reload | /bfc rl (console + players)
		if (args.length >= 1 && isReloadArg(args[0])) {
			handleReload(sender);
			return;
		}

		// Everything else requires a player (claim context)
		if (!(sender instanceof Player player)) {
			MessageHandler.sendConsole("&cThis command can only be used in-game (except: /bfc reload).");
			return;
		}

		// Enforce ban permission for the normal /bfc <player> path
		if (!player.hasPermission(Permissions.BAN.getName()) && !player.hasPermission("bfc.admin")) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return;
		}

		if (args.length == 0) {
			MessageHandler.sendMessage(player, Messages.NO_ARGUMENTS);
			return;
		}

		final RegionHook region = BfcPlugin.getHookManager().getActiveRegionHook();
		if (region == null) {
			MessageHandler.sendMessage(player, "&cNo supported protection hook is active.");
			return;
		}

		final String regionID = region.getRegionID(player);
		if (regionID == null) {
			MessageHandler.sendMessage(player, Messages.OUTSIDE_CLAIM);
			return;
		}

		final OfflinePlayer bannedPlayer = Bukkit.getOfflinePlayer(args[0]);

		final boolean allowBan =
				player.hasPermission("bfc.admin")
						|| region.isOwner(player, regionID)
						|| region.isManager(player, regionID);

		if (bannedPlayer.getUniqueId().equals(player.getUniqueId())) {
			MessageHandler.sendMessage(player, Messages.BAN_SELF);
			return;
		}

		if (!bannedPlayer.hasPlayedBefore()) {
			MessageHandler.sendMessage(player,
					Messages.placeholders(Messages.UNVALID_PLAYERNAME, args[0], player.getDisplayName(), null));
			return;
		}

		if (region.isOwner(bannedPlayer, regionID)) {
			MessageHandler.sendMessage(player, Messages.BAN_OWNER);
			return;
		}

		if (bannedPlayer.isOnline() && bannedPlayer.getPlayer() != null && bannedPlayer.getPlayer().hasPermission("bfc.bypass")) {
			MessageHandler.sendMessage(player,
					Messages.placeholders(Messages.PROTECTED, bannedPlayer.getPlayer().getDisplayName(), null, null));
			return;
		}

		if (!allowBan) {
			MessageHandler.sendMessage(player, Messages.NO_ACCESS);
			return;
		}

		final String claimOwner = region.getClaimOwnerName(regionID);

		if (!setClaimData(regionID, bannedPlayer.getUniqueId().toString(), true)) {
			MessageHandler.sendMessage(player, Messages.ALREADY_BANNED);
			return;
		}

		// If target is online and currently inside the claim, immediately move them to safestop/spawn (SYNC)
		if (bannedPlayer.isOnline() && bannedPlayer.getPlayer() != null) {
			final Player target = bannedPlayer.getPlayer();

			if (region.isInsideRegion(target, regionID)) {
				final Location dest = (Config.SAFE_LOCATION != null)
						? Config.SAFE_LOCATION
						: target.getWorld().getSpawnLocation();

				Bukkit.getScheduler().runTask(BfcPlugin.getPlugin(), () -> {
					target.teleport(dest);
					MessageHandler.sendMessage(target,
							Messages.placeholders(Messages.BANNED_TARGET,
									bannedPlayer.getName(),
									player.getDisplayName(),
									claimOwner));
				});
			}
		}

		// Feedback to banner
		MessageHandler.sendMessage(player, Messages.placeholders(Messages.BANNED, bannedPlayer.getName(), null, null));
	}

	private static boolean isReloadArg(String arg) {
		return arg.equalsIgnoreCase("reload") || arg.equalsIgnoreCase("rl");
	}

	private static void handleReload(CommandSender sender) {
		// Console always allowed (optional but recommended)
		if (!(sender instanceof ConsoleCommandSender)
				&& !sender.hasPermission("bfc.admin")
				&& !sender.hasPermission("bfc.reload")) {

			if (sender instanceof Player p) {
				MessageHandler.sendMessage(p, "&cYou do not have permission to do that.");
			} else {
				MessageHandler.sendConsole("&cYou do not have permission to do that.");
			}
			return;
		}

		Bukkit.getScheduler().runTask(BfcPlugin.getPlugin(), () -> {
			try {
				BfcPlugin.getPlugin().reloadConfig(); // config.yml
				Config.initialize();                  // your config loader
				Messages.reload();                    // messages.yml

				if (sender instanceof Player p) {
					MessageHandler.sendMessage(p, "&aBanFromClaim reloaded (config + messages).");
				} else {
					MessageHandler.sendConsole("&aBanFromClaim reloaded (config + messages).");
				}
			} catch (Throwable t) {
				BfcPlugin.getPlugin().getLogger().severe("Reload FAILED:");
				t.printStackTrace();

				if (sender instanceof Player p) {
					MessageHandler.sendMessage(p, "&cReload failed. Check console for error.");
				} else {
					MessageHandler.sendConsole("&cReload failed. Check console for error.");
				}
			}
		});
	}

	@Override
	public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
		if (args.length == 1) {
			String input = args[0].toLowerCase();

			// Suggest reload / rl
			if ("reload".startsWith(input)) return List.of("reload");
			if ("rl".startsWith(input)) return List.of("rl");

			// Suggest player names
			return Bukkit.getOnlinePlayers().stream()
					.map(Player::getName)
					.filter(name -> name.toLowerCase().startsWith(input))
					.sorted(String.CASE_INSENSITIVE_ORDER)
					.toList();
		}

		return List.of();
	}

	@Override
	public @Nullable String permission() {
		// IMPORTANT: return null so /bfc reload is not blocked by bfc.ban
		return null;
	}

	private boolean setClaimData(String claimID, String bannedUUID, boolean add) {
		final ClaimData claimData = new ClaimData();
		return claimData.setClaimData(claimID, bannedUUID, add);
	}
}