package com.example.edulearn.DTO.Evaluation;

import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.TypeEvaluation;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CompositionDTO {
    private Integer id ;
    private String description ;
    private Integer duree ;
    private Boolean active ;
    private String dateCreation ;
    private Boolean archived ;
    private Integer typeEvaluation;
    private Integer repetition;
    private Integer matiere;



}
