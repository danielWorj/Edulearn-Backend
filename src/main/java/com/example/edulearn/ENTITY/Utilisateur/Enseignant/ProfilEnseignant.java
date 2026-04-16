package com.example.edulearn.ENTITY.Utilisateur.Enseignant;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class ProfilEnseignant {
    //Enseigannt scientifique / litteraire / technique ...
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;
    private String intitule ;
}
