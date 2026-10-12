package cl.biblioteca.dao;

import cl.biblioteca.modelo.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {
    private final UsuarioDAO usuarios = new UsuarioDAO();

    public int crear(Connection connection, Estudiante estudiante) throws SQLException {
        int usuarioId = usuarios.crear(connection, estudiante);
        String sql = "INSERT INTO estudiantes(nombre,rut,curso,correo) VALUES(?,?,?,?)";
        try {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, estudiante.getNombre());
                statement.setString(2, estudiante.getRut());
                statement.setString(3, estudiante.getCurso());
                statement.setString(4, estudiante.getEmail());
                statement.executeUpdate();
            }
            try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM estudiantes WHERE rut=?")) {
                statement.setString(1, estudiante.getRut());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) return result.getInt("id");
                    throw new SQLException("No se encontró el perfil creado");
                }
            }
        } catch (SQLException e) {
            usuarios.eliminar(connection, usuarioId);
            throw e;
        }
    }

    public List<Estudiante> listar(Connection connection) throws SQLException {
        List<Estudiante> lista = new ArrayList<>();
        for (Usuario usuario : usuarios.listar(connection)) {
            if (usuario instanceof Estudiante && ((Estudiante) usuario).getEstudianteId() > 0) {
                lista.add((Estudiante) usuario);
            }
        }
        return lista;
    }

    public void actualizar(Connection connection, Estudiante estudiante) throws SQLException {
        Estudiante anterior = usuarios.porEstudianteId(connection, estudiante.getEstudianteId());
        usuarios.actualizar(connection, estudiante);
        String sql = "UPDATE estudiantes SET nombre=?,rut=?,curso=?,correo=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, estudiante.getNombre());
            statement.setString(2, estudiante.getRut());
            statement.setString(3, estudiante.getCurso());
            statement.setString(4, estudiante.getEmail());
            statement.setInt(5, estudiante.getEstudianteId());
            if (statement.executeUpdate() != 1) {
                throw new SQLException("El estudiante no existe");
            }
        } catch (SQLException e) {
            usuarios.actualizar(connection, anterior);
            throw e;
        }
    }

    public void eliminar(Connection connection, int perfilId) throws SQLException {
        Estudiante estudiante = usuarios.porEstudianteId(connection, perfilId);
        if (estudiante == null) {
            throw new SQLException("El estudiante no existe");
        }
        // Primero se comprueba la relación con los préstamos al eliminar el perfil.
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM estudiantes WHERE id=?")) {
            statement.setInt(1, perfilId);
            statement.executeUpdate();
        }
        usuarios.eliminar(connection, estudiante.getId());
    }
}
