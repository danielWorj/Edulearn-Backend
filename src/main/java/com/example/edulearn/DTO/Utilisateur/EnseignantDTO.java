package com.example.edulearn.DTO.Utilisateur;

import com.example.edulearn.ENTITY.Academie.Section;
import com.example.edulearn.ENTITY.Status.StatusEnseignant;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Diplome;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.ProfilEnseignant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EnseignantDTO  extends UtilisateurDTO{
    private Integer anneeexperience ;
    private String dateNaissance ;
    private Integer statusEnseignant;

    private String bio ;
    private Integer tarifHoraire ;
    private String cv ;
    private String diplomeurl ;
    private Integer section ;
    private Integer profilEnseignant ;
    private Integer diplome ;
    private String specialite ;

}
