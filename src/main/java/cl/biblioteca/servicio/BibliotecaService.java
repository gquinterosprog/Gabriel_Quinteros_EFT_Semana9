package cl.biblioteca.servicio;

import cl.biblioteca.modelo.*;
import cl.biblioteca.dao.*;
import cl.biblioteca.contrato.Permisos;
import cl.biblioteca.contrato.Identificable;
import cl.biblioteca.infraestructura.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BibliotecaService {
    private final DatabaseConnection db = DatabaseConnection.getInstance();
    private final UsuarioDAO usuarios = new UsuarioDAO();
    private final EstudianteDAO estudiantes = new EstudianteDAO();
    private final LibroDAO libros = new LibroDAO();
    private final CategoriaDAO categorias = new CategoriaDAO();
    private final PrestamoDAO prestamos = new PrestamoDAO();
    private final ReporteDAO reportes = new ReporteDAO();

    public String hoy() {
        return Fecha.hoy();
    }

    private void limpiarClave(char[] clave) {
        for (int i = 0; i < clave.length; i++) clave[i] = 0;
    }

    public String[] datosConexion() {
        return db.getDatos();
    }

    public void configurarConexion(String servidor, int puerto, String base, String usuario, char[] clave)
            throws SQLException {
        synchronized (db) {
            try {
                db.configurar(servidor, puerto, base, usuario, new String(clave));
                db.getConnection();
            } finally {
                limpiarClave(clave);
            }
        }
    }

    public Usuario login(String correo, char[] clave) throws SQLException {
        synchronized (db) {
            try {
                Usuario usuario = usuarios.porEmail(db.getConnection(), correo.trim().toLowerCase());
                if (usuario == null || !usuario.getContrasenia().equals(new String(clave))) {
                    throw new IllegalArgumentException("Correo o contraseña incorrectos");
                }
                if (usuario instanceof Estudiante && ((Estudiante) usuario).getEstudianteId() <= 0) {
                    throw new IllegalArgumentException("La cuenta no tiene un perfil de estudiante. Consulta al bibliotecario");
                }
                return usuario;
            } finally {
                limpiarClave(clave);
            }
        }
    }

    private Usuario validar(Connection connection, Usuario actor) throws SQLException {
        if (actor == null) {
            throw new IllegalArgumentException("Debes iniciar sesión");
        }
        Identificable identificacion = actor;
        Usuario actual = usuarios.porId(connection, identificacion.getId());
        if (actual == null || !actual.getRol().equals(actor.getRol())
                || !actual.getContrasenia().equals(actor.getContrasenia())) {
            throw new IllegalArgumentException("La sesión cambió. Ingresa nuevamente");
        }
        if (actual instanceof Estudiante && ((Estudiante) actual).getEstudianteId() <= 0) {
            throw new IllegalArgumentException("La cuenta no tiene un perfil de estudiante");
        }
        return actual;
    }

    private void admin(Connection connection, Usuario actor) throws SQLException {
        Permisos permisos = validar(connection, actor);
        if (!permisos.puedeAdministrar()) {
            throw new IllegalArgumentException("Esta operación requiere rol bibliotecario");
        }
    }

    private void comprobarEstudiante(Connection connection, Usuario actor, int id) throws SQLException {
        Usuario actual = validar(connection, actor);
        if (!actual.puedeAdministrar()) {
            Estudiante estudiante = (Estudiante) actual;
            if (id != estudiante.getEstudianteId()) {
                throw new IllegalArgumentException("Solo puedes gestionar tus propios préstamos");
            }
        }
    }

    private String texto(String valor, String campo, int maximo) {
        if (valor == null || valor.trim().isEmpty() || valor.trim().length() > maximo) {
            throw new IllegalArgumentException(campo + " es obligatorio y admite hasta " + maximo + " caracteres");
        }
        return valor.trim();
    }

    private String correo(String valor) {
        valor = texto(valor, "Correo", 100).toLowerCase();
        int arroba = valor.indexOf('@');
        int punto = valor.lastIndexOf('.');
        if (arroba <= 0 || arroba != valor.lastIndexOf('@') || punto <= arroba + 1
                || punto == valor.length() - 1 || valor.charAt(arroba - 1) == '.') {
            throw new IllegalArgumentException("Correo inválido");
        }
        for (int i = 0; i < valor.length(); i++) {
            if (valor.charAt(i) <= ' ') {
                throw new IllegalArgumentException("Correo inválido");
            }
        }
        return valor;
    }

    private String comprobarCorreo(Connection connection, String valor, Usuario anterior) throws SQLException {
        String email = correo(valor);
        Usuario existente = usuarios.porEmail(connection, email);
        if (existente != null && (anterior == null || existente.getId() != anterior.getId())) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo");
        }
        return email;
    }

    private String contrasenia(char[] clave, Usuario anterior) {
        if (clave.length == 0 && anterior != null) {
            return anterior.getContrasenia();
        }
        if (clave.length < 8 || clave.length > 100 || new String(clave).trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña debe tener entre 8 y 100 caracteres");
        }
        return new String(clave);
    }

    public List<Libro> libros(Usuario actor) throws SQLException {
        synchronized (db) {
            Connection connection = db.getConnection();
            validar(connection, actor);
            return libros.listar(connection);
        }
    }

    public List<Categoria> categorias(Usuario actor) throws SQLException {
        synchronized (db) {
            Connection connection = db.getConnection();
            validar(connection, actor);
            return categorias.listar(connection);
        }
    }

    public List<Estudiante> estudiantes(Usuario actor) throws SQLException {
        synchronized (db) {
            Connection connection = db.getConnection();
            admin(connection, actor);
            return estudiantes.listar(connection);
        }
    }

    public List<Usuario> usuarios(Usuario actor) throws SQLException {
        synchronized (db) {
            Connection connection = db.getConnection();
            admin(connection, actor);
            return usuarios.listar(connection);
        }
    }

    public List<Prestamo> prestamos(Usuario actor) throws SQLException {
        synchronized (db) {
            Connection connection = db.getConnection();
            Usuario actual = validar(connection, actor);
            if (actual.puedeAdministrar()) {
                return prestamos.listar(connection);
            }
            return prestamos.listar(connection, ((Estudiante) actual).getEstudianteId());
        }
    }

    public synchronized int guardarLibro(Usuario actor, Libro libro) throws SQLException {
        synchronized (db) {
            Connection connection = db.getConnection();
            admin(connection, actor);
            texto(libro.getTitulo(), "Título", 200);
            texto(libro.getAutor(), "Autor", 100);
            texto(libro.getEditorial(), "Editorial", 100);
            texto(libro.getIsbn(), "ISBN", 20);
            if (libro.getStockTotal() < 0) {
                throw new IllegalArgumentException("El stock no puede ser negativo");
            }
            if (libro.getId() == 0) {
                return libros.crear(connection, libro);
            }
            Libro anterior = libros.porId(connection, libro.getId());
            int cantidadPrestada = anterior.getStockTotal() - anterior.getStockDisponible();
            if (libro.getStockTotal() < cantidadPrestada) {
                throw new IllegalArgumentException("El total no puede ser menor que la cantidad prestada");
            }
            Libro actualizado = new Libro(libro.getId(), libro.getTitulo().trim(), libro.getAutor().trim(),
                    libro.getEditorial().trim(), libro.getIsbn().trim(), libro.getCategoriaId(),
                    libro.getCategoria(), libro.getStockTotal(), libro.getStockTotal() - cantidadPrestada);
            libros.actualizar(connection, actualizado);
            return libro.getId();
        }
    }

    public int guardarCategoria(Usuario actor, Categoria categoria) throws SQLException {
        synchronized (db) {
            Connection connection = db.getConnection();
            admin(connection, actor);
            Categoria nueva = new Categoria(categoria.getId(), texto(categoria.getNombre(), "Categoría", 100));
            for (Categoria existente : categorias.listar(connection)) {
                if (existente.getId() != nueva.getId() && existente.getNombre().equalsIgnoreCase(nueva.getNombre())) {
                    throw new IllegalArgumentException("Ya existe esa categoría");
                }
            }
            if (nueva.getId() == 0) {
                return categorias.crear(connection, nueva);
            }
            categorias.actualizar(connection, nueva);
            return nueva.getId();
        }
    }

    public int guardarEstudiante(Usuario actor, int id, String nombre, String email,
                                 char[] clave, String rut, String curso) throws SQLException {
        synchronized (db) {
            try {
                Connection connection = db.getConnection();
                admin(connection, actor);
                Usuario anterior = id == 0 ? null : usuarios.porEstudianteId(connection, id);
                if (id != 0 && anterior == null) {
                    throw new IllegalArgumentException("Selecciona un estudiante");
                }
                Estudiante estudiante = new Estudiante(anterior == null ? 0 : anterior.getId(), id,
                        texto(nombre, "Nombre", 100), comprobarCorreo(connection, email, anterior), contrasenia(clave, anterior),
                        texto(rut, "RUT", 12), texto(curso, "Curso", 20));
                if (id == 0) {
                    return estudiantes.crear(connection, estudiante);
                }
                estudiantes.actualizar(connection, estudiante);
                return id;
            } finally {
                limpiarClave(clave);
            }
        }
    }

    public int guardarBibliotecario(Usuario actor, int id, String nombre, String email, char[] clave)
            throws SQLException {
        synchronized (db) {
            try {
                Connection connection = db.getConnection();
                admin(connection, actor);
                Usuario anterior = id == 0 ? null : usuarios.porId(connection, id);
                if (id != 0 && !(anterior instanceof Bibliotecario)) {
                    throw new IllegalArgumentException("Los estudiantes se editan en su módulo");
                }
                if (id == actor.getId()) {
                    throw new IllegalArgumentException("No puedes modificar tu cuenta durante esta sesión");
                }
                Usuario usuario = new Bibliotecario(id, texto(nombre, "Nombre", 100), comprobarCorreo(connection, email, anterior),
                        contrasenia(clave, anterior));
                if (id == 0) {
                    return usuarios.crear(connection, usuario);
                }
                usuarios.actualizar(connection, usuario);
                return id;
            } finally {
                limpiarClave(clave);
            }
        }
    }

    public synchronized void eliminar(Usuario actor, String entidad, int id) throws SQLException {
        synchronized (db) {
            Connection connection = db.getConnection();
            admin(connection, actor);
            switch (entidad) {
                case "libros":
                    libros.eliminar(connection, id);
                    break;
                case "categorias":
                    categorias.eliminar(connection, id);
                    break;
                case "estudiantes":
                    estudiantes.eliminar(connection, id);
                    break;
                case "usuarios":
                    if (id == actor.getId()) {
                        throw new IllegalArgumentException("No puedes eliminar tu sesión actual");
                    }
                    if (!(usuarios.porId(connection, id) instanceof Bibliotecario)) {
                        throw new IllegalArgumentException("Elimina al estudiante desde su módulo");
                    }
                    usuarios.eliminar(connection, id);
                    break;
                default:
                    throw new IllegalArgumentException("Entidad inválida");
            }
        }
    }

    // Sobrecarga para que un estudiante solicite su propio préstamo.
    public int prestar(Usuario actor, int libroId) throws SQLException {
        if (!(actor instanceof Estudiante)) {
            throw new IllegalArgumentException("Selecciona el estudiante destinatario");
        }
        return prestar(actor, ((Estudiante) actor).getEstudianteId(), libroId);
    }

    // La comprobación y la modificación ocurren dentro de la misma sección crítica.
    public synchronized int prestar(Usuario actor, int estudianteId, int libroId) throws SQLException {
        synchronized (db) {
            Connection connection = db.getConnection();
            comprobarEstudiante(connection, actor, estudianteId);
            Estudiante estudiante = usuarios.porEstudianteId(connection, estudianteId);
            if (estudiante == null) {
                throw new IllegalArgumentException("Selecciona un estudiante válido");
            }
            Libro libro = libros.porId(connection, libroId);
            if (libro.getStockDisponible() < 1) {
                throw new IllegalArgumentException("No quedan ejemplares disponibles");
            }
            if (prestamos.tieneActivo(connection, estudianteId, libroId)) {
                throw new IllegalArgumentException("El estudiante ya tiene un préstamo activo de este libro");
            }
            String fecha = hoy();
            String vencimiento = Fecha.sumarDias(fecha, estudiante.diasPrestamo());
            Prestamo prestamo = new Prestamo(0, estudianteId, "", libroId, "", fecha, vencimiento, null, false);
            int id = prestamos.crear(connection, prestamo);
            try {
                libros.descontar(connection, libroId);
            } catch (SQLException e) {
                // Si no se pudo actualizar el stock, se retira el registro recién creado.
                prestamos.eliminar(connection, id);
                throw e;
            }
            // El préstamo persistido queda en el historial del estudiante.
            return id;
        }
    }

    public synchronized void devolver(Usuario actor, int prestamoId) throws SQLException {
        synchronized (db) {
            Connection connection = db.getConnection();
            Prestamo prestamo = prestamos.porId(connection, prestamoId);
            comprobarEstudiante(connection, actor, prestamo.getEstudianteId());
            if (!prestamo.activo()) {
                throw new IllegalArgumentException("El préstamo ya fue devuelto");
            }
            String fecha = hoy();
            Prestamo devuelto = new Prestamo(prestamo.getId(), prestamo.getEstudianteId(),
                    prestamo.getEstudiante(), prestamo.getLibroId(), prestamo.getLibro(),
                    prestamo.getFechaPrestamo(), prestamo.getVencimiento(), fecha,
                    Fecha.posterior(fecha, prestamo.getVencimiento()));
            prestamos.actualizar(connection, devuelto);
            try {
                libros.reponer(connection, prestamo.getLibroId());
            } catch (SQLException e) {
                prestamos.actualizar(connection, prestamo);
                throw e;
            }
        }
    }

    public List<Object[]> reporte(Usuario actor, String tipo, int estudianteId) throws SQLException {
        synchronized (db) {
            Connection connection = db.getConnection();
            Usuario actual = validar(connection, actor);
            switch (tipo) {
                case "Más prestados":
                    return reportes.masPrestados(connection);
                case "Historial por estudiante":
                    comprobarEstudiante(connection, actor, estudianteId);
                    return reportes.historial(connection, estudianteId);
                case "Préstamos activos":
                    int filtro = actual.puedeAdministrar() ? 0 : ((Estudiante) actual).getEstudianteId();
                    return reportes.activos(connection, filtro);
                default:
                    throw new IllegalArgumentException("Reporte inválido");
            }
        }
    }
}
