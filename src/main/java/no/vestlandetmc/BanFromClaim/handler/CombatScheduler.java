package no.vestlandetmc.BanFromClaim.handler;

import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.listener.CombatMode;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map.Entry;
import java.util.UUID;

public class CombatScheduler extends BukkitRunnable {

	@Override
	public void run() {
		if (CombatMode.isEmpty()) {
			return;
		}
		for (final Entry<UUID, Long> combat : CombatMode.getAllTime().entrySet()) {
			final UUID uuid = combat.getKey();
			final UUID attackerUUID = CombatMode.getAttacker(uuid);
			if (attackerUUID == null) {
				continue;
			}

			final OfflinePlayer victim = Bukkit.getOfflinePlayer(uuid);
			final OfflinePlayer attacker = Bukkit.getOfflinePlayer(attackerUUID);
			final long time = combat.getValue();
			final long newTime = System.currentTimeMillis() / 1000;
			final long combatTime = newTime - time;
			final long combatLeft = Config.COMBAT_TIME - combatTime;

			if (!victim.isOnline() || !attacker.isOnline()) {
				CombatMode.removeTime(combat.getKey());
				CombatMode.removeAttacker(combat.getKey());
			} else if (combatTime >= Config.COMBAT_TIME) {
				CombatMode.removeTime(combat.getKey());
				CombatMode.removeAttacker(combat.getKey());
			} else {
				if (Config.TIMER_ENABLED) {
					MessageHandler.sendAction(victim.getPlayer(), "&4&lVICTIM " + "== " + combatLeft + " SEC ==");
					MessageHandler.sendAction(attacker.getPlayer(), "&4&lATTACKER " + "== " + combatLeft + " SEC ==");
				}
			}
		}
	}
}
