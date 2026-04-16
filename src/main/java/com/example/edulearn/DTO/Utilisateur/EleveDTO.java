package com.example.edulearn.DTO.Utilisateur;

import com.example.edulearn.ENTITY.Academie.Filiere;
import com.example.edulearn.ENTITY.Academie.Niveau;
import com.example.edulearn.ENTITY.Utilisateur.Eleve.Etablissement;
import com.example.edulearn.ENTITY.Utilisateur.Parent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EleveDTO extends UtilisateurDTO{
    private String dateNaissance ;
    //private Integer etablissement ;
    private Integer niveau ;
    private String redoublant ;
    private Integer filiere ;
    private Integer parent ;
}
