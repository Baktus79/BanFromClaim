package no.vestlandetmc.BanFromClaim;

import lombok.Getter;
import no.vestlandetmc.BanFromClaim.commands.*;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.hooks.HookManager;
import no.vestlandetmc.BanFromClaim.listener.CombatMode;
import no.vestlandetmc.BanFromClaim.listener.PlayerListener;
import no.vestlandetmc.BanFromClaim.listener.RegionListener;
import no.vestlandetmc.BanFromClaim.schedulers.CombatScheduler;
import no.vestlandetmc.BanFromClaim.utils.UpdateNotification;
import org.bstats.bukkit.Metrics;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

public class BfcPlugin extends JavaPlugin {

	@Getter
	private static BfcPlugin plugin;
	@Getter
	private static HookManager hookManager;

	private FileConfiguration data;

	@Override
	public void onEnable() {
		plugin = this;

		MessageHandler.sendConsole("&2 ___ ___ ___");
		MessageHandler.sendConsole("&2| _ ) __/ __|        &8" + getDescription().getName() + " v" + getDescription().getVersion());
		MessageHandler.sendConsole("&2| _ \\ _| (__         &8Author: " + getDescription().getAuthors().toString().replace("[", "").replace("]", ""));
		MessageHandler.sendConsole("&2|___/_| \\___|");
		MessageHandler.sendConsole("");

		Config.initialize();
		hookManager = new HookManager();

		this.getServer().getPluginManager().registerEvents(new RegionListener(), this);
		this.getCommand("banfromclaim").setExecutor(new BfcCommand());
		this.getCommand("unbanfromclaim").setExecutor(new UnbfcCommand());
		this.getCommand("banfromclaimlist").setExecutor(new BfclistCommand());
		this.getCommand("banfromclaimall").setExecutor(new BfcAllCommand());

		if (Config.KICKMODE) {
			this.getCommand("kickfromclaim").setExecutor(new KfcCommand());
		}

		this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		this.getCommand("bfcsafespot").setExecutor(new SafeSpot());

		createDatafile();
		Messages.initialize();
		ClaimData.createSection();

		if (Config.COMBAT_ENABLED) {
			this.getServer().getPluginManager().registerEvents(new CombatMode(), this);
			new CombatScheduler().runTaskTimer(this, 0L, 20L);
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				ClaimData.cleanDatafile();
			}

		}.runTaskTimer(this, 30 * 20L, 3600 * 20L);

		new UpdateNotification(70897) {

			@Override
			public void onUpdateAvailable() {
				MessageHandler.sendConsole("&c-----------------------");
				MessageHandler.sendConsole("&2[" + getDescription().getPrefix() + "] &7Version " + getLatestVersion() + " is now available!");
				MessageHandler.sendConsole("&2[" + getDescription().getPrefix() + "] &7Download the update at https://www.spigotmc.org/resources/" + getProjectId());
				MessageHandler.sendConsole("&c-----------------------");
			}
		}.runTaskAsynchronously(this);

		final int pluginId = 22441;
		final Metrics metrics = new Metrics(this, pluginId);

	}

	public FileConfiguration getDataFile() {
		return this.data;
	}

	public void createDatafile() {
		final File dataFile = new File(this.getDataFolder(), "data.dat");
		if (!dataFile.exists()) {
			dataFile.getParentFile().mkdirs();
			try {
				dataFile.createNewFile();
			} catch (final IOException e) {
				getLogger().severe(e.getMessage());
			}
		}

		data = new YamlConfiguration();
		try {
			data.load(dataFile);
		} catch (IOException | InvalidConfigurationException e) {
			getLogger().severe(e.getMessage());
		}
	}
}
