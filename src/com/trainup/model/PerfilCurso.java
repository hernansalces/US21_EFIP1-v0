package com.trainup.model;

import com.trainup.enums.NivelExpertise;

public class PerfilCurso {
    private int idPerfil;
    private int idCurso;
    private String nombreCurso;    // campo auxiliar para vistas
    private boolean esObligatorio;
    private NivelExpertise nivelExpertise;
    private Integer orden;

    public PerfilCurso() {}

    public int getIdPerfil() { return idPerfil; }
    public void setIdPerfil(int idPerfil) { this.idPerfil = idPerfil; }
    public int getIdCurso() { return idCurso; }
    public void setIdCurso(int idCurso) { this.idCurso = idCurso; }
    public String getNombreCurso() { return nombreCurso; }
    public void setNombreCurso(String nombreCurso) { this.nombreCurso = nombreCurso; }
    public boolean isEsObligatorio() { return esObligatorio; }
    public void setEsObligatorio(boolean esObligatorio) { this.esObligatorio = esObligatorio; }
    public NivelExpertise getNivelExpertise() { return nivelExpertise; }
    public void setNivelExpertise(NivelExpertise nivelExpertise) { this.nivelExpertise = nivelExpertise; }
    public Integer getOrden() { return orden; }
    public void setOrden(Integer orden) { this.orden = orden; }
}
