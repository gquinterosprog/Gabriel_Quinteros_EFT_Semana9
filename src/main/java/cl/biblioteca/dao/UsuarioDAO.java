package cl.biblioteca.dao;

import cl.biblioteca.modelo.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {
    private static final String SELECT = "SELECT u.id,u.nombre,u.correo,u.`contraseña`,u.rol,e.id estudiante_id,u.rut,e.curso FROM usuarios u LEFT JOIN estudiantes e ON e.rut=u.rut";

    private Usuario convertir(ResultSet result) throws SQLException {
        if ("bibliotecario".equalsIgnoreCase(result.getString("rol"))) {
            return new Bibliotecario(result.getInt("id"), result.getString("nombre"),
                    result.getString("correo"), result.getString("contraseña"));
        }
        return new Estudiante(result.getInt("id"), result.getInt("estudiante_id"),
                result.getString("nombre"), result.getString("correo"), result.getString("contraseña"),
                result.getString("rut"), result.getString("curso"));
    }

    public Usuario porEmail(Connection connection, String correo) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT + " WHERE u.correo=?")) {
            statement.setString(1, correo);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? convertir(result) : null;
            }
        }
    }

    public Usuario porId(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT + " WHERE u.id=?")) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? convertir(result) : null;
            }
        }
    }

    public Estudiante porEstudianteId(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT + " WHERE e.id=?")) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? (Estudiante) convertir(result) : null;
            }
        }
    }

    public List<Usuario> listar(Connection connection) throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT + " ORDER BY u.nombre");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                lista.add(convertir(result));
            }
        }
        return lista;
    }

    public int crear(Connection connection, Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios(nombre,correo,`contraseña`,rol,rut) VALUES(?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, usuario.getNombre());
            statement.setString(2, usuario.getEmail());
            statement.setString(3, usuario.getContrasenia());
            statement.setString(4, usuario.getRol().toLowerCase());
            statement.setString(5, usuario instanceof Estudiante ? ((Estudiante) usuario).getRut() : null);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("No se pudo modificar el registro");
            }
        }
        Usuario creado = porEmail(connection, usuario.getEmail());
        if (creado == null) throw new SQLException("No se encontró la cuenta creada");
        return creado.getId();
    }

    public void actualizar(Connection connection, Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nombre=?,correo=?,`contraseña`=?,rut=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, usuario.getNombre());
            statement.setString(2, usuario.getEmail());
            statement.setString(3, usuario.getContrasenia());
            statement.setString(4, usuario instanceof Estudiante ? ((Estudiante) usuario).getRut() : null);
            statement.setInt(5, usuario.getId());
            if (statement.executeUpdate() != 1) {
                throw new SQLException("No se pudo modificar el registro");
            }
        }
    }

    public void eliminar(Connection connection, int id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("No se pudo modificar el registro");
            }
        }
    }

}
