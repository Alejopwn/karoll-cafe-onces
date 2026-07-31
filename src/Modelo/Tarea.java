package Modelo;

/**
 * Modelo para tareas asignadas a empleados con repetición (DIARIA, SEMANAL, MENSUAL).
 */
public class Tarea {
    private int id;
    private String titulo;
    private String descripcion;
    private String asignadoA;
    private boolean completada;
    private String repeticion; // NINGUNA, DIARIA, SEMANAL, MENSUAL
    private String fechaCreacion;
    private String fechaCompletado;

    public Tarea() {
    }

    public Tarea(int id, String titulo, String descripcion, String asignadoA, boolean completada, String repeticion, String fechaCreacion, String fechaCompletado) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.asignadoA = asignadoA;
        this.completada = completada;
        this.repeticion = repeticion;
        this.fechaCreacion = fechaCreacion;
        this.fechaCompletado = fechaCompletado;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getAsignadoA() { return asignadoA; }
    public void setAsignadoA(String asignadoA) { this.asignadoA = asignadoA; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }

    public String getRepeticion() { return repeticion; }
    public void setRepeticion(String repeticion) { this.repeticion = repeticion; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getFechaCompletado() { return fechaCompletado; }
    public void setFechaCompletado(String fechaCompletado) { this.fechaCompletado = fechaCompletado; }
}
