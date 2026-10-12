package cl.biblioteca.modelo;
public class Prestamo {
    private final int id;
    private final int estudianteId;
    private final String estudiante;
    private final int libroId;
    private final String libro;
    private final String fechaPrestamo;
    private final String vencimiento;
    private final String devolucion;
    private final boolean atraso;
    public Prestamo(int id, int estudianteId, String estudiante, int libroId, String libro, String fechaPrestamo, String vencimiento, String devolucion, boolean atraso) {
        this.id = id;
        this.estudianteId = estudianteId;
        this.estudiante = estudiante;
        this.libroId = libroId;
        this.libro = libro;
        this.fechaPrestamo = fechaPrestamo;
        this.vencimiento = vencimiento;
        this.devolucion = devolucion;
        this.atraso = atraso;
    }
    public int getId() {
        return id;
    }
    public int getEstudianteId() {
        return estudianteId;
    }
    public String getEstudiante() {
        return estudiante;
    }
    public int getLibroId() {
        return libroId;
    }
    public String getLibro() {
        return libro;
    }
    public String getFechaPrestamo() {
        return fechaPrestamo;
    }
    public String getVencimiento() {
        return vencimiento;
    }
    public String getDevolucion() {
        return devolucion;
    }
    public boolean isAtraso() {
        return atraso;
    }
    public boolean activo() {
        return devolucion == null;
    }
    public boolean vencido(String hoy) {
        return activo() && Fecha.posterior(hoy, vencimiento);
    }
    public String estado(String hoy) {
        if (activo()) {
            return vencido(hoy) ? "VENCIDO" : "ACTIVO";
        }
        return atraso ? "DEVUELTO CON ATRASO" : "DEVUELTO";
    }
}
