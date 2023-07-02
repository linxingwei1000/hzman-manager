/*
 * Selling Partner API for FBA Inventory
 * The Selling Partner API for FBA Inventory lets you programmatically retrieve information about inventory in Amazon's fulfillment network.
 *
 * OpenAPI spec version: v1
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.cn.hzm.core.spa.fbainventory.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * The Response schema.
 */
@ApiModel(description = "The Response schema.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-06-22T16:06:56.786+08:00")
public class GetInventorySummariesResponse {
  @SerializedName("payload")
  private GetInventorySummariesResult payload = null;

  @SerializedName("pagination")
  private Pagination pagination = null;

  @SerializedName("errors")
  private ErrorList errors = null;

  public GetInventorySummariesResponse payload(GetInventorySummariesResult payload) {
    this.payload = payload;
    return this;
  }

   /**
   * The payload for the getInventorySummaries operation.
   * @return payload
  **/
  @ApiModelProperty(value = "The payload for the getInventorySummaries operation.")
  public GetInventorySummariesResult getPayload() {
    return payload;
  }

  public void setPayload(GetInventorySummariesResult payload) {
    this.payload = payload;
  }

  public GetInventorySummariesResponse pagination(Pagination pagination) {
    this.pagination = pagination;
    return this;
  }

   /**
   * Get pagination
   * @return pagination
  **/
  @ApiModelProperty(value = "")
  public Pagination getPagination() {
    return pagination;
  }

  public void setPagination(Pagination pagination) {
    this.pagination = pagination;
  }

  public GetInventorySummariesResponse errors(ErrorList errors) {
    this.errors = errors;
    return this;
  }

   /**
   * One or more unexpected errors occurred during the getInventorySummaries operation.
   * @return errors
  **/
  @ApiModelProperty(value = "One or more unexpected errors occurred during the getInventorySummaries operation.")
  public ErrorList getErrors() {
    return errors;
  }

  public void setErrors(ErrorList errors) {
    this.errors = errors;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GetInventorySummariesResponse getInventorySummariesResponse = (GetInventorySummariesResponse) o;
    return Objects.equals(this.payload, getInventorySummariesResponse.payload) &&
        Objects.equals(this.pagination, getInventorySummariesResponse.pagination) &&
        Objects.equals(this.errors, getInventorySummariesResponse.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(payload, pagination, errors);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetInventorySummariesResponse {\n");

    sb.append("    payload: ").append(toIndentedString(payload)).append("\n");
    sb.append("    pagination: ").append(toIndentedString(pagination)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
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

