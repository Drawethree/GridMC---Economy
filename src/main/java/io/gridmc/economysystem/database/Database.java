package io.gridmc.economysystem.database;

import io.gridmc.economysystem.EconomySystem;
import org.bukkit.OfflinePlayer;

public abstract class Database {

	protected final EconomySystem plugin;

	public Database(EconomySystem plugin) {
		this.plugin = plugin;
	}

	public abstract double getBalance(OfflinePlayer player);

	public abstract void registerBalance(OfflinePlayer player);

	public abstract void updateBalance(OfflinePlayer player, double newAmount);

}
