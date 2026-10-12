package cl.biblioteca.modelo;
public class Libro {
    private final int id;
    private final String titulo;
    private final String autor;
    private final String editorial;
    private final String isbn;
    private final int categoriaId;
    private final String categoria;
    private final int stockTotal;
    private final int stockDisponible;
    public Libro(int id, String titulo, String autor, String editorial, String isbn, int categoriaId, String categoria, int stockTotal, int stockDisponible) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.isbn = isbn;
        this.categoriaId = categoriaId;
        this.categoria = categoria;
        this.stockTotal = stockTotal;
        this.stockDisponible = stockDisponible;
    }
    public int getId() {
        return id;
    }
    public String getTitulo() {
        return titulo;
    }
    public String getAutor() {
        return autor;
    }
    public String getEditorial() {
        return editorial;
    }
    public String getIsbn() {
        return isbn;
    }
    public int getCategoriaId() {
        return categoriaId;
    }
    public String getCategoria() {
        return categoria;
    }
    public int getStockTotal() {
        return stockTotal;
    }
    public int getStockDisponible() {
        return stockDisponible;
    }
    @Override public String toString() {
        return titulo + " (disponibles: " + stockDisponible + ")";
    }
}
