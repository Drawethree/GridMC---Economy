package io.gridmc.economysystem.database.implementations;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.gridmc.economysystem.EconomySystem;
import io.gridmc.economysystem.database.DatabaseCredentials;
import io.gridmc.economysystem.database.SQLDatabase;
import org.bukkit.OfflinePlayer;

public class MySQLDatabase extends SQLDatabase {

	private final DatabaseCredentials credentials;

	public MySQLDatabase(EconomySystem parent, DatabaseCredentials credentials) {
		super(parent);

		this.plugin.getLogger().info("Using MySQL (remote) database.");

		this.credentials = credentials;
		this.connect();
	}


	@Override
	public void connect() {
		final HikariConfig hikari = new HikariConfig();

		hikari.setPoolName("gridmc-economy-" + POOL_COUNTER.getAndIncrement());
		hikari.setJdbcUrl("jdbc:mysql://" + credentials.getHost() + ":" + credentials.getPort() + "/" + credentials.getDatabaseName());
		hikari.setConnectionTestQuery("SELECT 1");

		hikari.setUsername(credentials.getUserName());
		hikari.setPassword(credentials.getPassword());

		hikari.setMinimumIdle(MINIMUM_IDLE);
		hikari.setMaxLifetime(MAX_LIFETIME);
		hikari.setConnectionTimeout(CONNECTION_TIMEOUT);
		hikari.setMaximumPoolSize(MAXIMUM_POOL_SIZE);
		hikari.setLeakDetectionThreshold(LEAK_DETECTION_THRESHOLD);

		this.hikari = new HikariDataSource(hikari);

		this.createTables();
	}
}