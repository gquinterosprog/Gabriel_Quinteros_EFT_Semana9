package cl.biblioteca.dao;

import cl.biblioteca.modelo.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrestamoDAO {
    private static final String SELECT = "SELECT p.*,d.fecha_vencimiento,d.atraso,e.nombre estudiante,l.titulo libro "
            + "FROM prestamos p JOIN detalle_prestamos d ON d.id_prestamo=p.id "
            + "JOIN estudiantes e ON e.id=p.id_estudiante JOIN libros l ON l.id=p.id_libro";

    private Prestamo convertir(ResultSet result) throws SQLException {
        String devolucion = result.getBoolean("devuelto") ? result.getString("fecha_devolucion") : null;
        return new Prestamo(result.getInt("id"), result.getInt("id_estudiante"), result.getString("estudiante"),
                result.getInt("id_libro"), result.getString("libro"), result.getString("fecha_prestamo"),
                result.getString("fecha_vencimiento"), devolucion, result.getBoolean("atraso"));
    }

    public List<Prestamo> listar(Connection connection) throws SQLException {
        return listar(connection, 0);
    }

    public List<Prestamo> listar(Connection connection, int estudianteId) throws SQLException {
        List<Prestamo> lista = new ArrayList<>();
        String sql = SELECT;
        if (estudianteId > 0) sql += " WHERE p.id_estudiante=?";
        sql += " ORDER BY p.id DESC";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (estudianteId > 0) statement.setInt(1, estudianteId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) lista.add(convertir(result));
            }
        }
        return lista;
    }

    public Prestamo porId(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT + " WHERE p.id=?")) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return convertir(result);
                throw new SQLException("El préstamo no existe");
            }
        }
    }

    public boolean tieneActivo(Connection connection, int estudianteId, int libroId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM prestamos WHERE id_estudiante=? AND id_libro=? AND devuelto=FALSE")) {
            statement.setInt(1, estudianteId);
            statement.setInt(2, libroId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public int crear(Connection connection, Prestamo prestamo) throws SQLException {
        String sql = "INSERT INTO prestamos(id_estudiante,id_libro,fecha_prestamo,fecha_devolucion,devuelto) VALUES(?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, prestamo.getEstudianteId());
            statement.setInt(2, prestamo.getLibroId());
            statement.setString(3, prestamo.getFechaPrestamo());
            statement.setString(4, prestamo.activo() ? prestamo.getVencimiento() : prestamo.getDevolucion());
            statement.setBoolean(5, !prestamo.activo());
            statement.executeUpdate();
        }
        int id;
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM prestamos WHERE id_estudiante=? AND id_libro=? ORDER BY id DESC")) {
            statement.setInt(1, prestamo.getEstudianteId());
            statement.setInt(2, prestamo.getLibroId());
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) throw new SQLException("No se encontró el préstamo creado");
                id = result.getInt("id");
            }
        }
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO detalle_prestamos(id_prestamo,fecha_vencimiento,atraso) VALUES(?,?,?)")) {
            statement.setInt(1, id);
            statement.setString(2, prestamo.getVencimiento());
            statement.setBoolean(3, prestamo.isAtraso());
            statement.executeUpdate();
        } catch (SQLException e) {
            eliminar(connection, id);
            throw e;
        }
        return id;
    }

    public void actualizar(Connection connection, Prestamo prestamo) throws SQLException {
        Prestamo anterior = porId(connection, prestamo.getId());
        actualizarDetalle(connection, prestamo);
        try (PreparedStatement statement = connection.prepareStatement("UPDATE prestamos SET fecha_devolucion=?,devuelto=? WHERE id=?")) {
            statement.setString(1, prestamo.activo() ? prestamo.getVencimiento() : prestamo.getDevolucion());
            statement.setBoolean(2, !prestamo.activo());
            statement.setInt(3, prestamo.getId());
            if (statement.executeUpdate() != 1) throw new SQLException("No se pudo actualizar el préstamo");
        } catch (SQLException e) {
            actualizarDetalle(connection, anterior);
            throw e;
        }
    }

    private void actualizarDetalle(Connection connection, Prestamo prestamo) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE detalle_prestamos SET fecha_vencimiento=?,atraso=? WHERE id_prestamo=?")) {
            statement.setString(1, prestamo.getVencimiento());
            statement.setBoolean(2, prestamo.isAtraso());
            statement.setInt(3, prestamo.getId());
            if (statement.executeUpdate() != 1) throw new SQLException("No se pudo actualizar el detalle");
        }
    }

    public void eliminar(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM detalle_prestamos WHERE id_prestamo=?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM prestamos WHERE id=?")) {
            statement.setInt(1, id);
            if (statement.executeUpdate() != 1) throw new SQLException("El préstamo no existe");
        }
    }
}
