package cl.biblioteca.modelo;
public final class Estudiante extends Usuario {
    private final String rut, curso;
    private final int estudianteId;
    public Estudiante(int id, String nombre, String email, String contrasenia, String rut, String curso) {
        this(id, 0, nombre, email, contrasenia, rut, curso);
    }
    public Estudiante(int usuarioId, int estudianteId, String nombre, String email, String contrasenia, String rut, String curso) {
        super(usuarioId, nombre, email, contrasenia);
        this.estudianteId=estudianteId;
        this.rut=rut;
        this.curso=curso;
    }
    /** El identificador del perfil oficial es independiente del de la cuenta. */
    public int getEstudianteId() {
        return estudianteId;
    }
    public String getRut() {
        return rut;
    }
    public String getCurso() {
        return curso;
    }
    @Override public String getRol() {
        return "ESTUDIANTE";
    }
    @Override public boolean puedeAdministrar() {
        return false;
    }
    @Override public int diasPrestamo() {
        return 14;
    }
    @Override public String resumen(boolean incluirRol) {
        return super.resumen(incluirRol)+" · "+curso;
    }
}
