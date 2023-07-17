/*
 * Selling Partner API for Catalog Items
 * The Selling Partner API for Catalog Items provides programmatic access to information about items in the Amazon catalog.  For more information, refer to the [Catalog Items API Use Case Guide](doc:catalog-items-api-v2022-04-01-use-case-guide).
 *
 * OpenAPI spec version: 2022-04-01
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.cn.hzm.core.spa.item.model;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Individual dimension value of an Amazon catalog item or item package.
 */
@ApiModel(description = "Individual dimension value of an Amazon catalog item or item package.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2023-06-05T18:19:27.221+08:00")
public class Dimension {
  @SerializedName("unit")
  private String unit = null;

  @SerializedName("value")
  private BigDecimal value = null;

  public Dimension unit(String unit) {
    this.unit = unit;
    return this;
  }

   /**
   * Measurement unit of the dimension value.
   * @return unit
  **/
  @ApiModelProperty(value = "Measurement unit of the dimension value.")
  public String getUnit() {
    return unit;
  }

  public void setUnit(String unit) {
    this.unit = unit;
  }

  public Dimension value(BigDecimal value) {
    this.value = value;
    return this;
  }

   /**
   * Numeric dimension value.
   * @return value
  **/
  @ApiModelProperty(value = "Numeric dimension value.")
  public BigDecimal getValue() {
    return value;
  }

  public void setValue(BigDecimal value) {
    this.value = value;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Dimension dimension = (Dimension) o;
    return Objects.equals(this.unit, dimension.unit) &&
        Objects.equals(this.value, dimension.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(unit, value);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Dimension {\n");

    sb.append("    unit: ").append(toIndentedString(unit)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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
