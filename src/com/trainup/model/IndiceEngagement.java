package com.trainup.model;

import com.trainup.enums.CategoriaEngagement;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class IndiceEngagement {
    private int idEmpleado;
    private BigDecimal valorIe;
    private BigDecimal pa;
    private BigDecimal pr;
    private BigDecimal pp;
    private CategoriaEngagement categoria;
    private boolean esProvisional;
    private LocalDateTime fechaCalculo;
    private LocalDateTime updatedAt;

    public IndiceEngagement() {}

    public int getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }
    public BigDecimal getValorIe() { return valorIe; }
    public void setValorIe(BigDecimal valorIe) { this.valorIe = valorIe; }
    public BigDecimal getPa() { return pa; }
    public void setPa(BigDecimal pa) { this.pa = pa; }
    public BigDecimal getPr() { return pr; }
    public void setPr(BigDecimal pr) { this.pr = pr; }
    public BigDecimal getPp() { return pp; }
    public void setPp(BigDecimal pp) { this.pp = pp; }
    public CategoriaEngagement getCategoria() { return categoria; }
    public void setCategoria(CategoriaEngagement categoria) { this.categoria = categoria; }
    public boolean isEsProvisional() { return esProvisional; }
    public void setEsProvisional(boolean esProvisional) { this.esProvisional = esProvisional; }
    public LocalDateTime getFechaCalculo() { return fechaCalculo; }
    public void setFechaCalculo(LocalDateTime fechaCalculo) { this.fechaCalculo = fechaCalculo; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
