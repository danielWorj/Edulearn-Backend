package com.example.edulearn.DTO.Utilisateur;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UtilisateurDTO {
    private Integer id;
    private String nomComplet ;
    private String telephone ;
    private String email ;
    private Integer role ;
    private String password ;
    private String dateInscription ;
    private Boolean status ; // true : actif , false : suspendu
    private String localisation ;
    private String photo ;
}
