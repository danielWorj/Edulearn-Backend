package com.example.edulearn.ENTITY.Repetition;

import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Utilisateur.Eleve.Eleve;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Entity
@Table
@Data
public class Repetition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Enseignant enseignant ;

    //Une session de repetition va appartenir a une offre
    //Cest comme un contrat apres publication d'une offre par le parent
    //
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private OffreRepetition offreRepetition ;

    private Integer montant ; //Ca c'est le montant final decide

    @OneToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<MatiereRepetition> matiereRepetitions;



}
