package Modelo;

/**
 * Modelo que representa un recordatorio con fecha, hora y prioridad.
 */
public class Recordatorio {
    private int id;
    private String titulo;
    private String descripcion;
    private String fecha; // YYYY-MM-DD
    private String hora;  // HH:mm
    private String prioridad; // ALTA, MEDIA, BAJA
    private boolean completado;

    public Recordatorio() {
    }

    public Recordatorio(int id, String titulo, String descripcion, String fecha, String hora, String prioridad, boolean completado) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.hora = hora;
        this.prioridad = prioridad;
        this.completado = completado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }
}
