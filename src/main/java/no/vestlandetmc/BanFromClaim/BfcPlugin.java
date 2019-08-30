package no.vestlandetmc.BanFromClaim;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import no.vestlandetmc.BanFromClaim.commands.BfcCommand;
import no.vestlandetmc.BanFromClaim.commands.BfclistCommand;
import no.vestlandetmc.BanFromClaim.commands.UnbfcCommand;
import no.vestlandetmc.BanFromClaim.config.ClaimData;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.UpdateNotification;
import no.vestlandetmc.BanFromClaim.listener.BfcListener;

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
		} else {
			MessageHandler.sendConsole("&2[" + getDescription().getPrefix() + "] &cGriefPrevention was not found! Please add GriefPrevention.");
			MessageHandler.sendConsole("");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}

		this.getCommand("banfromclaim").setExecutor(new BfcCommand());
		this.getCommand("unbanfromclaim").setExecutor(new UnbfcCommand());
		this.getCommand("banfromclaimlist").setExecutor(new BfclistCommand());
		this.getServer().getPluginManager().registerEvents(new BfcListener(), this);

		createDatafile();
		ClaimData.createSection();

		new UpdateNotification(67723) {

			@Override
			public void onUpdateAvailable() {
				MessageHandler.sendConsole("&c-----------------------");
				MessageHandler.sendConsole("&6[" + getDescription().getPrefix() + "] &7Version " + getLatestVersion() + " is now available!");
				MessageHandler.sendConsole("&6[" + getDescription().getPrefix() + "] &7Download the update at https://www.spigotmc.org/resources/" + getProjectId());
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
