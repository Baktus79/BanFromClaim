package no.vestlandetmc.BanFromClaim.handler;

import no.vestlandetmc.BanFromClaim.BfcPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public abstract class UpdateNotification extends BukkitRunnable {

	private static int projectId;
	private static String latestVersion = "";

	public UpdateNotification(int projectId) {
		UpdateNotification.projectId = projectId;
	}

	@Override
	public void run() {

		try {
			final URL url = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + projectId);
			final URLConnection con = url.openConnection();

			try (BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				latestVersion = r.readLine();
			}

			if (isUpdateAvailable()) {
				onUpdateAvailable();
			}

		} catch (final IOException ex) {
			ex.getStackTrace();
		}
	}

	public abstract void onUpdateAvailable();

	public static boolean isUpdateAvailable() {
		return !latestVersion.equals(BfcPlugin.getInstance().getDescription().getVersion());
	}

	public static int getProjectId() {
		return projectId;
	}

	public static String getLatestVersion() {
		return latestVersion;
	}

	public static String getCurrentVersion() {
		return BfcPlugin.getInstance().getDescription().getVersion();
	}
}