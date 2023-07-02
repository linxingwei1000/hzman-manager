/*
 * Selling Partner API for Pricing
 * The Selling Partner API for Pricing helps you programmatically retrieve product pricing and offer information for Amazon Marketplace products.
 *
 * OpenAPI spec version: v0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.cn.hzm.core.spa.price.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Contains pricing information that includes special pricing when buying in bulk.
 */
@ApiModel(description = "Contains pricing information that includes special pricing when buying in bulk.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-06-07T22:11:14.374+08:00")
public class QuantityDiscountPriceType {
  @SerializedName("quantityTier")
  private Integer quantityTier = null;

  @SerializedName("quantityDiscountType")
  private QuantityDiscountType quantityDiscountType = null;

  @SerializedName("listingPrice")
  private MoneyType listingPrice = null;

  public QuantityDiscountPriceType quantityTier(Integer quantityTier) {
    this.quantityTier = quantityTier;
    return this;
  }

   /**
   * Indicates at what quantity this price becomes active.
   * @return quantityTier
  **/
  @ApiModelProperty(required = true, value = "Indicates at what quantity this price becomes active.")
  public Integer getQuantityTier() {
    return quantityTier;
  }

  public void setQuantityTier(Integer quantityTier) {
    this.quantityTier = quantityTier;
  }

  public QuantityDiscountPriceType quantityDiscountType(QuantityDiscountType quantityDiscountType) {
    this.quantityDiscountType = quantityDiscountType;
    return this;
  }

   /**
   * Indicates the type of quantity discount this price applies to.
   * @return quantityDiscountType
  **/
  @ApiModelProperty(required = true, value = "Indicates the type of quantity discount this price applies to.")
  public QuantityDiscountType getQuantityDiscountType() {
    return quantityDiscountType;
  }

  public void setQuantityDiscountType(QuantityDiscountType quantityDiscountType) {
    this.quantityDiscountType = quantityDiscountType;
  }

  public QuantityDiscountPriceType listingPrice(MoneyType listingPrice) {
    this.listingPrice = listingPrice;
    return this;
  }

   /**
   * The price at this quantity tier.
   * @return listingPrice
  **/
  @ApiModelProperty(required = true, value = "The price at this quantity tier.")
  public MoneyType getListingPrice() {
    return listingPrice;
  }

  public void setListingPrice(MoneyType listingPrice) {
    this.listingPrice = listingPrice;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QuantityDiscountPriceType quantityDiscountPriceType = (QuantityDiscountPriceType) o;
    return Objects.equals(this.quantityTier, quantityDiscountPriceType.quantityTier) &&
        Objects.equals(this.quantityDiscountType, quantityDiscountPriceType.quantityDiscountType) &&
        Objects.equals(this.listingPrice, quantityDiscountPriceType.listingPrice);
  }

  @Override
  public int hashCode() {
    return Objects.hash(quantityTier, quantityDiscountType, listingPrice);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class QuantityDiscountPriceType {\n");

    sb.append("    quantityTier: ").append(toIndentedString(quantityTier)).append("\n");
    sb.append("    quantityDiscountType: ").append(toIndentedString(quantityDiscountType)).append("\n");
    sb.append("    listingPrice: ").append(toIndentedString(listingPrice)).append("\n");
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

