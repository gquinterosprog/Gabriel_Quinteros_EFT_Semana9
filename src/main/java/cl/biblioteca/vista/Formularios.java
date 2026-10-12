package cl.biblioteca.vista;
import cl.biblioteca.modelo.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;
public final class Formularios {
    private Formularios() {
    }
    public static class Persona {
        private final int id;
        private final String nombre, correo, rut, curso;
        private final char[] clave;
        public Persona(int id, String nombre, String correo, char[] clave, String rut, String curso) {
            this.id = id;
            this.nombre = nombre;
            this.correo = correo;
            this.clave = clave;
            this.rut = rut;
            this.curso = curso;
        }
        public int getId() {
            return id;
        }
        public String getNombre() {
            return nombre;
        }
        public String getCorreo() {
            return correo;
        }
        public char[] getClave() {
            return clave;
        }
        public String getRut() {
            return rut;
        }
        public String getCurso() {
            return curso;
        }
    }
    private static JPanel grid() {
        return new JPanel(new GridLayout(0, 2, 12, 12));
    }
    private static void row(JPanel p, String name, JComponent input) {
        p.add(new JLabel(name));
        p.add(input);
    }
    private static boolean accept(Component owner, String title, JPanel p) {
        return JOptionPane.showConfirmDialog(owner, p, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION;
    }

    public static class Conexion {
        private final String servidor, base, usuario;
        private final int puerto;
        private final char[] clave;

        public Conexion(String servidor, int puerto, String base, String usuario, char[] clave) {
            this.servidor = servidor;
            this.puerto = puerto;
            this.base = base;
            this.usuario = usuario;
            this.clave = clave;
        }

        public String getServidor() { return servidor; }
        public int getPuerto() { return puerto; }
        public String getBase() { return base; }
        public String getUsuario() { return usuario; }
        public char[] getClave() { return clave; }
    }

    public static Conexion conexion(Component owner, String[] datos) {
        JPanel panel = grid();
        JTextField servidor = new JTextField(datos[0], 25);
        JTextField puerto = new JTextField(datos[1]);
        JTextField base = new JTextField(datos[2]);
        JTextField usuario = new JTextField(datos[3]);
        JPasswordField clave = new JPasswordField();
        row(panel, "Servidor", servidor);
        row(panel, "Puerto", puerto);
        row(panel, "Base de datos", base);
        row(panel, "Usuario MySQL", usuario);
        row(panel, "Contraseña MySQL", clave);
        if (!accept(owner, "Conexión MySQL", panel)) return null;
        int numeroPuerto;
        try {
            numeroPuerto = Integer.parseInt(puerto.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El puerto de MySQL debe ser un número entero");
        }
        return new Conexion(servidor.getText(), numeroPuerto, base.getText(), usuario.getText(), clave.getPassword());
    }

    public static Libro libro(Component owner, Libro old, List<Categoria> cats) {
        if (cats.isEmpty())throw new IllegalArgumentException("Crea una categoría antes de registrar libros");
        JPanel p=grid();
        JTextField titulo=new JTextField(old==null?"":old.getTitulo(), 28), autor=new JTextField(old==null?"":old.getAutor()), editorial=new JTextField(old==null?"":old.getEditorial()), isbn=new JTextField(old==null?"":old.getIsbn());
        JComboBox<Categoria> cat=new JComboBox<>(cats.toArray(new Categoria[0]));
        if (old!=null)for (int i=0; i<cats.size(); i++)if (cats.get(i).getId()==old.getCategoriaId())cat.setSelectedIndex(i);
        JTextField stock = new JTextField(old == null ? "1" : "" + old.getStockTotal());
        row(p, "Título", titulo);
        row(p, "Autor", autor);
        row(p, "Editorial", editorial);
        row(p, "ISBN", isbn);
        row(p, "Categoría", cat);
        row(p, "Ejemplares totales", stock);
        if (!accept(owner, old==null?"Nuevo libro":"Editar libro", p))return null;
        int cantidad;
        try {
            cantidad = Integer.parseInt(stock.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El stock debe ser un número entero");
        }
        if (cantidad < 0 || cantidad > 100000) {
            throw new IllegalArgumentException("El stock debe estar entre 0 y 100000");
        }
        Categoria c=(Categoria)cat.getSelectedItem();
        return new Libro(old==null?0:old.getId(), titulo.getText(), autor.getText(), editorial.getText(), isbn.getText().trim(), c.getId(), c.getNombre(), cantidad, 0);
    }
    public static Categoria categoria(Component owner, Categoria old) {
        String v=JOptionPane.showInputDialog(owner, "Nombre de la categoría", old==null?"":old.getNombre());
        return v==null?null:new Categoria(old==null?0:old.getId(), v);
    }
    public static Persona persona(Component owner, Usuario old, boolean estudiante) {
        JPanel p=grid();
        JTextField nombre=new JTextField(old==null?"":old.getNombre(), 27), correo=new JTextField(old==null?"":old.getEmail());
        JPasswordField clave=new JPasswordField();
        JTextField rut=new JTextField(old instanceof Estudiante ?((Estudiante)old).getRut() : ""), curso=new JTextField(old instanceof Estudiante ?((Estudiante)old).getCurso() : "");
        row(p, "Nombre", nombre);
        row(p, "Correo", correo);
        row(p, old==null?"Contraseña (mínimo 8)":"Contraseña (vacío conserva)", clave);
        if (estudiante) {
            row(p, "RUT o identificador", rut);
            row(p, "Curso", curso);
        }
        if (!accept(owner, (old==null?"Registrar ":"Editar ")+(estudiante?"estudiante":"bibliotecario"), p))return null;
        return new Persona(old==null?0:(old instanceof Estudiante ?((Estudiante)old).getEstudianteId() : old.getId()), nombre.getText(), correo.getText(), clave.getPassword(), rut.getText(), curso.getText());
    }
    public static Estudiante elegirEstudiante(Component owner, List<Estudiante> es) {
        if (es.isEmpty())throw new IllegalArgumentException("Registra un estudiante antes de prestar");
        return(Estudiante)JOptionPane.showInputDialog(owner, "Estudiante que solicita el préstamo", "Registrar préstamo", JOptionPane.PLAIN_MESSAGE, null, es.toArray(), es.get(0));
    }
    public static boolean confirmar(Component owner, String mensaje) {
        return JOptionPane.showConfirmDialog(owner, mensaje, "Confirmar", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;
    }
}
