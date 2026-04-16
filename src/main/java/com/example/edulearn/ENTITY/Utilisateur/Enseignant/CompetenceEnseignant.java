package com.example.edulearn.ENTITY.Utilisateur.Enseignant;

import com.example.edulearn.ENTITY.Status.StatusEnseignant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class CompetenceEnseignant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;




}
