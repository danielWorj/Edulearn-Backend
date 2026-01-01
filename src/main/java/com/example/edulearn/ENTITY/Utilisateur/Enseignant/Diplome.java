package com.example.edulearn.ENTITY.Utilisateur.Enseignant;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table
public class Diplome {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;
    private String intitule ;
}
