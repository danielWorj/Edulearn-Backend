package com.example.edulearn.ENTITY.Commentaire;

import com.example.edulearn.ENTITY.Evaluation.Enseignant.Composition;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.example.edulearn.ENTITY.Utilisateur.Parent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class Commentaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;
    private String contenu ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Enseignant enseignant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Parent parent;

}
