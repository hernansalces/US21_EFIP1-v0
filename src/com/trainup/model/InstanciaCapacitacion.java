package com.trainup.model;

import com.trainup.enums.EstadoInstancia;
import com.trainup.enums.ModalidadCurso;
import java.time.LocalDateTime;

public class InstanciaCapacitacion {
    private int id;
    private int idCurso;
    private String nombreCurso;    // campo auxiliar para vistas
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private ModalidadCurso modalidad;
    private String lugarUrl;
    private EstadoInstancia estado;
    private String motivoCancelacion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // Campo calculado (no persiste en BD): indica si se puede modificar la asistencia
    private boolean editableAsistencia = true;

    public InstanciaCapacitacion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdCurso() { return idCurso; }
    public void setIdCurso(int idCurso) { this.idCurso = idCurso; }
    public String getNombreCurso() { return nombreCurso; }
    public void setNombreCurso(String nombreCurso) { this.nombreCurso = nombreCurso; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public ModalidadCurso getModalidad() { return modalidad; }
    public void setModalidad(ModalidadCurso modalidad) { this.modalidad = modalidad; }
    public String getLugarUrl() { return lugarUrl; }
    public void setLugarUrl(String lugarUrl) { this.lugarUrl = lugarUrl; }
    public EstadoInstancia getEstado() { return estado; }
    public void setEstado(EstadoInstancia estado) { this.estado = estado; }
    public String getMotivoCancelacion() { return motivoCancelacion; }
    public void setMotivoCancelacion(String motivoCancelacion) { this.motivoCancelacion = motivoCancelacion; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public boolean isEditableAsistencia() { return editableAsistencia; }
    public void setEditableAsistencia(boolean editableAsistencia) { this.editableAsistencia = editableAsistencia; }
}
