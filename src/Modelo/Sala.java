package Modelo;

/**
 * Modelo que representa una sala o ambiente del restaurante.
 */
public class Sala {
    private int id;
    private String nombre;
    private int mesas;

    public Sala() {
    }

    public Sala(int id, String nombre, int mesas) {
        this.id = id;
        this.nombre = nombre;
        this.mesas = mesas;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getMesas() {
        return mesas;
    }

    public void setMesas(int mesas) {
        this.mesas = mesas;
    }
}
