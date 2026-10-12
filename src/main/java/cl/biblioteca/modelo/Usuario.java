package cl.biblioteca.modelo;
import cl.biblioteca.contrato.*;
/** Abstracción compartida por los dos roles. Implementa dos contratos. */
public abstract class Usuario implements Identificable, Permisos {
    private final int id;
    private final String nombre, email, contrasenia;
    protected Usuario(int id, String nombre, String email, String contrasenia) {
        this.id=id;
        this.nombre=nombre;
        this.email=email;
        this.contrasenia=contrasenia;
    }
    public int getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public String getEmail() {
        return email;
    }
    public String getContrasenia() {
        return contrasenia;
    }
    public abstract String getRol();
    public String resumen() {
        return resumen(true);
    }
    /** Sobrecarga utilizada por la tabla de usuarios y la cabecera. */
    public String resumen(boolean incluirRol) {
        return nombre+(incluirRol ? " · "+getRol() : "");
    }
    @Override public String toString() {
        return resumen();
    }
}
