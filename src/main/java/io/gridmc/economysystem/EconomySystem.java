package io.gridmc.economysystem;


import io.gridmc.economysystem.database.Database;
import io.gridmc.economysystem.database.DatabaseCredentials;
import io.gridmc.economysystem.database.SQLDatabase;
import io.gridmc.economysystem.database.implementations.MySQLDatabase;
import io.gridmc.economysystem.manager.EconomyManager;
import me.lucko.helper.plugin.ExtendedJavaPlugin;

public final class EconomySystem extends ExtendedJavaPlugin {

	private EconomyManager manager;

	private Database pluginDatabase;

	@Override
	public void enable() {

		this.saveDefaultConfig();

		try {
			this.pluginDatabase = new MySQLDatabase(this, DatabaseCredentials.fromConfig(this.getConfig()));
		} catch (Exception e) {
			this.getLogger().warning("Could not maintain Database Connection. Disabling plugin.");
			e.printStackTrace();
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		this.manager = new EconomyManager(this);
	}

	@Override
	public void disable() {
		this.manager.onDisable();

		if (this.pluginDatabase != null) {

			if (this.pluginDatabase instanceof SQLDatabase) {
				((SQLDatabase) this.pluginDatabase).close();
			}

		}
	}

	public Database getPluginDatabase() {
		return this.pluginDatabase;
	}

	public EconomyManager getEconomyManager() {
		return this.manager;
	}
}
