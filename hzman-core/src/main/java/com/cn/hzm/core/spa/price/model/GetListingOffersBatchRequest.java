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
 * The request associated with the &#x60;getListingOffersBatch&#x60; API call.
 */
@ApiModel(description = "The request associated with the `getListingOffersBatch` API call.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-06-07T22:11:14.374+08:00")
public class GetListingOffersBatchRequest {
  @SerializedName("requests")
  private ListingOffersRequestList requests = null;

  public GetListingOffersBatchRequest requests(ListingOffersRequestList requests) {
    this.requests = requests;
    return this;
  }

   /**
   * Get requests
   * @return requests
  **/
  @ApiModelProperty(value = "")
  public ListingOffersRequestList getRequests() {
    return requests;
  }

  public void setRequests(ListingOffersRequestList requests) {
    this.requests = requests;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetListingOffersBatchRequest getListingOffersBatchRequest = (GetListingOffersBatchRequest) o;
    return Objects.equals(this.requests, getListingOffersBatchRequest.requests);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requests);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetListingOffersBatchRequest {\n");

    sb.append("    requests: ").append(toIndentedString(requests)).append("\n");
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

