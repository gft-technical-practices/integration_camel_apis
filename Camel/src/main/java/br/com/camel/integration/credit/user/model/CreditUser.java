package br.com.camel.integration.credit.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class CreditUser implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id;
    @JsonProperty("cpf")
    private String cpf;
    @JsonProperty("value")
    private Double value;
    @JsonProperty("anualPercentage")
    private Double anualPercentage;
    @JsonProperty("name")
    private String name;
    @JsonProperty("age")
    private Integer age;
    @JsonProperty("statusMessage")
    private String statusMessage;

}
