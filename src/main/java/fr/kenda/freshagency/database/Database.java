package fr.kenda.freshagency.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.kenda.freshagency.FreshAgencyRunner;
import fr.kenda.freshagency.data.PlayerData;
import fr.kenda.freshagency.utils.Config;
import fr.kenda.freshagency.utils.Messages;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Database {

    private final String host;
    private final String user;
    private final String password;
    private final int port;
    private final String tableToken = "player";
    private final String tableGames = "server";
    private HikariDataSource hikariDataSource;

    public Database(String host, int port, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
        this.port = port;
        tryToConnect();
    }

    private void tryToConnect() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        final HikariConfig config = getHikariConfig();
        this.hikariDataSource = new HikariDataSource(config);

        try {
            final Connection connection = getConnection();
            if (connection != null) {
                FreshAgencyRunner.getInstance().getServer().getConsoleSender().sendMessage(Messages.transformColor("&aDatabase connection successful"));
                createTables();
                connection.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error during get connection", e);
        }
    }

    private HikariConfig getHikariConfig() {
        final HikariConfig config = new HikariConfig();
        config.setMaximumPoolSize(20); // Augmenter le pool de connexions max
        config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/freshagency?autoReconnect=true&serverTimezone=UTC"); // Ajout de serverTimezone pour éviter les problèmes de fuseau horaire
        config.setUsername(user);
        config.setPassword(password);
        config.setMaxLifetime(10 * 60 * 1000); // Temps max que la connexion peut rester dans le pool
        config.setIdleTimeout(5 * 60 * 1000); // Temps max que la connexion va rester inactive avant reset
        config.setLeakDetectionThreshold(60 * 1000); // Détection des fuites de connexion
        config.setConnectionTimeout(30 * 1000); // Timeout de connexion
        config.setValidationTimeout(3000); // Validation de connexion
        return config;
    }

    public Connection getConnection() throws SQLException {
        if (this.hikariDataSource == null) {
            tryToConnect();
        }
        return this.hikariDataSource.getConnection();
    }

    private void executeQuery(String query) {
        try {
            final Connection connection = getConnection();

            PreparedStatement statement = connection.prepareStatement(query);
            statement.execute();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error execute query", e);
        }
    }

    public void createTables() {
        executeQuery(
                "CREATE TABLE IF NOT EXISTS player_data  (" +
                        "    id_player_data INT AUTO_INCREMENT NOT NULL, " +
                        "    block_walk BIGINT NOT NULL, " +
                        "    block_back BIGINT NOT NULL, " +
                        "   time_live BIGINT NOT NULL, " +
                        "   top_donator Varchar (200) NULL, " +
                        "   amount_top_donator Int NULL, " +
                        "    CONSTRAINT player_data_PK PRIMARY KEY (id_player_data)" +
                        ") ENGINE=InnoDB;"
        );

        executeQuery(
                "CREATE TABLE IF NOT EXISTS player (" +
                        "    id_player INT AUTO_INCREMENT NOT NULL, " +
                        "    player_name VARCHAR(36) NOT NULL, " +
                        "    token VARCHAR(36) NOT NULL, " +
                        "    id_player_data INT NOT NULL, " +
                        "    CONSTRAINT player_AK UNIQUE (token), " +
                        "    CONSTRAINT player_PK PRIMARY KEY (id_player), " +
                        "    CONSTRAINT player_player_data_FK FOREIGN KEY (id_player_data) REFERENCES player_data(id_player_data) ON DELETE CASCADE" +
                        ") ENGINE=InnoDB;"
        );

        executeQuery(
                "CREATE TABLE IF NOT EXISTS server (" +
                        "    id_server INT AUTO_INCREMENT NOT NULL, " +
                        "       type_server VARCHAR(50) NOT NULL," +
                        "    server_owner VARCHAR(50) NOT NULL, " +
                        "    map_name VARCHAR(50) NOT NULL, " +
                        " permission VARCHAR(50) NULL, " +
                        "    CONSTRAINT server_PK PRIMARY KEY (id_server)" +
                        ") ENGINE=InnoDB;"
        );

        executeQuery(
                "CREATE TABLE IF NOT EXISTS Play (" +
                        "    id_server INT NOT NULL, " +
                        "    id_player INT NOT NULL, " +
                        "status_game Int NOT NULL, " +
                        "    CONSTRAINT Play_PK PRIMARY KEY (id_server, id_player), " +
                        "    CONSTRAINT Play_server_FK FOREIGN KEY (id_server) REFERENCES server(id_server) ON DELETE CASCADE, " +
                        "    CONSTRAINT Play_player0_FK FOREIGN KEY (id_player) REFERENCES player(id_player) ON DELETE CASCADE" +
                        ") ENGINE=InnoDB;"
        );
    }

    public boolean isGameExist(String gameWorld) {
        try {
            final Connection connection = getConnection();

            String sql = "SELECT COUNT(*) FROM " + tableGames + " WHERE map_name = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, gameWorld);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                connection.close();
                return rs.getInt(1) > 0;
            } else {
                connection.close();
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error during check if game exist", e);
        }
    }

    public void createGame(String gameWorld) {
        try {
            final Connection connection = getConnection();

            String sql = "INSERT INTO " + tableGames + " (type_server, server_owner, map_name, permission) VALUES (\"runner\", ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, Config.getString("server_name"));
            statement.setString(2, gameWorld);
            statement.setString(3, Config.getString("server_permission"));
            statement.execute();

            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error during create game in db", e);
        }
    }

    public void disconnect() {
        if (this.hikariDataSource != null) {
            this.hikariDataSource.close();
        }
    }

    public void clearAllGames() {
        try {
            final Connection connection = getConnection();
            String sql = "DELETE FROM " + tableGames + " WHERE server_owner = ?";
            PreparedStatement state = connection.prepareStatement(sql);
            state.setString(1, Config.getString("server_name"));
            state.execute();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error during delete games", e);
        }
    }

    public String getTokenFromPlayer(String player) {
        try {
            final Connection connection = getConnection();

            String sql = "SELECT token FROM " + tableToken + " WHERE player_name = ?";
            PreparedStatement state = connection.prepareStatement(sql);
            state.setString(1, player);
            ResultSet resultSet = state.executeQuery();
            if (resultSet.next()) {
                connection.close();
                return resultSet.getString("token");
            } else {
                connection.close();
                return "";
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error fetching token for player: " + player, e);
        }
    }

    public void addGame(World world, String playerName, int status) {
        try {
            final Connection connection = getConnection();

            String sql = "INSERT INTO play (id_server, id_player, status_game) VALUES " +
                    "(" +
                    "(SELECT id_server FROM server WHERE map_name = ?)," +
                    "(SELECT id_player FROM player WHERE player_name = ?)," +
                    "?)";
            PreparedStatement state = connection.prepareStatement(sql);
            state.setString(1, world.getName());
            state.setString(2, playerName);
            state.setInt(3, status);
            state.execute();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error during add game", e);
        }
    }

    public void updateGame(String playerName, int status) {
        try {
            final Connection connection = getConnection();
            String sql = "UPDATE PLAY INNER JOIN player ON play.id_player = player.id_player SET status_game = ? WHERE player_name = ?";
            PreparedStatement state = connection.prepareStatement(sql);
            state.setInt(1, status);
            state.setString(2, playerName);
            state.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error during update game", e);
        }
    }

    public int getTotalMap() {
        try {
            final Connection connection = getConnection();
            String sql = "SELECT COUNT(*) AS 'total' FROM " + tableGames + " WHERE server_owner LIKE \"" + Config.getString("server_name") + "\"";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                connection.close();
                return rs.getInt("total");
            } else {
                connection.close();
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error during fetching number of games", e);
        }
    }

    public String getHost() {
        return this.host;
    }

    public void deleteGame(Player player) {
        try {
            final Connection connection = getConnection();
            String sql = "DELETE Play FROM Play INNER JOIN player ON Play.id_player = player.id_player WHERE player.player_name = ?";
            PreparedStatement state = connection.prepareStatement(sql);
            state.setString(1, player.getName());
            state.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error during delete game", e);
        }
    }

    public void updateStats(PlayerData player) {
        Connection connection = null;
        PreparedStatement state = null;
        try {
            connection = getConnection();

            String sql = "UPDATE player_data " +
                    "INNER JOIN player ON player.id_player = player_data.id_player_data " +
                    "SET block_walk = block_walk + ?, " +
                    "    block_back = block_back + ?, " +
                    "    time_live = time_live + ?";

            boolean hasTopDonator = player.getTopDonator() != null;

            // Si topDonator existe, ajouter les champs top_donator et amount_top_donator
            if (hasTopDonator) {
                sql += ", top_donator = CASE WHEN ? > amount_top_donator THEN ? ELSE top_donator END, " +
                        "amount_top_donator = CASE WHEN ? > amount_top_donator THEN ? ELSE amount_top_donator END";
            }

            sql += " WHERE player.player_name = ?";

            state = connection.prepareStatement(sql);
            state.setInt(1, player.getBlockWalk());  // block_walk
            state.setInt(2, player.getBlockBack());  // block_back
            state.setLong(3, player.getTimeLive());  // time_live

            if (hasTopDonator) {
                int newTopDonatorAmount = player.getTopDonator().getValue();
                state.setInt(4, newTopDonatorAmount);  // condition for top_donator
                state.setString(5, player.getTopDonator().getKey());  // new top_donator
                state.setInt(6, newTopDonatorAmount);  // condition for amount_top_donator
                state.setInt(7, newTopDonatorAmount);  // new amount_top_donator
                state.setString(8, player.getPlayer().getName());  // player_name
            } else {
                state.setString(4, player.getPlayer().getName());  // player_name
            }

            state.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error during update of " + player.getPlayer().getName(), e);
        } finally {
            try {
                if (state != null) state.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();  // Optionally log or handle closing errors
            }
        }
    }
}
