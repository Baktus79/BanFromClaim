package no.vestlandetmc.BanFromClaim.listener;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatMode implements Listener {

	public static HashMap<UUID, Long> TIME = new HashMap<>();
	public static HashMap<UUID, UUID> ATTACKER = new HashMap<>();

	@EventHandler
	public void playerHit(EntityDamageByEntityEvent e) {
		if(!(e.getEntity() instanceof Player)) { return; }
		if(!(e.getDamager() instanceof Player)) { return; }

		final Player victim = (Player) e.getEntity();
		final Player attacker = (Player) e.getDamager();
		final long time = System.currentTimeMillis() / 1000;

		if(TIME.containsKey(attacker.getUniqueId())) {
			if(ATTACKER.get(attacker.getUniqueId()).equals(victim.getUniqueId()))
				return;
		}

		TIME.put(victim.getUniqueId(), time);
		ATTACKER.put(victim.getUniqueId(), attacker.getUniqueId());

	}

}
