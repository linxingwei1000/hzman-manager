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
 * The response schema for the getOrderRegulatedInfo operation.
 */
@ApiModel(description = "The response schema for the getOrderRegulatedInfo operation.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-01-05T18:23:51.475+08:00")
public class GetOrderRegulatedInfoResponse {
  @SerializedName("payload")
  private OrderRegulatedInfo payload = null;

  @SerializedName("errors")
  private ErrorList errors = null;

  public GetOrderRegulatedInfoResponse payload(OrderRegulatedInfo payload) {
    this.payload = payload;
    return this;
  }

   /**
   * The payload for the getOrderRegulatedInfo operation.
   * @return payload
  **/
  @ApiModelProperty(value = "The payload for the getOrderRegulatedInfo operation.")
  public OrderRegulatedInfo getPayload() {
    return payload;
  }

  public void setPayload(OrderRegulatedInfo payload) {
    this.payload = payload;
  }

  public GetOrderRegulatedInfoResponse errors(ErrorList errors) {
    this.errors = errors;
    return this;
  }

   /**
   * One or more unexpected errors occurred during the getOrderRegulatedInfo operation.
   * @return errors
  **/
  @ApiModelProperty(value = "One or more unexpected errors occurred during the getOrderRegulatedInfo operation.")
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
    GetOrderRegulatedInfoResponse getOrderRegulatedInfoResponse = (GetOrderRegulatedInfoResponse) o;
    return Objects.equals(this.payload, getOrderRegulatedInfoResponse.payload) &&
        Objects.equals(this.errors, getOrderRegulatedInfoResponse.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(payload, errors);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GetOrderRegulatedInfoResponse {\n");

    sb.append("    payload: ").append(toIndentedString(payload)).append("\n");
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

