package cl.biblioteca.dao;

import cl.biblioteca.modelo.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LibroDAO {
    private static final String SELECT = "SELECT l.*,c.nombre categoria FROM libros l JOIN categorias c ON c.id=l.id_categoria";

    private Libro convertir(Connection connection, ResultSet result) throws SQLException {
        return new Libro(result.getInt("id"), result.getString("titulo"), result.getString("autor"),
                result.getString("editorial"), result.getString("isbn"), result.getInt("id_categoria"),
                result.getString("categoria"), result.getInt("stock") + contarActivos(connection, result.getInt("id")), result.getInt("stock"));
    }

    private int contarActivos(Connection connection, int libroId) throws SQLException {
        int cantidad = 0;
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM prestamos WHERE id_libro=? AND devuelto=FALSE")) {
            statement.setInt(1, libroId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) cantidad++;
            }
        }
        return cantidad;
    }

    public Libro porId(Connection connection, int id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT + " WHERE l.id=?")) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return convertir(connection, result);
                }
                throw new SQLException("El libro no existe");
            }
        }
    }

    public List<Libro> listar(Connection connection) throws SQLException {
        List<Libro> lista = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SELECT + " ORDER BY l.titulo");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                lista.add(convertir(connection, result));
            }
        }
        return lista;
    }

    public int crear(Connection connection, Libro libro) throws SQLException {
        String sql = "INSERT INTO libros(titulo,autor,editorial,isbn,id_categoria,stock) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, libro.getTitulo().trim());
            statement.setString(2, libro.getAutor().trim());
            statement.setString(3, libro.getEditorial().trim());
            statement.setString(4, libro.getIsbn().trim());
            statement.setInt(5, libro.getCategoriaId());
            statement.setInt(6, libro.getStockTotal());
            if (statement.executeUpdate() != 1) {
                throw new SQLException("No se pudo modificar el registro");
            }
        }
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM libros WHERE isbn=?")) {
            statement.setString(1, libro.getIsbn().trim());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getInt("id");
                throw new SQLException("No se encontró el libro creado");
            }
        }
    }

    public void actualizar(Connection connection, Libro libro) throws SQLException {
        String sql = "UPDATE libros SET titulo=?,autor=?,editorial=?,isbn=?,id_categoria=?,stock=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, libro.getTitulo().trim());
            statement.setString(2, libro.getAutor().trim());
            statement.setString(3, libro.getEditorial().trim());
            statement.setString(4, libro.getIsbn().trim());
            statement.setInt(5, libro.getCategoriaId());
            statement.setInt(6, libro.getStockDisponible());
            statement.setInt(7, libro.getId());
            if (statement.executeUpdate() != 1) {
                throw new SQLException("No se pudo modificar el registro");
            }
        }
    }

    public void descontar(Connection connection, int id) throws SQLException {
        String sql = "UPDATE libros SET stock=stock-1 WHERE id=? AND stock>0";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("No se pudo modificar el registro");
            }
        }
    }

    public void reponer(Connection connection, int id) throws SQLException {
        String sql = "UPDATE libros SET stock=stock+1 WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("No se pudo modificar el registro");
            }
        }
    }

    public void eliminar(Connection connection, int id) throws SQLException {
        String sql = "DELETE FROM libros WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("No se pudo modificar el registro");
            }
        }
    }

}
