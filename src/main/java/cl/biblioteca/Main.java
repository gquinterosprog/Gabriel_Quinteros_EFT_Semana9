package cl.biblioteca;

import cl.biblioteca.controlador.BibliotecaController;
import cl.biblioteca.servicio.BibliotecaService;
import cl.biblioteca.infraestructura.DatabaseConnection;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 1 && "--verificar-conexion".equals(args[0])) {
            try {
                new BibliotecaService().login("antonia@correo.cl", "clave123".toCharArray());
                System.out.println("Conexión JDBC y autenticación correctas");
            } finally {
                DatabaseConnection.getInstance().close();
            }
            return;
        }
        SwingUtilities.invokeLater(() -> new BibliotecaController(new BibliotecaService()).showLogin());
    }
}
