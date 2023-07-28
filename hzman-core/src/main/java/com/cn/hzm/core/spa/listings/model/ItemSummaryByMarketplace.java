/*
 * Selling Partner API for Listings Items
 * The Selling Partner API for Listings Items (Listings Items API) provides programmatic access to selling partner listings on Amazon. Use this API in collaboration with the Selling Partner API for Product Type Definitions, which you use to retrieve the information about Amazon product types needed to use the Listings Items API.  For more information, see the [Listings Items API Use Case Guide](doc:listings-items-api-v2021-08-01-use-case-guide).
 *
 * OpenAPI spec version: 2021-08-01
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.cn.hzm.core.spa.listings.model;

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.threeten.bp.OffsetDateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Summary details of a listings item for an Amazon marketplace.
 */
@ApiModel(description = "Summary details of a listings item for an Amazon marketplace.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-01-06T11:24:46.340+08:00")
public class ItemSummaryByMarketplace {
  @SerializedName("marketplaceId")
  private String marketplaceId = null;

  @SerializedName("asin")
  private String asin = null;

  @SerializedName("productType")
  private String productType = null;

  /**
   * Identifies the condition of the listings item.
   */
  @JsonAdapter(ConditionTypeEnum.Adapter.class)
  public enum ConditionTypeEnum {
    NEW_NEW("new_new"),
    
    NEW_OPEN_BOX("new_open_box"),
    
    NEW_OEM("new_oem"),
    
    REFURBISHED_REFURBISHED("refurbished_refurbished"),
    
    USED_LIKE_NEW("used_like_new"),
    
    USED_VERY_GOOD("used_very_good"),
    
    USED_GOOD("used_good"),
    
    USED_ACCEPTABLE("used_acceptable"),
    
    COLLECTIBLE_LIKE_NEW("collectible_like_new"),
    
    COLLECTIBLE_VERY_GOOD("collectible_very_good"),
    
    COLLECTIBLE_GOOD("collectible_good"),
    
    COLLECTIBLE_ACCEPTABLE("collectible_acceptable"),
    
    CLUB_CLUB("club_club");

    private String value;

    ConditionTypeEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static ConditionTypeEnum fromValue(String text) {
      for (ConditionTypeEnum b : ConditionTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    public static class Adapter extends TypeAdapter<ConditionTypeEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final ConditionTypeEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public ConditionTypeEnum read(final JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return ConditionTypeEnum.fromValue(String.valueOf(value));
      }
    }
  }

  @SerializedName("conditionType")
  private ConditionTypeEnum conditionType = null;

  /**
   * Gets or Sets status
   */
  @JsonAdapter(StatusEnum.Adapter.class)
  public enum StatusEnum {
    BUYABLE("BUYABLE"),
    
    DISCOVERABLE("DISCOVERABLE");

    private String value;

    StatusEnum(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }

    public static StatusEnum fromValue(String text) {
      for (StatusEnum b : StatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }

    public static class Adapter extends TypeAdapter<StatusEnum> {
      @Override
      public void write(final JsonWriter jsonWriter, final StatusEnum enumeration) throws IOException {
        jsonWriter.value(enumeration.getValue());
      }

      @Override
      public StatusEnum read(final JsonReader jsonReader) throws IOException {
        String value = jsonReader.nextString();
        return StatusEnum.fromValue(String.valueOf(value));
      }
    }
  }

  @SerializedName("status")
  private List<StatusEnum> status = new ArrayList<StatusEnum>();

  @SerializedName("fnSku")
  private String fnSku = null;

  @SerializedName("itemName")
  private String itemName = null;

  @SerializedName("createdDate")
  private OffsetDateTime createdDate = null;

  @SerializedName("lastUpdatedDate")
  private OffsetDateTime lastUpdatedDate = null;

  @SerializedName("mainImage")
  private ItemImage mainImage = null;

  public ItemSummaryByMarketplace marketplaceId(String marketplaceId) {
    this.marketplaceId = marketplaceId;
    return this;
  }

   /**
   * A marketplace identifier. Identifies the Amazon marketplace for the listings item.
   * @return marketplaceId
  **/
  @ApiModelProperty(required = true, value = "A marketplace identifier. Identifies the Amazon marketplace for the listings item.")
  public String getMarketplaceId() {
    return marketplaceId;
  }

  public void setMarketplaceId(String marketplaceId) {
    this.marketplaceId = marketplaceId;
  }

  public ItemSummaryByMarketplace asin(String asin) {
    this.asin = asin;
    return this;
  }

   /**
   * Amazon Standard Identification Number (ASIN) of the listings item.
   * @return asin
  **/
  @ApiModelProperty(required = true, value = "Amazon Standard Identification Number (ASIN) of the listings item.")
  public String getAsin() {
    return asin;
  }

  public void setAsin(String asin) {
    this.asin = asin;
  }

  public ItemSummaryByMarketplace productType(String productType) {
    this.productType = productType;
    return this;
  }

   /**
   * The Amazon product type of the listings item.
   * @return productType
  **/
  @ApiModelProperty(required = true, value = "The Amazon product type of the listings item.")
  public String getProductType() {
    return productType;
  }

  public void setProductType(String productType) {
    this.productType = productType;
  }

  public ItemSummaryByMarketplace conditionType(ConditionTypeEnum conditionType) {
    this.conditionType = conditionType;
    return this;
  }

   /**
   * Identifies the condition of the listings item.
   * @return conditionType
  **/
  @ApiModelProperty(value = "Identifies the condition of the listings item.")
  public ConditionTypeEnum getConditionType() {
    return conditionType;
  }

  public void setConditionType(ConditionTypeEnum conditionType) {
    this.conditionType = conditionType;
  }

  public ItemSummaryByMarketplace status(List<StatusEnum> status) {
    this.status = status;
    return this;
  }

  public ItemSummaryByMarketplace addStatusItem(StatusEnum statusItem) {
    this.status.add(statusItem);
    return this;
  }

   /**
   * Statuses that apply to the listings item.
   * @return status
  **/
  @ApiModelProperty(required = true, value = "Statuses that apply to the listings item.")
  public List<StatusEnum> getStatus() {
    return status;
  }

  public void setStatus(List<StatusEnum> status) {
    this.status = status;
  }

  public ItemSummaryByMarketplace fnSku(String fnSku) {
    this.fnSku = fnSku;
    return this;
  }

   /**
   * Fulfillment network stock keeping unit is an identifier used by Amazon fulfillment centers to identify each unique item.
   * @return fnSku
  **/
  @ApiModelProperty(value = "Fulfillment network stock keeping unit is an identifier used by Amazon fulfillment centers to identify each unique item.")
  public String getFnSku() {
    return fnSku;
  }

  public void setFnSku(String fnSku) {
    this.fnSku = fnSku;
  }

  public ItemSummaryByMarketplace itemName(String itemName) {
    this.itemName = itemName;
    return this;
  }

   /**
   * Name, or title, associated with an Amazon catalog item.
   * @return itemName
  **/
  @ApiModelProperty(required = true, value = "Name, or title, associated with an Amazon catalog item.")
  public String getItemName() {
    return itemName;
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  public ItemSummaryByMarketplace createdDate(OffsetDateTime createdDate) {
    this.createdDate = createdDate;
    return this;
  }

   /**
   * Date the listings item was created, in ISO 8601 format.
   * @return createdDate
  **/
  @ApiModelProperty(required = true, value = "Date the listings item was created, in ISO 8601 format.")
  public OffsetDateTime getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(OffsetDateTime createdDate) {
    this.createdDate = createdDate;
  }

  public ItemSummaryByMarketplace lastUpdatedDate(OffsetDateTime lastUpdatedDate) {
    this.lastUpdatedDate = lastUpdatedDate;
    return this;
  }

   /**
   * Date the listings item was last updated, in ISO 8601 format.
   * @return lastUpdatedDate
  **/
  @ApiModelProperty(required = true, value = "Date the listings item was last updated, in ISO 8601 format.")
  public OffsetDateTime getLastUpdatedDate() {
    return lastUpdatedDate;
  }

  public void setLastUpdatedDate(OffsetDateTime lastUpdatedDate) {
    this.lastUpdatedDate = lastUpdatedDate;
  }

  public ItemSummaryByMarketplace mainImage(ItemImage mainImage) {
    this.mainImage = mainImage;
    return this;
  }

   /**
   * Main image for the listings item.
   * @return mainImage
  **/
  @ApiModelProperty(value = "Main image for the listings item.")
  public ItemImage getMainImage() {
    return mainImage;
  }

  public void setMainImage(ItemImage mainImage) {
    this.mainImage = mainImage;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ItemSummaryByMarketplace itemSummaryByMarketplace = (ItemSummaryByMarketplace) o;
    return Objects.equals(this.marketplaceId, itemSummaryByMarketplace.marketplaceId) &&
        Objects.equals(this.asin, itemSummaryByMarketplace.asin) &&
        Objects.equals(this.productType, itemSummaryByMarketplace.productType) &&
        Objects.equals(this.conditionType, itemSummaryByMarketplace.conditionType) &&
        Objects.equals(this.status, itemSummaryByMarketplace.status) &&
        Objects.equals(this.fnSku, itemSummaryByMarketplace.fnSku) &&
        Objects.equals(this.itemName, itemSummaryByMarketplace.itemName) &&
        Objects.equals(this.createdDate, itemSummaryByMarketplace.createdDate) &&
        Objects.equals(this.lastUpdatedDate, itemSummaryByMarketplace.lastUpdatedDate) &&
        Objects.equals(this.mainImage, itemSummaryByMarketplace.mainImage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(marketplaceId, asin, productType, conditionType, status, fnSku, itemName, createdDate, lastUpdatedDate, mainImage);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ItemSummaryByMarketplace {\n");

    sb.append("    marketplaceId: ").append(toIndentedString(marketplaceId)).append("\n");
    sb.append("    asin: ").append(toIndentedString(asin)).append("\n");
    sb.append("    productType: ").append(toIndentedString(productType)).append("\n");
    sb.append("    conditionType: ").append(toIndentedString(conditionType)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    fnSku: ").append(toIndentedString(fnSku)).append("\n");
    sb.append("    itemName: ").append(toIndentedString(itemName)).append("\n");
    sb.append("    createdDate: ").append(toIndentedString(createdDate)).append("\n");
    sb.append("    lastUpdatedDate: ").append(toIndentedString(lastUpdatedDate)).append("\n");
    sb.append("    mainImage: ").append(toIndentedString(mainImage)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

