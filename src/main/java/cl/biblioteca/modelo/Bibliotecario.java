package cl.biblioteca.modelo;
public final class Bibliotecario extends Usuario {
    public Bibliotecario(int id, String nombre, String email, String contrasenia) {
        super(id, nombre, email, contrasenia);
    }
    @Override public String getRol() {
        return "BIBLIOTECARIO";
    }
    @Override public boolean puedeAdministrar() {
        return true;
    }
    @Override public int diasPrestamo() {
        return 14;
    }
}
