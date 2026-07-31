package Modelo;

public class GastoCaja {
    private int id;
    private int idCierre;
    private double monto;
    private String descripcion;
    private String categoria;
    private String usuario;
    private String fecha;

    public GastoCaja() {}

    public GastoCaja(int id, int idCierre, double monto, String descripcion, String categoria, String usuario, String fecha) {
        this.id = id;
        this.idCierre = idCierre;
        this.monto = monto;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.usuario = usuario;
        this.fecha = fecha;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdCierre() { return idCierre; }
    public void setIdCierre(int idCierre) { this.idCierre = idCierre; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}
