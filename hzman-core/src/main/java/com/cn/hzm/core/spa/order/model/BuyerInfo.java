/*
 * Selling Partner API for Orders
 * The Selling Partner API for Orders helps you programmatically retrieve order information. These APIs let you develop fast, flexible, custom applications in areas like order synchronization, order research, and demand-based decision support tools.
 *
 * OpenAPI spec version: v0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.cn.hzm.core.spa.order.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Buyer information.
 */
@ApiModel(description = "Buyer information.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-01-05T18:23:51.475+08:00")
public class BuyerInfo {
  @SerializedName("BuyerEmail")
  private String buyerEmail = null;

  @SerializedName("BuyerName")
  private String buyerName = null;

  @SerializedName("BuyerCounty")
  private String buyerCounty = null;

  @SerializedName("BuyerTaxInfo")
  private BuyerTaxInfo buyerTaxInfo = null;

  @SerializedName("PurchaseOrderNumber")
  private String purchaseOrderNumber = null;

  public BuyerInfo buyerEmail(String buyerEmail) {
    this.buyerEmail = buyerEmail;
    return this;
  }

   /**
   * The anonymized email address of the buyer.
   * @return buyerEmail
  **/
  @ApiModelProperty(value = "The anonymized email address of the buyer.")
  public String getBuyerEmail() {
    return buyerEmail;
  }

  public void setBuyerEmail(String buyerEmail) {
    this.buyerEmail = buyerEmail;
  }

  public BuyerInfo buyerName(String buyerName) {
    this.buyerName = buyerName;
    return this;
  }

   /**
   * The buyer name or the recipient name.
   * @return buyerName
  **/
  @ApiModelProperty(value = "The buyer name or the recipient name.")
  public String getBuyerName() {
    return buyerName;
  }

  public void setBuyerName(String buyerName) {
    this.buyerName = buyerName;
  }

  public BuyerInfo buyerCounty(String buyerCounty) {
    this.buyerCounty = buyerCounty;
    return this;
  }

   /**
   * The county of the buyer.
   * @return buyerCounty
  **/
  @ApiModelProperty(value = "The county of the buyer.")
  public String getBuyerCounty() {
    return buyerCounty;
  }

  public void setBuyerCounty(String buyerCounty) {
    this.buyerCounty = buyerCounty;
  }

  public BuyerInfo buyerTaxInfo(BuyerTaxInfo buyerTaxInfo) {
    this.buyerTaxInfo = buyerTaxInfo;
    return this;
  }

   /**
   * Tax information about the buyer.
   * @return buyerTaxInfo
  **/
  @ApiModelProperty(value = "Tax information about the buyer.")
  public BuyerTaxInfo getBuyerTaxInfo() {
    return buyerTaxInfo;
  }

  public void setBuyerTaxInfo(BuyerTaxInfo buyerTaxInfo) {
    this.buyerTaxInfo = buyerTaxInfo;
  }

  public BuyerInfo purchaseOrderNumber(String purchaseOrderNumber) {
    this.purchaseOrderNumber = purchaseOrderNumber;
    return this;
  }

   /**
   * The purchase order (PO) number entered by the buyer at checkout. Returned only for orders where the buyer entered a PO number at checkout.
   * @return purchaseOrderNumber
  **/
  @ApiModelProperty(value = "The purchase order (PO) number entered by the buyer at checkout. Returned only for orders where the buyer entered a PO number at checkout.")
  public String getPurchaseOrderNumber() {
    return purchaseOrderNumber;
  }

  public void setPurchaseOrderNumber(String purchaseOrderNumber) {
    this.purchaseOrderNumber = purchaseOrderNumber;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BuyerInfo buyerInfo = (BuyerInfo) o;
    return Objects.equals(this.buyerEmail, buyerInfo.buyerEmail) &&
        Objects.equals(this.buyerName, buyerInfo.buyerName) &&
        Objects.equals(this.buyerCounty, buyerInfo.buyerCounty) &&
        Objects.equals(this.buyerTaxInfo, buyerInfo.buyerTaxInfo) &&
        Objects.equals(this.purchaseOrderNumber, buyerInfo.purchaseOrderNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(buyerEmail, buyerName, buyerCounty, buyerTaxInfo, purchaseOrderNumber);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BuyerInfo {\n");

    sb.append("    buyerEmail: ").append(toIndentedString(buyerEmail)).append("\n");
    sb.append("    buyerName: ").append(toIndentedString(buyerName)).append("\n");
    sb.append("    buyerCounty: ").append(toIndentedString(buyerCounty)).append("\n");
    sb.append("    buyerTaxInfo: ").append(toIndentedString(buyerTaxInfo)).append("\n");
    sb.append("    purchaseOrderNumber: ").append(toIndentedString(purchaseOrderNumber)).append("\n");
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

