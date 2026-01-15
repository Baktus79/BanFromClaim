package no.vestlandetmc.BanFromClaim.hooks;

import lombok.Getter;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

@Getter
public class HookManager {

	private final RegionHook activeRegionHook;

	public HookManager() {

		if (isPluginAvailable("GriefPrevention")) {
			activeRegionHook = new GriefPreventionHook();
			BfcPlugin.getPlugin().getLogger().info("GriefPrevention detected and hooked.");

		} else if (isPluginAvailable("GriefDefender")) {
			activeRegionHook = new GriefDefenderHook();
			BfcPlugin.getPlugin().getLogger().info("GriefDefender detected and hooked.");

		} else {
			activeRegionHook = null;
			BfcPlugin.getPlugin().getLogger().warning("No supported protection plugins found! (GriefPrevention / GriefDefender required)");
			Bukkit.getPluginManager().disablePlugin(BfcPlugin.getPlugin());
			return;
		}

		if (activeRegionHook instanceof Listener listener) {
			Bukkit.getPluginManager().registerEvents(listener, BfcPlugin.getPlugin());
		}
	}

	private boolean isPluginAvailable(String pluginName) {
		final Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin(pluginName);
		return plugin != null && plugin.isEnabled();
	}
}