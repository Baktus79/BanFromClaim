package no.vestlandetmc.BanFromClaim;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import no.vestlandetmc.BanFromClaim.commands.SafeSpot;
import no.vestlandetmc.BanFromClaim.commands.griefdefender.BfcCommandGD;
import no.vestlandetmc.BanFromClaim.commands.griefdefender.BfclistCommandGD;
import no.vestlandetmc.BanFromClaim.commands.griefdefender.UnbfcCommandGD;
import no.vestlandetmc.BanFromClaim.commands.griefprevention.BfcCommand;
import no.vestlandetmc.BanFromClaim.commands.griefprevention.BfclistCommand;
import no.vestlandetmc.BanFromClaim.commands.griefprevention.UnbfcCommand;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.config.Messages;
import no.vestlandetmc.BanFromClaim.handler.CombatScheduler;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.UpdateNotification;
import no.vestlandetmc.BanFromClaim.listener.CombatMode;
import no.vestlandetmc.BanFromClaim.listener.GDListener;
import no.vestlandetmc.BanFromClaim.listener.GPListener;
import no.vestlandetmc.BanFromClaim.listener.PlayerListener;

public class BfcPlugin extends JavaPlugin {

	public static BfcPlugin instance;

	private File dataFile;
	private FileConfiguration data;

	public static BfcPlugin getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;

		MessageHandler.sendConsole("&2 ___ ___ ___");
		MessageHandler.sendConsole("&2| _ ) __/ __|        &8" + getDescription().getName() + " v" + getDescription().getVersion());
		MessageHandler.sendConsole("&2| _ \\ _| (__         &8Author: " + getDescription().getAuthors().toString().replace("[", "").replace("]", ""));
		MessageHandler.sendConsole("&2|___/_| \\___|");
		MessageHandler.sendConsole("");

		if(getServer().getPluginManager().getPlugin("GriefPrevention") != null) {
			MessageHandler.sendConsole("&2[" + getDescription().getPrefix() + "] &7Successfully hooked into &eGriefPrevention");
			MessageHandler.sendConsole("");

			this.getServer().getPluginManager().registerEvents(new GPListener(), this);
			this.getCommand("banfromclaim").setExecutor(new BfcCommand());
			this.getCommand("unbanfromclaim").setExecutor(new UnbfcCommand());
			this.getCommand("banfromclaimlist").setExecutor(new BfclistCommand());

		} else if(getServer().getPluginManager().getPlugin("GriefDefender") != null) {
			MessageHandler.sendConsole("&2[" + getDescription().getPrefix() + "] &7Successfully hooked into &eGriefDefender");
			MessageHandler.sendConsole("");

			this.getServer().getPluginManager().registerEvents(new GDListener(), this);
			this.getCommand("banfromclaim").setExecutor(new BfcCommandGD());
			this.getCommand("unbanfromclaim").setExecutor(new UnbfcCommandGD());
			this.getCommand("banfromclaimlist").setExecutor(new BfclistCommandGD());

		} else {
			MessageHandler.sendConsole("&2[" + getDescription().getPrefix() + "] &cNo supported claimsystem was found.");
			MessageHandler.sendConsole("");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
		this.getCommand("bfcsafespot").setExecutor(new SafeSpot());

		createDatafile();
		Messages.initialize();
		Config.initialize();
		ClaimData.createSection();

		if(Config.COMBAT_ENABLED) {
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
	}

	@Override
	public void onDisable() {

	}

	public FileConfiguration getDataFile() {
		return this.data;
	}

	public void createDatafile() {
		dataFile = new File(this.getDataFolder(), "data.dat");
		if (!dataFile.exists()) {
			dataFile.getParentFile().mkdirs();
			try {
				dataFile.createNewFile();
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}

		data = new YamlConfiguration();
		try {
			data.load(dataFile);
		} catch (IOException | InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
}
