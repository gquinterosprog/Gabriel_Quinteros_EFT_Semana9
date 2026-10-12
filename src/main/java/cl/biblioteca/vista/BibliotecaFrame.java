package cl.biblioteca.vista;

import cl.biblioteca.modelo.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BibliotecaFrame extends JFrame {
    public final Usuario usuario;
    public final JButton salir = new JButton("Cerrar sesión");
    public final JButton actualizarLibros = new JButton("Actualizar");
    public final JButton prestar = new JButton("Prestar seleccionado");
    public final JButton crearLibro = new JButton("Nuevo libro");
    public final JButton editarLibro = new JButton("Editar");
    public final JButton eliminarLibro = new JButton("Eliminar");
    public final JButton actualizarPrestamos = new JButton("Actualizar");
    public final JButton devolver = new JButton("Registrar devolución");
    public final JButton verReporte = new JButton("Ver reporte");
    public final JButton crearEstudiante = new JButton("Nuevo estudiante");
    public final JButton editarEstudiante = new JButton("Editar");
    public final JButton eliminarEstudiante = new JButton("Eliminar");
    public final JButton actualizarEstudiantes = new JButton("Actualizar");
    public final JButton crearCategoria = new JButton("Nueva categoría");
    public final JButton editarCategoria = new JButton("Editar");
    public final JButton eliminarCategoria = new JButton("Eliminar");
    public final JButton actualizarCategorias = new JButton("Actualizar");
    public final JButton crearUsuario = new JButton("Nuevo bibliotecario");
    public final JButton editarUsuario = new JButton("Editar");
    public final JButton eliminarUsuario = new JButton("Eliminar");
    public final JButton actualizarUsuarios = new JButton("Actualizar");
    public final JTable libros = tabla(), estudiantes = tabla(), categorias = tabla();
    public final JTable usuarios = tabla(), prestamos = tabla(), reportes = tabla();
    public final JComboBox<String> tipoReporte = new JComboBox<>(new String[]{
            "Más prestados", "Historial por estudiante", "Préstamos activos"});
    public final JComboBox<Estudiante> estudianteReporte = new JComboBox<>();
    public final JLabel estado = new JLabel("Listo");
    public List<Libro> listaLibros = new ArrayList<>();
    public List<Estudiante> listaEstudiantes = new ArrayList<>();
    public List<Categoria> listaCategorias = new ArrayList<>();
    public List<Usuario> listaUsuarios = new ArrayList<>();
    public List<Prestamo> listaPrestamos = new ArrayList<>();
    private final JPanel contenido = new JPanel(new BorderLayout());
    private final JPanel catalogo, paginaPrestamos, paginaReportes;
    private final JPanel paginaEstudiantes, paginaCategorias, paginaUsuarios;

    public BibliotecaFrame(Usuario usuario) {
        super("Biblioteca Escolar · " + usuario.getNombre());
        this.usuario = usuario;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1180, 740);
        setMinimumSize(new Dimension(1040, 650));
        setLocationRelativeTo(null);
        JPanel accionesLibros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        accionesLibros.add(actualizarLibros);
        accionesLibros.add(prestar);
        if (usuario.puedeAdministrar()) {
            accionesLibros.add(crearLibro);
            accionesLibros.add(editarLibro);
            accionesLibros.add(eliminarLibro);
        }
        catalogo = pagina("Catálogo de libros", libros, accionesLibros);
        JPanel accionesPrestamos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        accionesPrestamos.add(actualizarPrestamos);
        accionesPrestamos.add(devolver);
        paginaPrestamos = pagina("Préstamos y devoluciones", prestamos, accionesPrestamos);
        JPanel opciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        opciones.add(tipoReporte);
        if (usuario.puedeAdministrar()) {
            opciones.add(new JLabel("Estudiante"));
            opciones.add(estudianteReporte);
        }
        opciones.add(verReporte);
        paginaReportes = pagina("Reportes de la biblioteca", reportes, opciones);
        paginaEstudiantes = pagina("Estudiantes", estudiantes,
                acciones(crearEstudiante, editarEstudiante, eliminarEstudiante, actualizarEstudiantes));
        paginaCategorias = pagina("Categorías", categorias,
                acciones(crearCategoria, editarCategoria, eliminarCategoria, actualizarCategorias));
        paginaUsuarios = pagina("Bibliotecarios", usuarios,
                acciones(crearUsuario, editarUsuario, eliminarUsuario, actualizarUsuarios));

        JButton catalogoMenu = new JButton("Catálogo");
        JButton prestamosMenu = new JButton("Préstamos");
        JButton reportesMenu = new JButton("Reportes");
        JButton estudiantesMenu = new JButton("Estudiantes");
        JButton categoriasMenu = new JButton("Categorías");
        JButton usuariosMenu = new JButton("Usuarios");
        catalogoMenu.addActionListener(event -> mostrarPagina("Catálogo"));
        prestamosMenu.addActionListener(event -> mostrarPagina("Préstamos"));
        reportesMenu.addActionListener(event -> mostrarPagina("Reportes"));
        estudiantesMenu.addActionListener(event -> mostrarPagina("Estudiantes"));
        categoriasMenu.addActionListener(event -> mostrarPagina("Categorías"));
        usuariosMenu.addActionListener(event -> mostrarPagina("Usuarios"));
        JPanel menu = new JPanel(new GridLayout(0, 1, 0, 10));
        menu.add(catalogoMenu);
        menu.add(prestamosMenu);
        menu.add(reportesMenu);
        if (usuario.puedeAdministrar()) {
            menu.add(estudiantesMenu);
            menu.add(categoriasMenu);
            menu.add(usuariosMenu);
        }
        JPanel lateral = new JPanel(new BorderLayout());
        lateral.add(menu, BorderLayout.NORTH);
        lateral.setPreferredSize(new Dimension(170, 500));
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.add(new JLabel("Biblioteca Escolar · " + usuario.resumen()), BorderLayout.CENTER);
        cabecera.add(salir, BorderLayout.EAST);
        JPanel base = new JPanel(new BorderLayout(18, 18));
        base.setBorder(BorderFactory.createEmptyBorder(18, 18, 14, 18));
        base.add(cabecera, BorderLayout.NORTH);
        base.add(lateral, BorderLayout.WEST);
        base.add(contenido, BorderLayout.CENTER);
        base.add(estado, BorderLayout.SOUTH);
        setContentPane(base);
        mostrarPagina("Catálogo");
    }

    private JPanel acciones(JButton nuevo, JButton editar, JButton eliminar, JButton actualizar) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(nuevo);
        panel.add(editar);
        panel.add(eliminar);
        panel.add(actualizar);
        return panel;
    }

    private JPanel pagina(String titulo, JTable tabla, JPanel acciones) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.add(new JLabel(titulo), BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);
        panel.add(acciones, BorderLayout.SOUTH);
        return panel;
    }

    private static JTable tabla() {
        DefaultTableModel modelo = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        return tabla;
    }

    public static int selected(JTable tabla) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) throw new IllegalArgumentException("Selecciona un registro en la tabla");
        return fila;
    }

    public void busy(boolean ocupado, String mensaje) {
        JButton[] botones = {salir, actualizarLibros, prestar, crearLibro, editarLibro, eliminarLibro,
                actualizarPrestamos, devolver, verReporte, crearEstudiante, editarEstudiante,
                eliminarEstudiante, actualizarEstudiantes, crearCategoria, editarCategoria,
                eliminarCategoria, actualizarCategorias, crearUsuario, editarUsuario,
                eliminarUsuario, actualizarUsuarios};
        for (JButton boton : botones) boton.setEnabled(!ocupado);
        estado.setText(mensaje);
    }

    public void showState(List<Libro> ls, List<Estudiante> es, List<Categoria> cs,
                          List<Usuario> us, List<Prestamo> ps, String hoy) {
        listaLibros = ls;
        listaEstudiantes = es;
        listaCategorias = cs;
        listaUsuarios = us;
        listaPrestamos = ps;
        List<Object[]> filas = new ArrayList<>();
        for (Libro libro : ls) filas.add(new Object[]{libro.getId(), libro.getTitulo(), libro.getAutor(),
                libro.getEditorial(), libro.getCategoria(), libro.getIsbn(), libro.getStockTotal(), libro.getStockDisponible()});
        fill(libros, new String[]{"ID", "Título", "Autor", "Editorial", "Categoría", "ISBN", "Total", "Disponibles"}, filas);
        filas = new ArrayList<>();
        for (Estudiante estudiante : es) filas.add(new Object[]{estudiante.getEstudianteId(), estudiante.getNombre(),
                estudiante.getEmail(), estudiante.getRut(), estudiante.getCurso()});
        fill(estudiantes, new String[]{"ID", "Nombre", "Correo", "RUT", "Curso"}, filas);
        filas = new ArrayList<>();
        for (Categoria categoria : cs) filas.add(new Object[]{categoria.getId(), categoria.getNombre()});
        fill(categorias, new String[]{"ID", "Nombre"}, filas);
        filas = new ArrayList<>();
        for (Usuario cuenta : us) filas.add(new Object[]{cuenta.getId(), cuenta.resumen(false), cuenta.getEmail(), cuenta.getRol()});
        fill(usuarios, new String[]{"ID", "Nombre", "Correo", "Rol"}, filas);
        filas = new ArrayList<>();
        for (Prestamo prestamo : ps) filas.add(new Object[]{prestamo.getId(), prestamo.getEstudiante(), prestamo.getLibro(),
                prestamo.getFechaPrestamo(), prestamo.getVencimiento(), prestamo.getDevolucion(), prestamo.estado(hoy)});
        fill(prestamos, new String[]{"ID", "Estudiante", "Libro", "Préstamo", "Vencimiento", "Devolución", "Estado"}, filas);
        Estudiante anterior = (Estudiante) estudianteReporte.getSelectedItem();
        estudianteReporte.removeAllItems();
        for (Estudiante estudiante : es) estudianteReporte.addItem(estudiante);
        if (anterior != null) {
            for (int i = 0; i < es.size(); i++) {
                if (es.get(i).getEstudianteId() == anterior.getEstudianteId()) estudianteReporte.setSelectedIndex(i);
            }
        }
        libros.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchos = {42, 240, 145, 110, 100, 145, 55, 82};
        for (int i = 0; i < anchos.length; i++) libros.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
    }

    public static void fill(JTable tabla, String[] columnas, List<Object[]> filas) {
        DefaultTableModel modelo = (DefaultTableModel) tabla.getModel();
        modelo.setColumnIdentifiers(columnas);
        modelo.setRowCount(0);
        for (Object[] fila : filas) modelo.addRow(fila);
        if (columnas.length == 7) {
            tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            int[] anchos = {42, 135, 235, 100, 100, 100, 160};
            for (int i = 0; i < anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
    }

    public void mostrarPagina(String pagina) {
        JPanel seleccionada;
        switch (pagina) {
            case "Préstamos": seleccionada = paginaPrestamos; break;
            case "Reportes": seleccionada = paginaReportes; break;
            case "Estudiantes": seleccionada = paginaEstudiantes; break;
            case "Categorías": seleccionada = paginaCategorias; break;
            case "Usuarios": seleccionada = paginaUsuarios; break;
            default: seleccionada = catalogo;
        }
        if (!usuario.puedeAdministrar() && (pagina.equals("Estudiantes")
                || pagina.equals("Categorías") || pagina.equals("Usuarios"))) {
            throw new IllegalArgumentException("Esta página requiere un bibliotecario");
        }
        contenido.removeAll();
        contenido.add(seleccionada, BorderLayout.CENTER);
        contenido.revalidate();
        contenido.repaint();
    }
}
