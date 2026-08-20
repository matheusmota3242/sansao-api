package dev.m2g2.simao.model.catalog;

import dev.m2g2.simao.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

@Entity
public class Product extends BaseModel {

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    // Sequential number within the category; part of the derived SKU.
    private Integer num;
    // Size/variation suffix (may be empty, never null).
    private String tam = "";

    private String nome;

    @Column(name = "descricao")
    private String descricao;

    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.ATIVO;

    private String obs;

    private BigDecimal gram;

    @Column(name = "tempo_horas")
    private BigDecimal tempoHoras;

    @Column(name = "trab_min")
    private BigDecimal trabMin;

    private BigDecimal insumos;

    private BigDecimal embalagem;

    @Column(name = "catalogo_preco")
    private BigDecimal catalogoPreco;

    @Column(name = "tempo_exato")
    private boolean tempoExato = true;

    private String foto;
    private String origem;
    private String impressora;
    private String filamento;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getTam() {
        return tam;
    }

    public void setTam(String tam) {
        this.tam = tam == null ? "" : tam;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public String getObs() {
        return obs;
    }

    public void setObs(String obs) {
        this.obs = obs;
    }

    public BigDecimal getGram() {
        return gram;
    }

    public void setGram(BigDecimal gram) {
        this.gram = gram;
    }

    public BigDecimal getTempoHoras() {
        return tempoHoras;
    }

    public void setTempoHoras(BigDecimal tempoHoras) {
        this.tempoHoras = tempoHoras;
    }

    public BigDecimal getTrabMin() {
        return trabMin;
    }

    public void setTrabMin(BigDecimal trabMin) {
        this.trabMin = trabMin;
    }

    public BigDecimal getInsumos() {
        return insumos;
    }

    public void setInsumos(BigDecimal insumos) {
        this.insumos = insumos;
    }

    public BigDecimal getEmbalagem() {
        return embalagem;
    }

    public void setEmbalagem(BigDecimal embalagem) {
        this.embalagem = embalagem;
    }

    public BigDecimal getCatalogoPreco() {
        return catalogoPreco;
    }

    public void setCatalogoPreco(BigDecimal catalogoPreco) {
        this.catalogoPreco = catalogoPreco;
    }

    public boolean isTempoExato() {
        return tempoExato;
    }

    public void setTempoExato(boolean tempoExato) {
        this.tempoExato = tempoExato;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getImpressora() {
        return impressora;
    }

    public void setImpressora(String impressora) {
        this.impressora = impressora;
    }

    public String getFilamento() {
        return filamento;
    }

    public void setFilamento(String filamento) {
        this.filamento = filamento;
    }
}
