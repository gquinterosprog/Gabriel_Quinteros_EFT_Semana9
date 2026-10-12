package cl.biblioteca;

import cl.biblioteca.modelo.*;
import cl.biblioteca.dao.*;
import cl.biblioteca.servicio.BibliotecaService;
import cl.biblioteca.infraestructura.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class IntegrationTest {
    private static final BibliotecaService service = new BibliotecaService();
    private static final DatabaseConnection db = DatabaseConnection.getInstance();
    private static final List<Integer> estudiantes = new ArrayList<>();
    private static final List<Integer> libros = new ArrayList<>();
    private static final List<Integer> categorias = new ArrayList<>();
    private static final List<Integer> bibliotecarios = new ArrayList<>();
    private static final String marca = "Q" + System.currentTimeMillis() % 1000000;
    private static Usuario admin;
    private static int aprobadas;

    private interface Accion {
        void ejecutar() throws Exception;
    }

    private static void comprobar(boolean resultado, String nombre) {
        if (!resultado) {
            throw new AssertionError(nombre);
        }
        aprobadas++;
        System.out.println("OK " + nombre);
    }

    private static void rechazar(Accion accion, String nombre) throws Exception {
        boolean rechazado = false;
        try {
            accion.ejecutar();
        } catch (IllegalArgumentException | SQLException e) {
            rechazado = true;
        }
        comprobar(rechazado, nombre);
    }

    private static int crearEstudiante(int numero) throws SQLException {
        int id = service.guardarEstudiante(admin, 0, marca + " Estudiante " + numero,
                marca + numero + "@prueba.cl", "Prueba123".toCharArray(), marca + "-" + numero, "Prueba");
        estudiantes.add(id);
        return id;
    }

    private static Libro datosLibro(int id, String isbn, int total) {
        return new Libro(id, marca + " Libro", "Autor", "Editorial", isbn,
                categorias.get(0), "", total, 0);
    }

    private static int crearLibro(int total) throws SQLException {
        int id = service.guardarLibro(admin, datosLibro(0, marca + "-" + libros.size(), total));
        libros.add(id);
        return id;
    }

    private static Libro leerLibro(int id) throws SQLException {
        for (Libro libro : service.libros(admin)) {
            if (libro.getId() == id) {
                return libro;
            }
        }
        throw new AssertionError("Libro no encontrado");
    }

    private static Prestamo leerPrestamo(int id) throws SQLException {
        for (Prestamo prestamo : service.prestamos(admin)) {
            if (prestamo.getId() == id) {
                return prestamo;
            }
        }
        throw new AssertionError("Préstamo no encontrado");
    }

    private static int contar(String sql, int id) throws SQLException {
        try (PreparedStatement statement = db.getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                int cantidad = 0;
                while (result.next()) cantidad++;
                return cantidad;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            admin = service.login("antonia@correo.cl", "clave123".toCharArray());
            Usuario alumno = service.login("carlos@correo.cl", "clave123".toCharArray());
            comprobar(admin instanceof Bibliotecario && alumno instanceof Estudiante, "Login con los roles de los scripts");
            comprobar(alumno.getId() == 2 && ((Estudiante) alumno).getEstudianteId() == 1,
                    "Carlos conserva cuenta 2 y perfil 1");
            Estudiante luis = (Estudiante) service.login("luis@correo.cl", "clave123".toCharArray());
            comprobar(luis.getEstudianteId() == 3, "Luis conserva su perfil del poblado");
            comprobar(admin.puedeAdministrar() && !alumno.puedeAdministrar(), "Permisos sobrescritos según el rol");
            comprobar(admin instanceof cl.biblioteca.contrato.Identificable
                    && admin instanceof cl.biblioteca.contrato.Permisos, "Usuario implementa dos interfaces");
            comprobar(alumno.resumen().contains("ESTUDIANTE") && alumno.resumen(false).contains("Medio"),
                    "Sobrecarga y sobrescritura de resumen");
            comprobar(db == DatabaseConnection.getInstance() && db.getConnection() == db.getConnection(),
                    "Singleton reutiliza la conexión");
            rechazar(() -> service.login("antonia@correo.cl", "error".toCharArray()), "Rechaza contraseña incorrecta");
            rechazar(() -> service.login("' OR 1=1 --", "clave123".toCharArray()), "Login parametrizado rechaza entrada SQL");
            for (String email : new String[]{"a.@", "x@y.", "x@@y.cl", "a\tb@correo.cl", "@correo.cl", "a@.cl"}) {
                rechazar(() -> service.guardarBibliotecario(admin, 0, marca, email, "Prueba123".toCharArray()),
                        "Rechaza correo mal formado: " + email.replace("\t", "[TAB]"));
            }
            rechazar(() -> service.guardarBibliotecario(admin, 0, marca, marca + "@prueba.cl", "        ".toCharArray()),
                    "Rechaza contraseña formada solo por espacios");
            int sinPerfil = new UsuarioDAO().crear(db.getConnection(), new Estudiante(0, 0, marca,
                    marca + "sinperfil@prueba.cl", "Prueba123", marca + "-SP", "Prueba"));
            bibliotecarios.add(sinPerfil);
            Usuario cuentaSinPerfil = new UsuarioDAO().porId(db.getConnection(), sinPerfil);
            rechazar(() -> service.login(cuentaSinPerfil.getEmail(), "Prueba123".toCharArray()),
                    "Cuenta de estudiante sin perfil no inicia sesión");
            rechazar(() -> service.prestamos(cuentaSinPerfil), "Cuenta sin perfil no consulta préstamos ajenos");
            rechazar(() -> service.reporte(cuentaSinPerfil, "Préstamos activos", 0),
                    "Cuenta sin perfil no consulta reportes ajenos");
            new UsuarioDAO().eliminar(db.getConnection(), sinPerfil);
            bibliotecarios.remove(Integer.valueOf(sinPerfil));
            Prestamo oficial = leerPrestamo(1);
            comprobar(oficial.getEstudiante().equals("Carlos Ruiz") && oficial.activo()
                    && oficial.getDevolucion() == null && oficial.getVencimiento().equals("2025-06-22"),
                    "Interpreta correctamente el préstamo oficial pendiente");
            comprobar(contar("SELECT id FROM prestamos WHERE id<=?", 10) == 10, "Conserva diez préstamos oficiales");

            int categoria = service.guardarCategoria(admin, new Categoria(0, marca));
            categorias.add(categoria);
            service.guardarCategoria(admin, new Categoria(categoria, marca + " Editada"));
            boolean editada = false;
            for (Categoria c : service.categorias(admin)) {
                if (c.getId() == categoria && c.getNombre().endsWith("Editada")) {
                    editada = true;
                }
            }
            comprobar(editada, "Categoría: crear, leer y actualizar");
            int categoriaSinTilde = service.guardarCategoria(admin, new Categoria(0, "Tecnologia"));
            categorias.add(categoriaSinTilde);
            boolean identificadorCorrecto = false;
            for (Categoria c : service.categorias(admin)) {
                if (c.getId() == categoriaSinTilde && c.getNombre().equals("Tecnologia")) identificadorCorrecto = true;
            }
            comprobar(identificadorCorrecto, "Recupera el ID creado aunque MySQL compare nombres sin distinguir tildes");
            int a = crearEstudiante(0);
            int b = crearEstudiante(1);
            service.guardarEstudiante(admin, a, marca + " Editado", marca + "0@prueba.cl",
                    new char[0], marca + "-0", "Curso editado");
            Estudiante editado = (Estudiante) service.login(marca + "0@prueba.cl", "Prueba123".toCharArray());
            comprobar(editado.getEstudianteId() == a && editado.getCurso().equals("Curso editado"),
                    "Estudiante: crear, leer, actualizar y conservar contraseña");
            rechazar(() -> service.guardarEstudiante(admin, 0, marca, editado.getEmail(),
                    "Prueba123".toCharArray(), marca + "-D", "Prueba"), "Rechaza correo duplicado");
            rechazar(() -> service.guardarEstudiante(admin, a, marca, editado.getEmail(),
                    new char[0], marca + "-1", "Prueba"), "Rechaza RUT duplicado al editar");
            comprobar(service.login(editado.getEmail(), "Prueba123".toCharArray()).getNombre().equals(editado.getNombre()),
                    "Edición rechazada conserva la cuenta anterior");
            int usuario = service.guardarBibliotecario(admin, 0, marca + " Bibliotecario",
                    marca + "b@prueba.cl", "Prueba123".toCharArray());
            bibliotecarios.add(usuario);
            service.guardarBibliotecario(admin, usuario, marca + " Editado", marca + "b@prueba.cl", new char[0]);
            comprobar(service.login(marca + "b@prueba.cl", "Prueba123".toCharArray()).getNombre().endsWith("Editado"),
                    "Bibliotecario: crear, leer y actualizar");
            rechazar(() -> service.guardarBibliotecario(admin, admin.getId(), "Otro", admin.getEmail(), new char[0]),
                    "Protege la cuenta de la sesión");
            int libro = crearLibro(2);
            String isbn = leerLibro(libro).getIsbn();
            service.guardarLibro(admin, datosLibro(libro, isbn, 3));
            comprobar(leerLibro(libro).getStockDisponible() == 3, "Libro: crear, leer y actualizar stock");
            rechazar(() -> service.guardarLibro(admin, datosLibro(0, "", 1)), "Valida ISBN obligatorio");
            rechazar(() -> service.guardarLibro(admin, datosLibro(0, isbn, 1)), "Rechaza ISBN duplicado");
            rechazar(() -> service.guardarLibro(admin, datosLibro(0, marca + "-neg", -1)), "Rechaza stock negativo");
            int agotado = crearLibro(0);
            rechazar(() -> service.prestar(admin, a, agotado), "No presta un libro con stock cero");
            comprobar(contar("SELECT id FROM prestamos WHERE id_libro=?", agotado) == 0,
                    "Solicitud sin stock no crea historial");
            rechazar(() -> service.guardarLibro(alumno, datosLibro(0, "OTRO", 1)), "Estudiante no administra libros");
            rechazar(() -> service.estudiantes(alumno), "Estudiante no consulta otros perfiles");
            rechazar(() -> service.prestar(alumno, b, libro), "Estudiante no solicita préstamos ajenos");

            int prestamo = service.prestar(admin, a, libro);
            comprobar(leerLibro(libro).getStockDisponible() == 2, "Préstamo descuenta un ejemplar");
            comprobar(service.reporte(admin, "Historial por estudiante", a).size() == 1,
                    "El préstamo queda persistido en el historial");
            Prestamo registro = leerPrestamo(prestamo);
            String vencimiento = Fecha.sumarDias(registro.getFechaPrestamo(), 14);
            comprobar(registro.getVencimiento().equals(vencimiento), "Calcula el vencimiento a 14 días");
            comprobar(Fecha.sumarDias("2026-04-01", 14)
                    .equals("2026-04-15"), "El plazo conserva 14 fechas de calendario al cambiar la hora");
            comprobar(Fecha.sumarDias("2024-02-20", 14)
                    .equals("2024-03-05"), "El plazo contempla el año bisiesto");
            rechazar(() -> service.prestar(admin, a, libro), "Impide duplicar un préstamo activo");
            rechazar(() -> service.guardarLibro(admin, datosLibro(libro, isbn, 0)), "No reduce total por debajo de cantidad prestada");
            rechazar(() -> service.eliminar(admin, "libros", libro), "Conserva un libro con historial");
            rechazar(() -> service.eliminar(admin, "estudiantes", a), "Conserva un estudiante con préstamos");
            rechazar(() -> service.devolver(alumno, prestamo), "Estudiante no devuelve préstamos ajenos");
            service.devolver(admin, prestamo);
            comprobar(leerLibro(libro).getStockDisponible() == 3 && !leerPrestamo(prestamo).activo(),
                    "Devolución repone stock y conserva el historial");
            rechazar(() -> service.devolver(admin, prestamo), "Doble devolución no duplica stock");
            int atrasado = service.prestar(admin, b, libro);
            try (PreparedStatement statement = db.getConnection().prepareStatement("UPDATE detalle_prestamos SET fecha_vencimiento=? WHERE id_prestamo=?")) {
                statement.setString(1, "2000-01-01");
                statement.setInt(2, atrasado);
                statement.executeUpdate();
            }
            boolean vencido = false;
            for (Object[] fila : service.reporte(admin, "Historial por estudiante", b)) {
                if (((Integer) fila[0]) == atrasado && "VENCIDO".equals(fila[6])) {
                    vencido = true;
                }
            }
            comprobar(vencido, "Historial identifica un préstamo vencido sin devolución");
            service.devolver(admin, atrasado);
            comprobar(leerPrestamo(atrasado).isAtraso(), "Registra devolución con atraso");
            rechazar(() -> service.reporte(alumno, "Historial por estudiante", b), "Historial propio para estudiantes");
            comprobar(!service.reporte(admin, "Más prestados", 0).isEmpty(), "Reporte de libros más prestados");
            int propio = service.prestar(editado, libro);
            comprobar(leerPrestamo(propio).getEstudianteId() == a, "Sobrecarga presta al estudiante conectado");
            boolean soloPropios = true;
            for (Prestamo p : service.prestamos(editado)) {
                if (p.getEstudianteId() != a) soloPropios = false;
            }
            comprobar(soloPropios, "Lista del estudiante contiene solo sus préstamos");
            boolean activoPropio = false;
            for (Object[] fila : service.reporte(editado, "Préstamos activos", 0)) {
                if (((Integer) fila[0]) == propio) activoPropio = true;
                comprobar(fila[1].equals(editado.getNombre()), "Reporte activo respeta el estudiante conectado");
            }
            comprobar(activoPropio, "Reporte activo incluye el préstamo propio");
            service.devolver(editado, propio);

            int unico = crearLibro(1);
            for (int i = 2; i < 12; i++) {
                crearEstudiante(i);
            }
            AtomicInteger aceptados = new AtomicInteger();
            AtomicInteger errores = new AtomicInteger();
            ExecutorService executor = Executors.newFixedThreadPool(12);
            for (int id : estudiantes) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            new BibliotecaService().prestar(admin, id, unico);
                            aceptados.incrementAndGet();
                        } catch (IllegalArgumentException e) {
                            // Los otros solicitantes encuentran el stock agotado.
                        } catch (SQLException e) {
                            errores.incrementAndGet();
                        }
                    }
                });
            }
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                throw new AssertionError("Los hilos no finalizaron");
            }
            comprobar(errores.get() == 0 && aceptados.get() == 1 && leerLibro(unico).getStockDisponible() == 0,
                    "12 tareas para un ejemplar: un préstamo aceptado, stock cero");
            comprobar(contar("SELECT id FROM prestamos WHERE id_libro=?", unico) == 1,
                    "Los hilos guardan exactamente un préstamo");
            int concurrente = 0;
            for (Prestamo r : service.prestamos(admin)) {
                if (r.getLibroId() == unico) {
                    concurrente = r.getId();
                }
            }
            boolean presente = false;
            for (Object[] fila : service.reporte(admin, "Préstamos activos", 0)) {
                if (((Integer) fila[0]) == concurrente) {
                    presente = true;
                }
            }
            comprobar(presente, "Reporte de préstamos activos");
            service.devolver(admin, concurrente);
            comprobar(leerLibro(unico).getStockDisponible() == 1, "Devolución del caso concurrente repone un ejemplar");

            PrestamoDAO dao = new PrestamoDAO();
            int temporal = dao.crear(db.getConnection(), new Prestamo(0, a, "", libro, "", service.hoy(), vencimiento, null, false));
            dao.actualizar(db.getConnection(), new Prestamo(temporal, a, "", libro, "", service.hoy(), vencimiento, service.hoy(), false));
            comprobar(!dao.porId(db.getConnection(), temporal).activo(), "DAO préstamo: crear, leer y actualizar");
            dao.eliminar(db.getConnection(), temporal);
            comprobar(contar("SELECT id FROM prestamos WHERE id=?", temporal) == 0, "DAO préstamo: eliminar");
            int sinHistorial = crearLibro(1);
            service.eliminar(admin, "libros", sinHistorial);
            libros.remove(Integer.valueOf(sinHistorial));
            comprobar(contar("SELECT id FROM libros WHERE id=?", sinHistorial) == 0, "Libro: eliminar sin historial");
            int alumnoTemporal = crearEstudiante(99);
            service.eliminar(admin, "estudiantes", alumnoTemporal);
            estudiantes.remove(Integer.valueOf(alumnoTemporal));
            comprobar(contar("SELECT id FROM estudiantes WHERE id=?", alumnoTemporal) == 0,
                    "Estudiante: eliminar perfil y cuenta sin préstamos");
            service.eliminar(admin, "usuarios", usuario);
            bibliotecarios.remove(Integer.valueOf(usuario));
            comprobar(contar("SELECT id FROM usuarios WHERE id=?", usuario) == 0, "Bibliotecario: eliminar");
            int categoriaTemporal = service.guardarCategoria(admin, new Categoria(0, marca + " Temporal"));
            service.eliminar(admin, "categorias", categoriaTemporal);
            comprobar(contar("SELECT id FROM categorias WHERE id=?", categoriaTemporal) == 0, "Categoría: eliminar");
            comprobar(Fecha.sumarDias("2026-12-28", 14).equals("2027-01-11"), "El plazo cambia de año correctamente");
            comprobar(Fecha.sumarDias("2100-02-20", 14).equals("2100-03-06"), "El año 2100 no se considera bisiesto");
            comprobar(!Fecha.posterior("2026-10-11", "2026-10-11"), "Devolver en la fecha límite no genera atraso");
            comprobar(Fecha.posterior("2027-01-01", "2026-12-31"), "Detecta atraso al cambiar de año");
            System.out.println("RESULTADO: " + aprobadas + " verificaciones aprobadas contra MySQL");
        } finally {
            limpiar();
            db.close();
        }
    }

    private static void borrar(String sql, int id) throws SQLException {
        try (PreparedStatement statement = db.getConnection().prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private static void limpiar() throws SQLException {
        for (int id : libros) {
            PrestamoDAO dao = new PrestamoDAO();
            for (Prestamo prestamo : dao.listar(db.getConnection())) {
                if (prestamo.getLibroId() == id) dao.eliminar(db.getConnection(), prestamo.getId());
            }
            borrar("DELETE FROM libros WHERE id=?", id);
        }
        for (int id : estudiantes) {
            new EstudianteDAO().eliminar(db.getConnection(), id);
        }
        for (int id : bibliotecarios) {
            borrar("DELETE FROM usuarios WHERE id=?", id);
        }
        for (int id : categorias) {
            borrar("DELETE FROM categorias WHERE id=?", id);
        }
    }
}
