package io.gridmc.economysystem.manager;

import io.gridmc.economysystem.EconomySystem;
import me.lucko.helper.Commands;
import me.lucko.helper.Events;
import me.lucko.helper.Schedulers;
import me.lucko.helper.utils.Players;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EconomyManager {


	private EconomySystem plugin;

	private Map<UUID, Double> balances;

	public EconomyManager(EconomySystem plugin) {
		this.plugin = plugin;
		this.balances = new HashMap<>();

		this.loadOnlinePlayersBalances();

		this.registerEvents();

		this.registerCommands();
	}

	private void registerCommands() {
		Commands.create()
				.assertPlayer()
				.handler(c -> {
					if (c.args().size() == 0) {
						c.reply(String.format("Your balance is %,.2f", this.getBalance(c.sender())));
					} else if (c.args().size() == 1) {
						Player target = c.arg(0).parseOrFail(Player.class);
						c.reply(String.format("%s's balance is %,.2f", target.getName(), this.getBalance(target)));
					} else {
						c.reply("Usage: /bal [player]");
					}

				}).registerAndBind(this.plugin, "bal", "balance");

		Commands.create()
				.assertOp()
				.handler(c -> {
					if (c.args().size() != 2) {
						c.reply("Usage: /updatebal <player> <new_balance>");
						return;
					}

					OfflinePlayer target = c.arg(0).parseOrFail(OfflinePlayer.class);
					double newBalance = c.arg(1).parseOrFail(Double.class);

					this.setBalance(c.sender(), target, newBalance);

				}).registerAndBind(this.plugin, "updatebal", "updatebalance");
	}

	private void registerEvents() {
		Events.subscribe(PlayerJoinEvent.class, EventPriority.LOW)
				.handler(e -> this.loadPlayerBalance(e.getPlayer())).bindWith(this.plugin);

		Events.subscribe(PlayerQuitEvent.class)
				.handler(e -> this.savePlayerBalance(e.getPlayer())).bindWith(this.plugin);
	}

	private void savePlayerBalance(OfflinePlayer player) {
		Schedulers.async().run(() -> {
			double balance = this.getBalance(player);
			this.plugin.getPluginDatabase().updateBalance(player, balance);
			this.balances.remove(player.getUniqueId());
		});
	}

	public void onDisable() {
		Schedulers.sync().run(() -> {
			for (Map.Entry<UUID, Double> balance : balances.entrySet()) {
				this.plugin.getPluginDatabase().updateBalance(Players.getOfflineNullable(balance.getKey()), this.balances.get(balance.getValue()));
			}
			balances = new HashMap<>();
		});
	}

	private void loadOnlinePlayersBalances() {
		Players.all().forEach(p -> loadPlayerBalance(p));
	}

	private void loadPlayerBalance(OfflinePlayer player) {
		Schedulers.async().run(() -> {
			this.plugin.getPluginDatabase().registerBalance(player);
			double playerBalance = this.plugin.getPluginDatabase().getBalance(player);
			this.balances.put(player.getUniqueId(), playerBalance);
		});
	}


	public double getBalance(OfflinePlayer player) {
		return this.balances.getOrDefault(player.getUniqueId(), 0.0);
	}

	public boolean setBalance(CommandSender sender, OfflinePlayer player, double newAmount) {

		if (!player.hasPlayedBefore()) {
			return false;
		}

		if (newAmount < 0.0) {
			return false;
		}

		this.balances.put(player.getUniqueId(), newAmount);

		if (sender != null) {
			sender.sendMessage(String.format("Successfully set %s's balance to %,.2f", player.getName(), newAmount));
		}

		return true;
	}

}
