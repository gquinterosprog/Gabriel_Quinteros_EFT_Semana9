package cl.biblioteca.infraestructura;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final DatabaseConnection INSTANCE = new DatabaseConnection();
    private String servidor = "127.0.0.1";
    private int puerto = 3319;
    private String base = "biblioteca";
    private String usuario = "biblioteca_app";
    private String clave = "BibliotecaDemo2026!";
    private Connection connection;

    private DatabaseConnection() {
    }

    public static DatabaseConnection getInstance() {
        return INSTANCE;
    }

    public synchronized String[] getDatos() {
        return new String[]{servidor, "" + puerto, base, usuario};
    }

    public synchronized void configurar(String servidor, int puerto, String base, String usuario, String clave)
            throws SQLException {
        if (servidor.trim().isEmpty() || base.trim().isEmpty() || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException("Completa el servidor, la base y el usuario de MySQL");
        }
        if (puerto < 1 || puerto > 65535) {
            throw new IllegalArgumentException("El puerto debe estar entre 1 y 65535");
        }
        close();
        this.servidor = servidor.trim();
        this.puerto = puerto;
        this.base = base.trim();
        this.usuario = usuario.trim();
        this.clave = clave;
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection != null) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT 1");
                 ResultSet result = statement.executeQuery()) {
                result.next();
            } catch (SQLException e) {
                try {
                    connection.close();
                } finally {
                    connection = null;
                }
            }
        }
        if (connection == null) {
            String url = "jdbc:mysql://" + servidor + ":" + puerto + "/" + base;
            connection = DriverManager.getConnection(url, usuario, clave);
        }
        return connection;
    }

    public synchronized void close() throws SQLException {
        if (connection != null) {
            try {
                connection.close();
            } finally {
                connection = null;
            }
        }
    }
}
