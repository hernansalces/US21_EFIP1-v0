package com.trainup.model;

import com.trainup.enums.CategoriaEngagement;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HistorialEngagement {
    private int id;
    private int idEmpleado;
    private BigDecimal valorIe;
    private BigDecimal pa;
    private BigDecimal pr;
    private BigDecimal pp;
    private CategoriaEngagement categoria;
    private LocalDateTime fechaCalculo;

    public HistorialEngagement() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
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
    public LocalDateTime getFechaCalculo() { return fechaCalculo; }
    public void setFechaCalculo(LocalDateTime fechaCalculo) { this.fechaCalculo = fechaCalculo; }
}
