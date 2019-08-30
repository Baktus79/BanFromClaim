package no.vestlandetmc.BanFromClaim.config;

import org.bukkit.Material;
import org.bukkit.Particle;

import no.vestlandetmc.BanFromClaim.handler.MessageHandler;

public class Config extends ConfigHandler {

	private Config(String fileName) {
		super(fileName);
	}

	public static Material
	BLOCK_TYPE,
	TP_BLOCK_TYPE;

	public static Particle
	PARTICLE_TYPE;

	public static String
	ELEVATOR_LOCALE_UP,
	ELEVATOR_LOCALE_DOWN,
	ELEVATOR_LOCALE_DANGER,
	ELEVATOR_LOCALE_ACTIVATED,
	COOLDOWN_LOCALE,
	SOUND_ACTIVATED,
	ELEVATOR_SOUND,
	TP_SOUND,
	TP_LOCALE_DANGER,
	TP_LOCALE_LINKSELF,
	TP_LOCALE_CANCELLED,
	TP_LOCALE_INIT,
	TP_LOCALE_WARMUP,
	TP_LOCALE_UNVALID,
	TP_LOCALE_EXIST,
	TP_LOCALE_UNEXIST,
	TP_LOCALE_ADDED,
	TP_LOCALE_LINKEXIST,
	TP_LOCALE_NOOWNER,
	TP_LOCALE_LINKED,
	TP_LOCALE_REMOVED,
	TP_LOCALE_UNLINKED,
	TP_LOCALE_NODEST,
	TP_LOCALE_LISTHEADER,
	TP_LOCALE_LISTNOTP,
	TP_LOCALE_SPECIFYTP,
	TP_LOCALE_SPECIFYMORETP,
	TP_LOCALE_PERMBLOCK,
	TP_LOCALE_HELPHEADER,
	TP_LOCALE_HELPADD,
	TP_LOCALE_HELPHELP,
	TP_LOCALE_HELPLINK,
	TP_LOCALE_HELPLIST,
	TP_LOCALE_HELPREMOVE,
	TP_LOCALE_HELPUNLINK,
	ML_LOCALE_PERMISSION;

	public static int
	PARTICLE_COUNT,
	COOLDOWN_TIME,
	TP_WARMUP_TIME;

	public static boolean
	PARTICLE_ENABLED,
	GRIEFPREVENTION_HOOK,
	WORLDGUARD_HOOK,
	COOLDOWN_ENABLED,
	TP_PARTICLE_ENABLE,
	TP_WARMUP_ENABLE;

	private void onLoad() {

		BLOCK_TYPE = Material.matchMaterial(getString("Elevator.BlockType").toUpperCase());
		PARTICLE_TYPE = Particle.valueOf(getString("Elevator.ParticleType").toUpperCase());
		PARTICLE_ENABLED = getBoolean("Elevator.EnableParticle");
		ELEVATOR_LOCALE_UP = getString("ElevatorLocale.ElevatorUp");
		ELEVATOR_LOCALE_DOWN = getString("ElevatorLocale.ElevatorDown");
		ELEVATOR_LOCALE_DANGER = getString("ElevatorLocale.ElevatorDanger");
		ELEVATOR_LOCALE_ACTIVATED = getString("ElevatorLocale.ElevatorActivated");
		ELEVATOR_SOUND = getString("Elevator.UsageSound");
		SOUND_ACTIVATED = getString("Elevator.ActivateSound");
		PARTICLE_COUNT = getInt("Elevator.ParticleCount");
		GRIEFPREVENTION_HOOK = getBoolean("Hooks.GriefPrevention");
		WORLDGUARD_HOOK = getBoolean("Hooks.WorldGuard");
		COOLDOWN_ENABLED = getBoolean("Cooldown.EnableCooldown");
		COOLDOWN_TIME = getInt("Cooldown.Time");
		COOLDOWN_LOCALE = getString("Cooldown.Locale");
		TP_WARMUP_ENABLE = getBoolean("Teleporter.WarmupEnable");
		TP_WARMUP_TIME = getInt("Teleporter.WarmupTime");
		TP_BLOCK_TYPE = Material.matchMaterial(getString("Teleporter.BlockType").toUpperCase());
		TP_SOUND = getString("Teleporter.UsageSound");
		TP_PARTICLE_ENABLE = getBoolean("Teleporter.EnableParticle");
		TP_LOCALE_DANGER = getString("TeleporterLocale.Danger");
		TP_LOCALE_CANCELLED = getString("TeleporterLocale.Cancelled");
		TP_LOCALE_INIT = getString("TeleporterLocale.Initialized");
		TP_LOCALE_WARMUP = getString("TeleporterLocale.Warmup");
		TP_LOCALE_UNVALID = getString("TeleporterLocale.UnvalidBlock");
		TP_LOCALE_EXIST = getString("TeleporterLocale.Exist");
		TP_LOCALE_UNEXIST = getString("TeleporterLocale.Unexist");
		TP_LOCALE_ADDED = getString("TeleporterLocale.Added");
		TP_LOCALE_LINKEXIST = getString("TeleporterLocale.LinkExist");
		TP_LOCALE_NOOWNER = getString("TeleporterLocale.NoOwner");
		TP_LOCALE_LINKED = getString("TeleporterLocale.Linked");
		TP_LOCALE_REMOVED = getString("TeleporterLocale.Removed");
		TP_LOCALE_UNLINKED = getString("TeleporterLocale.Unlinked");
		TP_LOCALE_NODEST = getString("TeleporterLocale.NoDestination");
		TP_LOCALE_LISTHEADER = getString("TeleporterLocale.ListHeader");
		TP_LOCALE_LISTNOTP = getString("TeleporterLocale.ListNoTeleporters");
		TP_LOCALE_SPECIFYTP = getString("TeleporterLocale.SpecifyTeleporter");
		TP_LOCALE_SPECIFYMORETP = getString("TeleporterLocale.SpecifyMoreTeleporter");
		TP_LOCALE_PERMBLOCK = getString("TeleporterLocale.PermissionBlocks");
		TP_LOCALE_HELPHEADER = getString("TeleporterLocale.HelpHeader");
		TP_LOCALE_HELPADD = getString("TeleporterLocale.HelpAdd");
		TP_LOCALE_HELPHELP = getString("TeleporterLocale.HelpHelp");
		TP_LOCALE_HELPLINK = getString("TeleporterLocale.HelpLink");
		TP_LOCALE_HELPLIST = getString("TeleporterLocale.HelpList");
		TP_LOCALE_HELPREMOVE = getString("TeleporterLocale.HelpRemove");
		TP_LOCALE_HELPUNLINK = getString("TeleporterLocale.HelpUnlink");
		TP_LOCALE_LINKSELF = getString("TeleporterLocale.LinkSelf");
		ML_LOCALE_PERMISSION = getString("MiscellaneousLocale.Permission");

		boolean saveFile = false;

		//Cleanup old path in config file
		if(contains("ElevatorUp")) {
			set("ElevatorUp", null);
			saveFile = true;
		}

		if(contains("ElevatorDown")) {
			set("ElevatorDown", null);
			saveFile = true;
		}

		if(contains("ElevatorDanger")) {
			set("ElevatorDanger", null);
			saveFile = true;
		}

		if(contains("ElevatorActivated")) {
			set("ElevatorActivated", null);
			saveFile = true;
		}

		if(contains("ElevatorCooldown")) {
			set("ElevatorCooldown", null);
			saveFile = true;
		}

		if(contains("BlockType")) {
			set("BlockType", null);
			saveFile = true;
		}

		if(contains("EnableParticle")) {
			set("EnableParticle", null);
			saveFile = true;
		}

		if(contains("ParticleType")) {
			set("ParticleType", null);
			saveFile = true;
		}

		if(contains("ParticleCount")) {
			set("ParticleCount", null);
			saveFile = true;
		}

		if(contains("UsageSound")) {
			set("UsageSound", null);
			saveFile = true;
		}

		if(contains("ActivateSound")) {
			set("ActivateSound", null);
			saveFile = true;
		}

		if(saveFile) {
			saveConfig();
			MessageHandler.sendConsole("&7[Elevator] &6The config file has been cleaned. Please check the config file for changes.");
		}

		sendInfo();

	}

	public static void initialize() {
		new Config("config.yml").onLoad();
	}

	private void sendInfo() {
		if(PARTICLE_ENABLED) {
			MessageHandler.sendConsole("&7[Elevator] &9Enabling &7particles for elevators");
			MessageHandler.sendConsole("&7[Elevator] Using &9" +  PARTICLE_TYPE.toString().replace("_", " ") + " &7as particle for elevators");
		} else {
			MessageHandler.sendConsole("&7[Elevator] &cDisabling &7particles for elevators");
		}

		if(TP_PARTICLE_ENABLE) {
			MessageHandler.sendConsole("&7[Elevator] &9Enabling &7particles for teleporters");
		} else {
			MessageHandler.sendConsole("&7[Elevator] &cDisabling &7particles for teleporters");
		}

		MessageHandler.sendConsole("&7[Elevator] Using &9" + BLOCK_TYPE.name().replace("_", " ") + " &7as elevator block");
		MessageHandler.sendConsole("&7[Elevator] Using &9" + TP_BLOCK_TYPE.name().replace("_", " ") + " &7as teleporter block");
		MessageHandler.sendConsole("&7[Elevator] Using the sound &9" + SOUND_ACTIVATED + " &7on activate elevator");
		MessageHandler.sendConsole("&7[Elevator] Using the sound &9" + ELEVATOR_SOUND + " &7when using elevator");
		MessageHandler.sendConsole("&7[Elevator] Using the sound &9" + TP_SOUND + " &7when using teleporters");
	}
}
