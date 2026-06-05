package com.trainup.model;

import com.trainup.enums.EstadoAsistencia;
import com.trainup.enums.ResultadoCurso;
import java.time.LocalDateTime;

public class RegistroAsistencia {
    private int id;
    private int idInstancia;
    private int idEmpleado;
    private String nombreEmpleado;    // campo auxiliar para vistas
    private String nombreCurso;       // campo auxiliar para vistas
    private EstadoAsistencia estadoAsistencia;
    private ResultadoCurso resultado;
    private String observaciones;
    private LocalDateTime fechaRegistro;
    private LocalDateTime updatedAt;

    public RegistroAsistencia() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdInstancia() { return idInstancia; }
    public void setIdInstancia(int idInstancia) { this.idInstancia = idInstancia; }
    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }
    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }
    public String getNombreCurso() { return nombreCurso; }
    public void setNombreCurso(String nombreCurso) { this.nombreCurso = nombreCurso; }
    public EstadoAsistencia getEstadoAsistencia() { return estadoAsistencia; }
    public void setEstadoAsistencia(EstadoAsistencia estadoAsistencia) { this.estadoAsistencia = estadoAsistencia; }
    public ResultadoCurso getResultado() { return resultado; }
    public void setResultado(ResultadoCurso resultado) { this.resultado = resultado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
