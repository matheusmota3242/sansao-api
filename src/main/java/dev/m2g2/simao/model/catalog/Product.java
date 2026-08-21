package dev.m2g2.simao.model.catalog;

import dev.m2g2.simao.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    // Cover photo, kept in sync with photos[0] for compatibility.
    private String foto;
    private String origem;
    private String impressora;
    private String filamento;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<ProductPhoto> photos = new ArrayList<>();

    // --- storefront / SEO ---
    private String slug;
    private Integer prazo;          // dias
    private Integer ordem;          // ordenação na loja
    private String material;
    @Column(name = "dim_peca")
    private String dimPeca;
    @Column(name = "emb_peso")
    private BigDecimal embPeso;
    @Column(name = "emb_dim")
    private String embDim;
    private boolean publicado = true;
    private boolean destaque = false;
    @Column(name = "desc_longa")
    private String descLonga;
    @Column(name = "meta_desc")
    private String metaDesc;
    private String licenca;

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

    public List<ProductPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<ProductPhoto> photos) {
        this.photos = photos;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Integer getPrazo() {
        return prazo;
    }

    public void setPrazo(Integer prazo) {
        this.prazo = prazo;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getDimPeca() {
        return dimPeca;
    }

    public void setDimPeca(String dimPeca) {
        this.dimPeca = dimPeca;
    }

    public BigDecimal getEmbPeso() {
        return embPeso;
    }

    public void setEmbPeso(BigDecimal embPeso) {
        this.embPeso = embPeso;
    }

    public String getEmbDim() {
        return embDim;
    }

    public void setEmbDim(String embDim) {
        this.embDim = embDim;
    }

    public boolean isPublicado() {
        return publicado;
    }

    public void setPublicado(boolean publicado) {
        this.publicado = publicado;
    }

    public boolean isDestaque() {
        return destaque;
    }

    public void setDestaque(boolean destaque) {
        this.destaque = destaque;
    }

    public String getDescLonga() {
        return descLonga;
    }

    public void setDescLonga(String descLonga) {
        this.descLonga = descLonga;
    }

    public String getMetaDesc() {
        return metaDesc;
    }

    public void setMetaDesc(String metaDesc) {
        this.metaDesc = metaDesc;
    }

    public String getLicenca() {
        return licenca;
    }

    public void setLicenca(String licenca) {
        this.licenca = licenca;
    }
}
