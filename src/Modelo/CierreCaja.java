package Modelo;

public class CierreCaja {
    private int id;
    private String usuario;
    private double montoInicial;
    private String fechaApertura;
    private double montoVentasEfectivo;
    private double montoVentasTransaccion;
    private double montoGastos;
    private double montoEsperadoEfectivo;
    private double montoRealEfectivo;
    private double diferencia;
    private String estado; // 'ABIERTA' o 'CERRADA'
    private String fechaCierre;
    private String observaciones;

    public CierreCaja() {}

    public CierreCaja(int id, String usuario, double montoInicial, String fechaApertura,
                      double montoVentasEfectivo, double montoVentasTransaccion, double montoGastos,
                      double montoEsperadoEfectivo, double montoRealEfectivo, double diferencia,
                      String estado, String fechaCierre, String observaciones) {
        this.id = id;
        this.usuario = usuario;
        this.montoInicial = montoInicial;
        this.fechaApertura = fechaApertura;
        this.montoVentasEfectivo = montoVentasEfectivo;
        this.montoVentasTransaccion = montoVentasTransaccion;
        this.montoGastos = montoGastos;
        this.montoEsperadoEfectivo = montoEsperadoEfectivo;
        this.montoRealEfectivo = montoRealEfectivo;
        this.diferencia = diferencia;
        this.estado = estado;
        this.fechaCierre = fechaCierre;
        this.observaciones = observaciones;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public double getMontoInicial() { return montoInicial; }
    public void setMontoInicial(double montoInicial) { this.montoInicial = montoInicial; }

    public String getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(String fechaApertura) { this.fechaApertura = fechaApertura; }

    public double getMontoVentasEfectivo() { return montoVentasEfectivo; }
    public void setMontoVentasEfectivo(double montoVentasEfectivo) { this.montoVentasEfectivo = montoVentasEfectivo; }

    public double getMontoVentasTransaccion() { return montoVentasTransaccion; }
    public void setMontoVentasTransaccion(double montoVentasTransaccion) { this.montoVentasTransaccion = montoVentasTransaccion; }

    public double getMontoGastos() { return montoGastos; }
    public void setMontoGastos(double montoGastos) { this.montoGastos = montoGastos; }

    public double getMontoEsperadoEfectivo() { return montoEsperadoEfectivo; }
    public void setMontoEsperadoEfectivo(double montoEsperadoEfectivo) { this.montoEsperadoEfectivo = montoEsperadoEfectivo; }

    public double getMontoRealEfectivo() { return montoRealEfectivo; }
    public void setMontoRealEfectivo(double montoRealEfectivo) { this.montoRealEfectivo = montoRealEfectivo; }

    public double getDiferencia() { return diferencia; }
    public void setDiferencia(double diferencia) { this.diferencia = diferencia; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(String fechaCierre) { this.fechaCierre = fechaCierre; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
