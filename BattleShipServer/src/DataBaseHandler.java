import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DataBaseHandler {

	private java.sql.Connection conn;
	private static DataBaseHandler dataBaseHandler;

	private DataBaseHandler() {
		try {
			conn = DriverManager.getConnection("jdbc:mysql://localhost/battleship?autoReconnect=true&useSSL=false",
					"root", "");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Connection with database failed!");
		} 
	}

	// not safe for multithreading
	public static DataBaseHandler getInstance() {
		if (dataBaseHandler == null)
			dataBaseHandler = new DataBaseHandler();
		return dataBaseHandler;
	}

	public void addWinToAPlayer(String playerName) {
		// PreparedStatement insert = null;
		// PreparedStatement update = null;
		// PreparedStatement check = null;
		ResultSet rs;
		String stringForCheck = "SELECT * FROM users WHERE users.name = ?";
		String stringForUpdate = "UPDATE users SET total = total + 1, won = won + 1 WHERE users.name = ?";
		String stringForInsert = "INSERT INTO users(name, total, won) VALUES(?, 1, 1)";
		try (PreparedStatement check = conn.prepareStatement(stringForCheck);
				PreparedStatement update = conn.prepareStatement(stringForUpdate);
				PreparedStatement insert = conn.prepareStatement(stringForInsert);) {

			check.setString(1, playerName);
			rs = check.executeQuery();
			if (rs.next()) {
				update.setString(1, playerName);
				update.executeUpdate();
			} else {
				insert.setString(1, playerName);
				insert.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Adding a win to a player failed!");
		}

	}

	public void addGameToAPlayer(String playerName) {
		PreparedStatement insert;
		PreparedStatement update;
		PreparedStatement check;
		ResultSet rs;
		String stringForCheck = "SELECT * FROM users WHERE users.name = ?";
		String stringForUpdate = "UPDATE users SET total = total + 1 WHERE users.name = ?";
		String stringForInsert = "INSERT INTO users(name, total) VALUES(?, 1)";
		try {
			check = conn.prepareStatement(stringForCheck);
			check.setString(1, playerName);
			rs = check.executeQuery();
			if (rs.next()) {
				update = conn.prepareStatement(stringForUpdate);
				update.setString(1, playerName);
				update.executeUpdate();
			} else {
				insert = conn.prepareStatement(stringForInsert);
				insert.setString(1, playerName);
				insert.executeUpdate();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Adding a game to a player failed!");
		}

	}
}
