package com.example.edulearn.ENTITY.Utilisateur.Enseignant;

import com.example.edulearn.ENTITY.Academie.Section;
import com.example.edulearn.ENTITY.Status.StatusEnseignant;
import com.example.edulearn.ENTITY.Utilisateur.Utilisateur;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@DiscriminatorValue(value = "enseignant")
public class Enseignant extends Utilisateur {
    private Integer anneeexperience ;
    private LocalDate dateNaissance ;
    @Lob
    private String bio ;
    private Integer tarifHoraire ;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private StatusEnseignant statusEnseignant ;
    private String cv ;
    private String diplomeurl ;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Section section ;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private ProfilEnseignant profilEnseignant ;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Diplome diplome ;
    private String specialite ;



}
