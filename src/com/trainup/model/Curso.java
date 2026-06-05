package com.trainup.model;

import com.trainup.enums.ModalidadCurso;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Curso {
    private int id;
    private String nombre;
    private String descripcion;
    private BigDecimal duracionHoras;
    private ModalidadCurso modalidad;
    private String categoriaTematica;
    private Integer idCapacitador;
    private String nombreCapacitador;  // campo auxiliar para vistas
    private boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Curso() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getDuracionHoras() { return duracionHoras; }
    public void setDuracionHoras(BigDecimal duracionHoras) { this.duracionHoras = duracionHoras; }
    public ModalidadCurso getModalidad() { return modalidad; }
    public void setModalidad(ModalidadCurso modalidad) { this.modalidad = modalidad; }
    public String getCategoriaTematica() { return categoriaTematica; }
    public void setCategoriaTematica(String categoriaTematica) { this.categoriaTematica = categoriaTematica; }
    public Integer getIdCapacitador() { return idCapacitador; }
    public void setIdCapacitador(Integer idCapacitador) { this.idCapacitador = idCapacitador; }
    public String getNombreCapacitador() { return nombreCapacitador; }
    public void setNombreCapacitador(String nombreCapacitador) { this.nombreCapacitador = nombreCapacitador; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() { return nombre; }
}
