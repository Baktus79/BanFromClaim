package no.vestlandetmc.BanFromClaim.commands;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import no.vestlandetmc.BanFromClaim.config.Config;
import no.vestlandetmc.BanFromClaim.handler.MessageHandler;
import no.vestlandetmc.BanFromClaim.handler.Permissions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@NullMarked
public class SafeSpot implements BasicCommand {

	@Override
	public void execute(CommandSourceStack commandSourceStack, String[] args) {
		if (!(commandSourceStack.getSender() instanceof Player player)) {
			MessageHandler.sendConsole("&cYou cannot use this command from the console.");
			return;
		}

		Config.setSafespot(player.getLocation());
		MessageHandler.sendMessage(player, "&eCurrent location has been stored as a safespot.");
	}

	@Override
	public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
		return BasicCommand.super.suggest(commandSourceStack, args);
	}

	@Override
	public boolean canUse(CommandSender sender) {
		return BasicCommand.super.canUse(sender);
	}

	@Override
	public @Nullable String permission() {
		return Permissions.SAFESPOT.getName();
	}
}
