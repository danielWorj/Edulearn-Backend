package com.example.edulearn.ENTITY.MarketPlace.New;

import com.example.edulearn.ENTITY.Academie.Filiere;
import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Academie.Niveau;
import com.example.edulearn.ENTITY.MarketPlace.TypeResource;
import com.example.edulearn.ENTITY.Repetition.New.Offre;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Support {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;
    private String title ;
    @Lob
    private String resume;
    private Boolean statut ;
    private LocalDate date;
    private Integer prix ;
    private String file ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Matiere matiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Niveau niveau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Filiere filiere;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private TypeResource type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Enseignant enseignant;




}
