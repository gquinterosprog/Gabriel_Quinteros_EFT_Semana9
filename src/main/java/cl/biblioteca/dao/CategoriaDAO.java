package cl.biblioteca.dao;

import cl.biblioteca.modelo.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {
    public int crear(Connection connection, Categoria categoria) throws SQLException {
        String sql = "INSERT INTO categorias(nombre) VALUES(?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, categoria.getNombre());
            if (statement.executeUpdate() != 1) {
                throw new SQLException("No se pudo modificar el registro");
            }
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM categorias WHERE nombre=? ORDER BY id DESC")) {
            statement.setString(1, categoria.getNombre());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getInt("id");
                throw new SQLException("No se encontró la categoría creada");
            }
        }
    }

    public List<Categoria> listar(Connection connection) throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT id,nombre FROM categorias ORDER BY nombre");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                lista.add(new Categoria(result.getInt("id"), result.getString("nombre")));
            }
        }
        return lista;
    }

    public void actualizar(Connection connection, Categoria categoria) throws SQLException {
        String sql = "UPDATE categorias SET nombre=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, categoria.getNombre());
            statement.setInt(2, categoria.getId());
            if (statement.executeUpdate() != 1) {
                throw new SQLException("No se pudo modificar el registro");
            }
        }
    }

    public void eliminar(Connection connection, int id) throws SQLException {
        String sql = "DELETE FROM categorias WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("No se pudo modificar el registro");
            }
        }
    }

}
