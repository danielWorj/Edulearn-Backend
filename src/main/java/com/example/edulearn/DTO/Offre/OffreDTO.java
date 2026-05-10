package com.example.edulearn.DTO.Offre;

import com.example.edulearn.ENTITY.Academie.Niveau;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class OffreDTO {
    //Informations sur l'offre
    private String bio;
    private String budget ;
    private Integer frequence;
    private String duree;
    private Integer niveau;
    private Integer filiere ;
    //Informations sur les matieres
    private List<Integer> matieres ;

    //Informations sur le parent
    private String nomComplet ;
    private String telephone ;
    private String email ;
    private String password ;
    private String localisation ;
    private String profession ;
}
