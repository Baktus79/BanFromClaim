package no.vestlandetmc.BanFromClaim.listener;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.ParticleHandler;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import no.vestlandetmc.BanFromClaim.utils.UpdateNotification;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.UUID;

/**
 * - Operator update notification
 * - Enforce bans even if a player logs in while standing inside a claim they're banned from
 * - Cleanup per-player caches to avoid memory growth over time
 */
public class PlayerListener implements Listener {

	private final ClaimData claimData = new ClaimData();

	@EventHandler(ignoreCancelled = true)
	public void playerJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();

		notifyOpIfUpdateAvailable(player);

		// Delay 1 tick so hooks/GP data are ready
		Bukkit.getScheduler().runTaskLater(BfcPlugin.getPlugin(), () -> enforceIfBannedOnJoin(player), 1L);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
		RegionListener.cleanup(event.getPlayer().getUniqueId());
	}

	private void notifyOpIfUpdateAvailable(Player player) {
		if (!player.isOp()) return;
		if (!UpdateNotification.isUpdateAvailable()) return;

		MessageHandler.sendMessage(player, "&2" + BfcPlugin.getPlugin().getPluginMeta().getName() + " &ais outdated. Update is available!");
		MessageHandler.sendMessage(player, "&aYour version is &2" + UpdateNotification.getCurrentVersion() + " &aand can be updated to version &2" + UpdateNotification.getLatestVersion());
		MessageHandler.sendMessage(player, "&aGet the new update at &2https://modrinth.com/plugin/" + UpdateNotification.getProjectSlug());
	}

	private void enforceIfBannedOnJoin(Player player) {
		if (player == null || !player.isOnline()) return;
		if (canBypass(player)) return;

		final RegionHook hook = BfcPlugin.getHookManager().getActiveRegionHook();
		if (hook == null) return;

		final Location loc = player.getLocation();
		final String regionId = hook.getRegionID(loc);
		if (regionId == null) return;

		// Trust always wins
		if (hook.hasTrust(player, regionId)) return;

		// Combat exception (same as RegionListener)
		final UUID ownerId = hook.getOwnerID(regionId);
		final boolean hasAttacked =
				ownerId != null
						&& CombatMode.attackerContains(player.getUniqueId())
						&& ownerId.equals(CombatMode.getAttacker(player.getUniqueId()));
		if (hasAttacked) return;

		if (!isBanned(player.getUniqueId(), regionId) && !claimData.isAllBanned(regionId)) return;

		// Teleport to configured destination immediately
		player.teleport(Config.getBannedTeleportTarget(player.getWorld()));

		sendDeniedFeedback(player, loc);
	}

	private boolean isBanned(UUID uuid, String claimId) {
		final List<String> banned = claimData.bannedPlayers(claimId);
		return banned != null && banned.contains(uuid.toString());
	}

	private boolean canBypass(Player player) {
		return player.hasPermission("bfc.bypass") || player.getGameMode() == GameMode.SPECTATOR;
	}

	private void sendDeniedFeedback(Player player, Location at) {
		if (player == null) return;

		if (!MessageHandler.spamMessageClaim.contains(player.getUniqueId().toString())) {
			MessageHandler.sendTitle(player, Messages.TITLE_MESSAGE, Messages.SUBTITLE_MESSAGE);
			MessageHandler.spamMessageClaim.add(player.getUniqueId().toString());

			Bukkit.getScheduler().runTaskLater(
					BfcPlugin.getPlugin(),
					() -> MessageHandler.spamMessageClaim.remove(player.getUniqueId().toString()),
					5L * 20L
			);
		}

		// Optional visual feedback
		try {
			new ParticleHandler(at).drawCircle(1, true);
		} catch (Throwable ignored) {
			// Best-effort only
		}
	}
}