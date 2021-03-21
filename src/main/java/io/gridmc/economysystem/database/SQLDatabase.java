package io.gridmc.economysystem.database;

import com.zaxxer.hikari.HikariDataSource;
import io.gridmc.economysystem.EconomySystem;
import io.gridmc.economysystem.database.implementations.MySQLDatabase;
import me.lucko.helper.Schedulers;
import me.lucko.helper.time.Time;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SQLDatabase extends Database {

    protected static final AtomicInteger POOL_COUNTER = new AtomicInteger(0);
    protected static final int MAXIMUM_POOL_SIZE = (Runtime.getRuntime().availableProcessors() * 2) + 1;
    protected static final int MINIMUM_IDLE = Math.min(MAXIMUM_POOL_SIZE, 10);

    protected static final long MAX_LIFETIME = TimeUnit.MINUTES.toMillis(30); // 30 Minutes
    protected static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toMillis(10); // 10 seconds
    protected static final long LEAK_DETECTION_THRESHOLD = TimeUnit.SECONDS.toMillis(10); // 10 seconds

    protected static final String ECONOMY_TABLE_NAME = "GridMC_Economy";

    protected static final String ECONOMY_UUID_COLNAME = "uuid";
    protected static final String ECONOMY_BALANCE_COLNAME = "balance";

    protected EconomySystem plugin;
    protected HikariDataSource hikari;

    public SQLDatabase(EconomySystem plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    public abstract void connect();

    public void close() {
        if (this.hikari != null) {
            this.hikari.close();
        }
    }

    public void execute(String sql, Object... replacements) {
        try (Connection c = this.hikari.getConnection(); PreparedStatement statement = c.prepareStatement(sql)) {
            if (replacements != null) {
                for (int i = 0; i < replacements.length; i++) {
                    statement.setObject(i + 1, replacements[i]);
                }
            }
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


	public void executeAsync(String sql, Object... replacements) {
		Schedulers.async().run(() -> {
			this.execute(sql, replacements);
		});
	}

    public void createTables() {
        Schedulers.async().run(() -> execute("CREATE TABLE IF NOT EXISTS " + ECONOMY_TABLE_NAME + "(uuid varchar(36) NOT NULL UNIQUE, balance double default 0, primary key (UUID))"));
    }

    @Override
    public void registerBalance(OfflinePlayer player) {
        this.executeAsync("INSERT IGNORE INTO " + MySQLDatabase.ECONOMY_TABLE_NAME + " VALUES(?,?)", player.getUniqueId().toString(), 0.0);
    }

    @Override
    public void updateBalance(OfflinePlayer p, double newAmount) {
		this.executeAsync("UPDATE " + ECONOMY_TABLE_NAME + " SET " + ECONOMY_BALANCE_COLNAME + "=? WHERE " + ECONOMY_UUID_COLNAME + "=?", newAmount, p.getUniqueId().toString());
    }


    @Override
    public double getBalance(OfflinePlayer player) {
        try (Connection con = this.hikari.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM " + MySQLDatabase.ECONOMY_TABLE_NAME + " WHERE " + ECONOMY_UUID_COLNAME + "=?")) {
            statement.setString(1, player.getUniqueId().toString());
            try (ResultSet set = statement.executeQuery()) {
                if (set.next()) {
                    return set.getDouble(MySQLDatabase.ECONOMY_BALANCE_COLNAME);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
