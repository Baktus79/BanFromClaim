package no.vestlandetmc.BanFromClaim.utils;

import lombok.Getter;
import no.vestlandetmc.BanFromClaim.BfcPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public abstract class UpdateNotification extends BukkitRunnable {

	@Getter
	private static int projectId;
	@Getter
	private static String latestVersion = "";

	public UpdateNotification(int projectId) {
		UpdateNotification.projectId = projectId;
	}

	@Override
	public void run() {

		try {
			final URI uri = new URI("https://api.spigotmc.org/legacy/update.php?resource=" + projectId);
			final URL url = uri.toURL();
			final URLConnection con = url.openConnection();

			try (BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
				latestVersion = r.readLine();
			}

			if (isUpdateAvailable()) {
				onUpdateAvailable();
			}

		} catch (final IOException | URISyntaxException e) {
			e.getStackTrace();
		}
	}

	public abstract void onUpdateAvailable();

	public static boolean isUpdateAvailable() {
		return !latestVersion.equals(BfcPlugin.getPlugin().getDescription().getVersion());
	}

	public static String getCurrentVersion() {
		return BfcPlugin.getPlugin().getDescription().getVersion();
	}
}