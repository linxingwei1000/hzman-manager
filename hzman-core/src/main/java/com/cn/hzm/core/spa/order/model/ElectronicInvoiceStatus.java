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

import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * The status of the electronic invoice.
 */
@JsonAdapter(ElectronicInvoiceStatus.Adapter.class)
public enum ElectronicInvoiceStatus {
  
  NOTREQUIRED("NotRequired"),
  
  NOTFOUND("NotFound"),
  
  PROCESSING("Processing"),
  
  ERRORED("Errored"),
  
  ACCEPTED("Accepted");

  private String value;

  ElectronicInvoiceStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  public static ElectronicInvoiceStatus fromValue(String text) {
    for (ElectronicInvoiceStatus b : ElectronicInvoiceStatus.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }

  public static class Adapter extends TypeAdapter<ElectronicInvoiceStatus> {
    @Override
    public void write(final JsonWriter jsonWriter, final ElectronicInvoiceStatus enumeration) throws IOException {
      jsonWriter.value(enumeration.getValue());
    }

    @Override
    public ElectronicInvoiceStatus read(final JsonReader jsonReader) throws IOException {
      String value = jsonReader.nextString();
      return ElectronicInvoiceStatus.fromValue(String.valueOf(value));
    }
  }
}

