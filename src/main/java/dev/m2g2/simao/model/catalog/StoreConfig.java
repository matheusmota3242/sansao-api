package dev.m2g2.simao.model.catalog;

import dev.m2g2.simao.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Public storefront settings — a single row. The repeated content blocks
 * (trust badges, process steps, faq) are free-form lists, so they are JSON.
 */
@Entity
public class StoreConfig extends BaseModel {

    private String instagram;
    private String whatsapp;

    @Column(name = "frete_gratis")
    private BigDecimal freteGratis;

    @Column(name = "hero_titulo")
    private String heroTitulo;

    @Column(name = "hero_texto")
    private String heroTexto;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, String>> confianca = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, String>> processo = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, String>> faq = new ArrayList<>();

    private String rodape;

    @Column(name = "obs_pedido")
    private String obsPedido;

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public BigDecimal getFreteGratis() {
        return freteGratis;
    }

    public void setFreteGratis(BigDecimal freteGratis) {
        this.freteGratis = freteGratis;
    }

    public String getHeroTitulo() {
        return heroTitulo;
    }

    public void setHeroTitulo(String heroTitulo) {
        this.heroTitulo = heroTitulo;
    }

    public String getHeroTexto() {
        return heroTexto;
    }

    public void setHeroTexto(String heroTexto) {
        this.heroTexto = heroTexto;
    }

    public List<Map<String, String>> getConfianca() {
        return confianca;
    }

    public void setConfianca(List<Map<String, String>> confianca) {
        this.confianca = confianca;
    }

    public List<Map<String, String>> getProcesso() {
        return processo;
    }

    public void setProcesso(List<Map<String, String>> processo) {
        this.processo = processo;
    }

    public List<Map<String, String>> getFaq() {
        return faq;
    }

    public void setFaq(List<Map<String, String>> faq) {
        this.faq = faq;
    }

    public String getRodape() {
        return rodape;
    }

    public void setRodape(String rodape) {
        this.rodape = rodape;
    }

    public String getObsPedido() {
        return obsPedido;
    }

    public void setObsPedido(String obsPedido) {
        this.obsPedido = obsPedido;
    }
}
