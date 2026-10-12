package cl.biblioteca.controlador;

import cl.biblioteca.modelo.*;
import cl.biblioteca.servicio.BibliotecaService;
import cl.biblioteca.vista.*;
import javax.swing.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BibliotecaController {
    private final BibliotecaService service;
    private BibliotecaFrame frame;
    private boolean trabajando;

    public BibliotecaController(BibliotecaService service) {
        this.service = service;
    }

    public void showLogin() {
        LoginFrame login = new LoginFrame();
        login.conexion.addActionListener(event -> configurarConexion(login));
        login.ingresar.addActionListener(event -> {
            login.busy(true);
            String correo = login.email.getText();
            char[] clave = login.password.getPassword();
            Thread hilo = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Usuario usuario = service.login(correo, clave);
                        SwingUtilities.invokeLater(() -> {
                            if (!login.isDisplayable()) {
                                return;
                            }
                            login.dispose();
                            abrir(usuario);
                        });
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> {
                            if (!login.isDisplayable()) {
                                return;
                            }
                            login.busy(false);
                            login.estado.setText("No se pudo ingresar");
                            error(login, e);
                        });
                    }
                }
            }, "ingreso-biblioteca");
            hilo.start();
        });
        login.setVisible(true);
    }

    private void configurarConexion(LoginFrame login) {
        Formularios.Conexion datos;
        try {
            datos = Formularios.conexion(login, service.datosConexion());
        } catch (IllegalArgumentException e) {
            error(login, e);
            return;
        }
        if (datos == null) return;
        login.busy(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    service.configurarConexion(datos.getServidor(), datos.getPuerto(), datos.getBase(),
                            datos.getUsuario(), datos.getClave());
                    SwingUtilities.invokeLater(() -> {
                        if (login.isDisplayable()) {
                            login.busy(false);
                            login.estado.setText("Conexión MySQL comprobada. Ingresa con tu cuenta");
                        }
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        if (login.isDisplayable()) {
                            login.busy(false);
                            login.estado.setText("Revisa la configuración de MySQL");
                            error(login, e);
                        }
                    });
                }
            }
        }, "conexion-mysql").start();
    }

    public BibliotecaFrame abrir(Usuario usuario) {
        frame = new BibliotecaFrame(usuario);
        conectarBotones();
        frame.setVisible(true);
        refresh();
        return frame;
    }

    // Cada operación define su tarea. El flujo común maneja el hilo y la vista.
    private abstract class Operacion implements Runnable {
        private final String mensaje;
        protected final BibliotecaFrame ventana;

        Operacion(String mensaje) {
            this.mensaje = mensaje;
            this.ventana = frame;
        }

        protected abstract void ejecutar() throws SQLException;

        public void iniciar() {
            if (trabajando) {
                return;
            }
            trabajando = true;
            ventana.busy(true, mensaje);
            new Thread(this, "operacion-biblioteca").start();
        }

        @Override
        public void run() {
            try {
                ejecutar();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    if (ventana.isDisplayable()) {
                        error(ventana, e);
                    }
                });
            } finally {
                SwingUtilities.invokeLater(() -> {
                    trabajando = false;
                    if (ventana.isDisplayable()) {
                        ventana.busy(false, "Listo");
                    }
                });
            }
        }
    }

    private void cargarDatos(BibliotecaFrame ventana) throws SQLException {
        Usuario usuario = ventana.usuario;
        List<Libro> libros = service.libros(usuario);
        List<Categoria> categorias = service.categorias(usuario);
        List<Estudiante> estudiantes = new ArrayList<>();
        List<Usuario> usuarios = new ArrayList<>();
        if (usuario.puedeAdministrar()) {
            estudiantes = service.estudiantes(usuario);
            usuarios = service.usuarios(usuario);
        }
        List<Prestamo> prestamos = service.prestamos(usuario);
        List<Estudiante> alumnosCargados = estudiantes;
        List<Usuario> usuariosCargados = usuarios;
        SwingUtilities.invokeLater(() -> {
            if (!ventana.isDisplayable()) {
                return;
            }
            ventana.showState(libros, alumnosCargados, categorias, usuariosCargados, prestamos, service.hoy());
            BibliotecaFrame.fill(ventana.reportes, new String[]{"Selecciona un reporte"}, new ArrayList<>());
        });
    }

    public void refresh() {
        new Operacion("Cargando datos...") {
            @Override
            protected void ejecutar() throws SQLException {
                cargarDatos(ventana);
            }
        }.iniciar();
    }

    private void boton(JButton boton, Runnable accion) {
        boton.addActionListener(event -> {
            try {
                accion.run();
            } catch (Exception e) {
                error(frame, e);
            }
        });
    }

    private void conectarBotones() {
        boton(frame.salir, () -> { frame.dispose(); showLogin(); });
        boton(frame.actualizarLibros, () -> refresh());
        boton(frame.actualizarPrestamos, () -> refresh());
        boton(frame.verReporte, () -> consultarReporte());
        boton(frame.prestar, () -> prestar());
        boton(frame.devolver, () -> devolver());
        if (frame.usuario.puedeAdministrar()) {
            boton(frame.actualizarEstudiantes, () -> refresh());
            boton(frame.actualizarCategorias, () -> refresh());
            boton(frame.actualizarUsuarios, () -> refresh());
            boton(frame.crearLibro, () -> guardarLibro(null));
            boton(frame.editarLibro, () -> guardarLibro(frame.listaLibros.get(BibliotecaFrame.selected(frame.libros))));
            boton(frame.eliminarLibro, () -> eliminar("libros", frame.listaLibros.get(BibliotecaFrame.selected(frame.libros)).getId()));
            boton(frame.crearCategoria, () -> guardarCategoria(null));
            boton(frame.editarCategoria, () -> guardarCategoria(frame.listaCategorias.get(BibliotecaFrame.selected(frame.categorias))));
            boton(frame.eliminarCategoria, () -> eliminar("categorias", frame.listaCategorias.get(BibliotecaFrame.selected(frame.categorias)).getId()));
            boton(frame.crearEstudiante, () -> guardarPersona(null, true));
            boton(frame.editarEstudiante, () -> guardarPersona(frame.listaEstudiantes.get(BibliotecaFrame.selected(frame.estudiantes)), true));
            boton(frame.eliminarEstudiante, () -> eliminar("estudiantes", frame.listaEstudiantes.get(BibliotecaFrame.selected(frame.estudiantes)).getEstudianteId()));
            boton(frame.crearUsuario, () -> guardarPersona(null, false));
            boton(frame.editarUsuario, () -> guardarPersona(frame.listaUsuarios.get(BibliotecaFrame.selected(frame.usuarios)), false));
            boton(frame.eliminarUsuario, () -> eliminar("usuarios", frame.listaUsuarios.get(BibliotecaFrame.selected(frame.usuarios)).getId()));
        }
    }

    private void guardarLibro(Libro anterior) {
        Libro libro = Formularios.libro(frame, anterior, frame.listaCategorias);
        if (libro == null) {
            return;
        }
        new Operacion("Guardando libro...") {
            @Override
            protected void ejecutar() throws SQLException {
                service.guardarLibro(ventana.usuario, libro);
                cargarDatos(ventana);
            }
        }.iniciar();
    }

    private void guardarCategoria(Categoria anterior) {
        Categoria categoria = Formularios.categoria(frame, anterior);
        if (categoria == null) {
            return;
        }
        new Operacion("Guardando categoría...") {
            @Override
            protected void ejecutar() throws SQLException {
                service.guardarCategoria(ventana.usuario, categoria);
                cargarDatos(ventana);
            }
        }.iniciar();
    }

    private void guardarPersona(Usuario anterior, boolean esEstudiante) {
        if (!esEstudiante && anterior instanceof Estudiante) {
            throw new IllegalArgumentException("Edita al estudiante desde su módulo");
        }
        Formularios.Persona persona = Formularios.persona(frame, anterior, esEstudiante);
        if (persona == null) {
            return;
        }
        new Operacion("Guardando cuenta...") {
            @Override
            protected void ejecutar() throws SQLException {
                if (esEstudiante) {
                    service.guardarEstudiante(ventana.usuario, persona.getId(), persona.getNombre(),
                            persona.getCorreo(), persona.getClave(), persona.getRut(), persona.getCurso());
                } else {
                    service.guardarBibliotecario(ventana.usuario, persona.getId(), persona.getNombre(),
                            persona.getCorreo(), persona.getClave());
                }
                cargarDatos(ventana);
            }
        }.iniciar();
    }

    private void eliminar(String entidad, int id) {
        if (!Formularios.confirmar(frame, "¿Eliminar el registro seleccionado?")) {
            return;
        }
        new Operacion("Eliminando registro...") {
            @Override
            protected void ejecutar() throws SQLException {
                service.eliminar(ventana.usuario, entidad, id);
                cargarDatos(ventana);
            }
        }.iniciar();
    }

    private void prestar() {
        Libro libro = frame.listaLibros.get(BibliotecaFrame.selected(frame.libros));
        Usuario usuario = frame.usuario;
        if (usuario.puedeAdministrar()) {
            Estudiante estudiante = Formularios.elegirEstudiante(frame, frame.listaEstudiantes);
            if (estudiante == null) {
                return;
            }
            new Operacion("Registrando préstamo...") {
                @Override
                protected void ejecutar() throws SQLException {
                    service.prestar(usuario, estudiante.getEstudianteId(), libro.getId());
                    cargarDatos(ventana);
                }
            }.iniciar();
        } else if (Formularios.confirmar(frame, "¿Solicitar un préstamo de «" + libro.getTitulo() + "» por 14 días?")) {
            new Operacion("Registrando préstamo...") {
                @Override
                protected void ejecutar() throws SQLException {
                    service.prestar(usuario, libro.getId());
                    cargarDatos(ventana);
                }
            }.iniciar();
        }
    }

    private void devolver() {
        Prestamo prestamo = frame.listaPrestamos.get(BibliotecaFrame.selected(frame.prestamos));
        if (!prestamo.activo()) {
            throw new IllegalArgumentException("Selecciona un préstamo activo");
        }
        if (!Formularios.confirmar(frame, "¿Registrar devolución de «" + prestamo.getLibro() + "»?")) {
            return;
        }
        new Operacion("Registrando devolución...") {
            @Override
            protected void ejecutar() throws SQLException {
                service.devolver(ventana.usuario, prestamo.getId());
                cargarDatos(ventana);
            }
        }.iniciar();
    }

    private void consultarReporte() {
        String tipo = (String) frame.tipoReporte.getSelectedItem();
        Estudiante estudiante = (Estudiante) frame.estudianteReporte.getSelectedItem();
        int id;
        if (frame.usuario.puedeAdministrar()) {
            id = estudiante == null ? 0 : estudiante.getEstudianteId();
        } else {
            id = ((Estudiante) frame.usuario).getEstudianteId();
        }
        if ("Historial por estudiante".equals(tipo) && id == 0) {
            throw new IllegalArgumentException("Selecciona un estudiante");
        }
        String[] columnas;
        switch (tipo) {
            case "Más prestados":
                columnas = new String[]{"Libro", "Préstamos acumulados"};
                break;
            case "Historial por estudiante":
                columnas = new String[]{"ID", "Estudiante", "Libro", "Préstamo", "Vencimiento", "Devolución", "Estado"};
                break;
            default:
                columnas = new String[]{"ID", "Estudiante", "Libro", "Préstamo", "Vencimiento", "Estado"};
        }
        new Operacion("Consultando reporte...") {
            @Override
            protected void ejecutar() throws SQLException {
                List<Object[]> filas = service.reporte(ventana.usuario, tipo, id);
                SwingUtilities.invokeLater(() -> {
                    if (ventana.isDisplayable()) {
                        BibliotecaFrame.fill(ventana.reportes, columnas, filas);
                    }
                });
            }
        }.iniciar();
    }

    private static void error(java.awt.Component ventana, Exception e) {
        String mensaje = e.getMessage();
        if (e instanceof SQLException) {
            e.printStackTrace();
            SQLException sql = (SQLException) e;
            switch (sql.getErrorCode()) {
                case 1045:
                    mensaje = "Usuario o contraseña de MySQL incorrectos. Revisa Conexión MySQL.";
                    break;
                case 1049:
                    mensaje = "No existe la base indicada. Importa los scripts SQL y revisa Conexión MySQL.";
                    break;
                case 1146:
                    mensaje = "Faltan tablas de la biblioteca. Importa los tres scripts SQL en el orden del README.";
                    break;
                case 1062:
                    mensaje = "Ya existe ese correo, RUT o ISBN. Revisa los datos.";
                    break;
                case 1451:
                    mensaje = "Este registro tiene información asociada y debe conservarse.";
                    break;
                case 1452:
                    mensaje = "La relación seleccionada ya no existe. Actualiza los datos.";
                    break;
                default:
                    mensaje = "No se pudo completar la operación con MySQL. Inicia el servidor o revisa Conexión MySQL.";
            }
        }
        JOptionPane.showMessageDialog(ventana, mensaje == null ? "No se pudo completar la operación" : mensaje,
                "Biblioteca Escolar", JOptionPane.WARNING_MESSAGE);
    }
}
