package no.vestlandetmc.BanFromClaim.utils;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.hooks.RegionHook;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public final class BanEnforcer {

    private BanEnforcer() {}

    // Reuse a single instance (prevents repeated file init / reload calls)
    private static final ClaimData CLAIM_DATA = new ClaimData();

    public static void enforceAt(Player p, Location loc) {
        if (p == null || loc == null) return;

        final RegionHook hook = BfcPlugin.getHookManager().getActiveRegionHook();
        if (hook == null) return;

        final String regionId = hook.getRegionID(loc);
        if (regionId == null) return;

        if (canBypass(p)) return;
        if (hook.hasTrust(p, regionId)) return;

        final boolean banned =
                CLAIM_DATA.isAllBanned(regionId) || isPlayerBanned(p.getUniqueId(), regionId);

        if (!banned) return;

        teleportToBanArea(p, hook, regionId);
    }

    private static boolean canBypass(Player p) {
        return p.hasPermission("bfc.bypass") || p.getGameMode() == GameMode.SPECTATOR;
    }

    private static boolean isPlayerBanned(UUID uuid, String regionId) {
        final List<String> list = CLAIM_DATA.bannedPlayers(regionId);
        return list != null && list.contains(uuid.toString());
    }

    /**
     * Instant enforcement target (SAFE_LOCATION/spawn).
     * For join/respawn/world-change/command reroutes, always send to banned teleport target.
     */
    public static void teleportToBanArea(Player p, RegionHook hook, String regionId) {
        if (p == null) return;

        final World world = p.getWorld();
        Location dest = Config.getBannedTeleportTarget(world);

        // Safety: if misconfigured and safespot is inside the same region, fallback to world spawn.
        try {
            final String destRegion = hook.getRegionID(dest);
            if (destRegion != null && destRegion.equals(regionId)) {
                dest = world.getSpawnLocation();
            }
        } catch (Throwable ignored) {}

        final Location finalDest = dest.clone();

        // MUST be sync
        Bukkit.getScheduler().runTask(BfcPlugin.getPlugin(), () -> {
            if (p.isOnline()) p.teleport(finalDest);
        });
    }
}