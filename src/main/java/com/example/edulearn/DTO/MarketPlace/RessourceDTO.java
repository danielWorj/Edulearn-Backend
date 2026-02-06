package com.example.edulearn.DTO.MarketPlace;

import com.example.edulearn.ENTITY.MarketPlace.TypeResource;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RessourceDTO {
    private Integer id;
    private String nom;
    private String description;
    private String url;
    private Integer prix ;
    private String date ;
    private Integer enseignant ;
    private Integer typeResource ;
}
