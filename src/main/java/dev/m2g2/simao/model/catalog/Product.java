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
    private String size = "";

    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.ACTIVE;

    private String observations;

    private BigDecimal grams;

    @Column(name = "print_time_hours")
    private BigDecimal printTimeHours;

    @Column(name = "labor_minutes")
    private BigDecimal laborMinutes;

    private BigDecimal supplies;

    private BigDecimal packaging;

    @Column(name = "catalog_price")
    private BigDecimal catalogPrice;

    @Column(name = "exact_time")
    private boolean exactTime = true;

    // Cover photo, kept in sync with photos[0] for compatibility.
    private String photo;
    private String origin;
    private String printer;
    private String filament;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<ProductPhoto> photos = new ArrayList<>();

    // --- storefront / SEO ---
    private String slug;
    private Integer leadTimeDays;   // prazo de produção, em dias
    private Integer sortOrder;      // ordenação na vitrine
    private String material;
    @Column(name = "part_dimensions")
    private String partDimensions;
    @Column(name = "package_weight")
    private BigDecimal packageWeight;
    @Column(name = "package_dimensions")
    private String packageDimensions;
    private boolean published = true;
    private boolean featured = false;
    @Column(name = "long_description")
    private String longDescription;
    @Column(name = "meta_description")
    private String metaDescription;
    private String license;

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

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size == null ? "" : size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public BigDecimal getGrams() {
        return grams;
    }

    public void setGrams(BigDecimal grams) {
        this.grams = grams;
    }

    public BigDecimal getPrintTimeHours() {
        return printTimeHours;
    }

    public void setPrintTimeHours(BigDecimal printTimeHours) {
        this.printTimeHours = printTimeHours;
    }

    public BigDecimal getLaborMinutes() {
        return laborMinutes;
    }

    public void setLaborMinutes(BigDecimal laborMinutes) {
        this.laborMinutes = laborMinutes;
    }

    public BigDecimal getSupplies() {
        return supplies;
    }

    public void setSupplies(BigDecimal supplies) {
        this.supplies = supplies;
    }

    public BigDecimal getPackaging() {
        return packaging;
    }

    public void setPackaging(BigDecimal packaging) {
        this.packaging = packaging;
    }

    public BigDecimal getCatalogPrice() {
        return catalogPrice;
    }

    public void setCatalogPrice(BigDecimal catalogPrice) {
        this.catalogPrice = catalogPrice;
    }

    public boolean isExactTime() {
        return exactTime;
    }

    public void setExactTime(boolean exactTime) {
        this.exactTime = exactTime;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getPrinter() {
        return printer;
    }

    public void setPrinter(String printer) {
        this.printer = printer;
    }

    public String getFilament() {
        return filament;
    }

    public void setFilament(String filament) {
        this.filament = filament;
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

    public Integer getLeadTimeDays() {
        return leadTimeDays;
    }

    public void setLeadTimeDays(Integer leadTimeDays) {
        this.leadTimeDays = leadTimeDays;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public String getPartDimensions() {
        return partDimensions;
    }

    public void setPartDimensions(String partDimensions) {
        this.partDimensions = partDimensions;
    }

    public BigDecimal getPackageWeight() {
        return packageWeight;
    }

    public void setPackageWeight(BigDecimal packageWeight) {
        this.packageWeight = packageWeight;
    }

    public String getPackageDimensions() {
        return packageDimensions;
    }

    public void setPackageDimensions(String packageDimensions) {
        this.packageDimensions = packageDimensions;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public String getMetaDescription() {
        return metaDescription;
    }

    public void setMetaDescription(String metaDescription) {
        this.metaDescription = metaDescription;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }
}
