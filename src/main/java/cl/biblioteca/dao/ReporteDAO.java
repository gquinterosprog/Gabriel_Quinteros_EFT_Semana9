package cl.biblioteca.dao;

import cl.biblioteca.modelo.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {
    private final PrestamoDAO prestamos = new PrestamoDAO();

    public List<Object[]> masPrestados(Connection connection) throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        List<Prestamo> historial = prestamos.listar(connection);
        try (PreparedStatement statement = connection.prepareStatement("SELECT id,titulo FROM libros ORDER BY titulo");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                int cantidad = 0;
                for (Prestamo prestamo : historial) {
                    if (prestamo.getLibroId() == result.getInt("id")) cantidad++;
                }
                filas.add(new Object[]{result.getString("titulo"), cantidad});
            }
        }
        for (int i = 0; i < filas.size(); i++) {
            for (int j = 0; j < filas.size() - 1; j++) {
                if ((Integer) filas.get(j)[1] < (Integer) filas.get(j + 1)[1]) {
                    Object[] anterior = filas.get(j);
                    filas.set(j, filas.get(j + 1));
                    filas.set(j + 1, anterior);
                }
            }
        }
        return filas;
    }

    public List<Object[]> historial(Connection connection, int estudianteId) throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        for (Prestamo prestamo : prestamos.listar(connection, estudianteId)) {
            filas.add(new Object[]{prestamo.getId(), prestamo.getEstudiante(), prestamo.getLibro(),
                    prestamo.getFechaPrestamo(), prestamo.getVencimiento(), prestamo.getDevolucion(),
                    prestamo.estado(Fecha.hoy())});
        }
        return filas;
    }

    public List<Object[]> activos(Connection connection, int estudianteId) throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        for (Prestamo prestamo : prestamos.listar(connection, estudianteId)) {
            if (prestamo.activo()) {
                filas.add(new Object[]{prestamo.getId(), prestamo.getEstudiante(), prestamo.getLibro(),
                        prestamo.getFechaPrestamo(), prestamo.getVencimiento(), prestamo.estado(Fecha.hoy())});
            }
        }
        return filas;
    }
}
