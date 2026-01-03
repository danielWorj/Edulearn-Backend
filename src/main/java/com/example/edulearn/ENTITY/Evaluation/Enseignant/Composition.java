package com.example.edulearn.ENTITY.Evaluation.Enseignant;

import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Repetition.Repetition;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@Table
public class Composition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;
    private String description ;
    private Integer duree ;//En minutes
    private Boolean active ;
    private LocalDate dateCreation ;
    private Boolean archived ;//Pour savoir si l'eleve a deja compose ou non
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TypeEvaluation typeEvaluation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Repetition repetition;



}
