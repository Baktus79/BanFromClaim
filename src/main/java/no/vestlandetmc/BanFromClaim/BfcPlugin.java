package no.vestlandetmc.BanFromClaim;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import no.vestlandetmc.BanFromClaim.apiversions.VersionManager;
import no.vestlandetmc.BanFromClaim.commands.*;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.Permissions;
import no.vestlandetmc.BanFromClaim.hooks.HookManager;
import no.vestlandetmc.BanFromClaim.listener.CombatMode;
import no.vestlandetmc.BanFromClaim.listener.PlayerListener;
import no.vestlandetmc.BanFromClaim.listener.RegionListener;
import no.vestlandetmc.BanFromClaim.schedulers.CombatScheduler;
import no.vestlandetmc.BanFromClaim.utils.BanManager;
import no.vestlandetmc.BanFromClaim.utils.UpdateNotification;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class BfcPlugin extends JavaPlugin {

	@Getter
	private static BfcPlugin plugin;
	@Getter
	private static HookManager hookManager;
	@Getter
	private static VersionManager versionManager;
	@Getter
	private static FileConfiguration dataFile;
	@Getter
	private static BanManager banManager;

	@Override
	public void onEnable() {
		plugin = this;

		MessageHandler.sendConsole("&2 ___ ___ ___");
		MessageHandler.sendConsole("&2| _ ) __/ __|        &8" + getPluginMeta().getName() + " v" + getPluginMeta().getVersion());
		MessageHandler.sendConsole("&2| _ \\ _| (__         &8Author: " + getPluginMeta().getAuthors().toString().replace("[", "").replace("]", ""));
		MessageHandler.sendConsole("&2|___/_| \\___|");
		MessageHandler.sendConsole("");

		Config.initialize();
		Permissions.register();
		versionManager = new VersionManager();
		hookManager = new HookManager();
		banManager = new BanManager();

		this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
			commands.registrar().register(
					"banfromclaimall",
					"Ban all players from your claim.",
					List.of("bfca", "bfcall"),
					new BfcAllCommand());

			commands.registrar().register(
					"banfromclaim",
					"Ban a player from your claim.",
					List.of("bfc", "banfc"),
					new BfcCommand());

			commands.registrar().register(
					"banfromclaimlist",
					"Displays a list of banned players in your claim.",
					List.of("bfcl", "bfclist"),
					new BfclistCommand());

			commands.registrar().register(
					"bfcsafespot",
					"Set new safespot.",
					List.of("bfcs", "bfcsetsafe"),
					new SafeSpot());

			commands.registrar().register(
					"unbanfromclaim",
					"Unban a player from your claim.",
					List.of("ubfc", "unbanfc"),
					new UnbfcCommand());

			if (Config.KICKMODE) {
				commands.registrar().register(
						"kickfromclaim",
						"Kick a player from your claim.",
						List.of("kfc", "kickfc"),
						new KfcCommand());
			}
		});

		this.getServer().getPluginManager().registerEvents(new RegionListener(), this);
		this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);

		createDatafile();
		Messages.initialize();
		ClaimData.createSection();

		if (Config.COMBAT_ENABLED) {
			this.getServer().getPluginManager().registerEvents(new CombatMode(), this);
			new CombatScheduler().runTaskTimer(this, 0L, 20L);
		}

		new UpdateNotification("banfromclaim") {

			@Override
			public void onUpdateAvailable() {
				MessageHandler.sendConsole("&c-----------------------");
				MessageHandler.sendConsole("&2[BanFromClaim] &7Version " + getLatestVersion() + " is now available!");
				MessageHandler.sendConsole("&2[BanFromClaim] &7Download the update at https://modrinth.com/plugin/" + getProjectSlug());
				MessageHandler.sendConsole("&c-----------------------");
			}
		}.runTaskAsynchronously(this);

		final int pluginId = 22441;
		final Metrics metrics = new Metrics(this, pluginId);

	}

	public void createDatafile() {
		final File newDataFile = new File(this.getDataFolder(), "data.dat");
		if (!newDataFile.exists()) {
			newDataFile.getParentFile().mkdirs();
			try {
				newDataFile.createNewFile();
			} catch (final IOException e) {
				getLogger().severe(e.getMessage());
			}
		}

		dataFile = new YamlConfiguration();
		try {
			dataFile.load(newDataFile);
		} catch (IOException | InvalidConfigurationException e) {
			getLogger().severe(e.getMessage());
		}
	}
}
