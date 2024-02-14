package no.vestlandetmc.BanFromClaim.handler;

public class Hooks {

	private static boolean griefPrevention = false;
	private static boolean griefDefender = false;

	public static boolean gpEnabled() {
		return griefPrevention;
	}

	public static boolean gdEnabled() {
		return griefDefender;
	}

	public static void setGP() {
		griefPrevention = true;
	}

	public static void setGD() {
		griefDefender = true;
	}

}
