package com.example.edulearn.DTO.MarketPlace;

import com.example.edulearn.ENTITY.Academie.Filiere;
import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Academie.Niveau;
import com.example.edulearn.ENTITY.MarketPlace.TypeResource;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class SupportDTO {

    private Integer id ;
    private String title ;
    @Lob
    private String resume ;
    private Integer prix ;
    private Integer matiere;
    private Integer niveau;
    private Integer filiere;
    private Integer type;
    private Integer enseignant;
}
