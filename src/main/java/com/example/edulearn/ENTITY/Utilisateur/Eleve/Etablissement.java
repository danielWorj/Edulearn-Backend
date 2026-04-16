package com.example.edulearn.ENTITY.Utilisateur.Eleve;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table
public class Etablissement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;
    private String intitule ;
}
