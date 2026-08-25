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

    @Column(name = "free_shipping_from")
    private BigDecimal freeShippingFrom;

    @Column(name = "hero_title")
    private String heroTitle;

    @Column(name = "hero_text")
    private String heroText;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, String>> trustBadges = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, String>> process = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Map<String, String>> faq = new ArrayList<>();

    private String footer;

    @Column(name = "order_notes")
    private String orderNotes;

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

    public BigDecimal getFreeShippingFrom() {
        return freeShippingFrom;
    }

    public void setFreeShippingFrom(BigDecimal freeShippingFrom) {
        this.freeShippingFrom = freeShippingFrom;
    }

    public String getHeroTitle() {
        return heroTitle;
    }

    public void setHeroTitle(String heroTitle) {
        this.heroTitle = heroTitle;
    }

    public String getHeroText() {
        return heroText;
    }

    public void setHeroText(String heroText) {
        this.heroText = heroText;
    }

    public List<Map<String, String>> getTrustBadges() {
        return trustBadges;
    }

    public void setTrustBadges(List<Map<String, String>> trustBadges) {
        this.trustBadges = trustBadges;
    }

    public List<Map<String, String>> getProcess() {
        return process;
    }

    public void setProcess(List<Map<String, String>> process) {
        this.process = process;
    }

    public List<Map<String, String>> getFaq() {
        return faq;
    }

    public void setFaq(List<Map<String, String>> faq) {
        this.faq = faq;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }

    public String getOrderNotes() {
        return orderNotes;
    }

    public void setOrderNotes(String orderNotes) {
        this.orderNotes = orderNotes;
    }
}
