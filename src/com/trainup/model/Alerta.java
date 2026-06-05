package com.trainup.model;

import com.trainup.enums.OrigenAlerta;
import com.trainup.enums.TipoAlerta;
import java.time.LocalDateTime;

public class Alerta {
    private int id;
    private int idEmpleado;
    private String nombreEmpleado;    // campo auxiliar para vistas
    private TipoAlerta tipo;
    private OrigenAlerta origen;
    private String mensaje;
    private boolean leidaEmpleado;
    private boolean leidaAdmin;
    private LocalDateTime fechaGeneracion;
    private LocalDateTime fechaLecturaEmpleado;
    private LocalDateTime fechaLecturaAdmin;

    public Alerta() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }
    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }
    public TipoAlerta getTipo() { return tipo; }
    public void setTipo(TipoAlerta tipo) { this.tipo = tipo; }
    public OrigenAlerta getOrigen() { return origen; }
    public void setOrigen(OrigenAlerta origen) { this.origen = origen; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public boolean isLeidaEmpleado() { return leidaEmpleado; }
    public void setLeidaEmpleado(boolean leidaEmpleado) { this.leidaEmpleado = leidaEmpleado; }
    public boolean isLeidaAdmin() { return leidaAdmin; }
    public void setLeidaAdmin(boolean leidaAdmin) { this.leidaAdmin = leidaAdmin; }
    public LocalDateTime getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDateTime fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }
    public LocalDateTime getFechaLecturaEmpleado() { return fechaLecturaEmpleado; }
    public void setFechaLecturaEmpleado(LocalDateTime fechaLecturaEmpleado) { this.fechaLecturaEmpleado = fechaLecturaEmpleado; }
    public LocalDateTime getFechaLecturaAdmin() { return fechaLecturaAdmin; }
    public void setFechaLecturaAdmin(LocalDateTime fechaLecturaAdmin) { this.fechaLecturaAdmin = fechaLecturaAdmin; }
}
