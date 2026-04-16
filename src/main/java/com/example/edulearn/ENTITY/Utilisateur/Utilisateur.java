package com.example.edulearn.ENTITY.Utilisateur;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Inheritance(strategy = InheritanceType.JOINED) //Heritage
@DiscriminatorColumn(name = "quality_user")
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nomComplet ;
    private String telephone ;
    private String email ;
    private Integer role ; //1 :ADMIN / 2: ENSEIGNANT / 3: PARENT / 4: ELEVE
    private String password ;
    private LocalDate dateInscription ;
    private Boolean status ; // true : actif , false : suspendu
    private String localisation ;
    private String photo ;
}
