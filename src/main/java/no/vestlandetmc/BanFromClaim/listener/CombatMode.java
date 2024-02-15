package no.vestlandetmc.BanFromClaim.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.UUID;

public class CombatMode implements Listener {

	private static final HashMap<UUID, Long> TIME = new HashMap<>();
	private static final HashMap<UUID, UUID> ATTACKER = new HashMap<>();

	@EventHandler
	public void playerHit(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player)) {
			return;
		}
		if (!(e.getDamager() instanceof Player)) {
			return;
		}

		final Player victim = (Player) e.getEntity();
		final Player attacker = (Player) e.getDamager();
		final long time = System.currentTimeMillis() / 1000;

		if (TIME.containsKey(attacker.getUniqueId())) {
			if (ATTACKER.get(attacker.getUniqueId()).equals(victim.getUniqueId()))
				return;
		}

		TIME.put(victim.getUniqueId(), time);
		ATTACKER.put(victim.getUniqueId(), attacker.getUniqueId());

	}

	public static UUID getAttacker(UUID uuid) {
		return ATTACKER.getOrDefault(uuid, null);
	}

	public static long getTime(UUID uuid) {
		if (TIME.containsKey(uuid)) {
			return TIME.get(uuid);
		} else {
			return 0;
		}
	}

	public static void registerTime(UUID uuid, long time) {
		TIME.put(uuid, time);
	}

	public static void registerAttacker(UUID victim, UUID attacker) {
		ATTACKER.put(victim, attacker);
	}

	public static void removeTime(UUID uuid) {
		TIME.remove(uuid);
	}

	public static void removeAttacker(UUID victim) {
		ATTACKER.remove(victim);
	}

	public static HashMap<UUID, Long> getAllTime() {
		return TIME;
	}

	public static boolean isEmpty() {
		if (TIME.isEmpty() || ATTACKER.isEmpty()) {
			TIME.clear();
			ATTACKER.clear();

			return true;
		} else {
			return false;
		}

	}

	public static boolean attackerContains(UUID uuid) {
		return ATTACKER.containsKey(uuid);
	}

}
