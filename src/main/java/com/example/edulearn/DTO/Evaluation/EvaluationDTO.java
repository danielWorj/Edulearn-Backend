package com.example.edulearn.DTO.Evaluation;

import com.example.edulearn.ENTITY.Evaluation.Enseignant.Composition;
import com.example.edulearn.ENTITY.Utilisateur.Eleve.Eleve;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;


@Data
public class EvaluationDTO {
    private Integer id ;
    private LocalDate dateCreated;
    private LocalTime startTime ;
    private LocalTime endTime ;
    private Double note ;
    private Boolean completed ;
    private Integer composition;
}
