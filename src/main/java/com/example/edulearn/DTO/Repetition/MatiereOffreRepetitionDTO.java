package com.example.edulearn.DTO.Repetition;

import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Repetition.OffreRepetition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class MatiereOffreRepetitionDTO {

    private Integer id ;
    private Integer offreRepetition ;
    private Integer matiere ;
}
