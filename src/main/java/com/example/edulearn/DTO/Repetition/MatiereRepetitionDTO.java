package com.example.edulearn.DTO.Repetition;

import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Repetition.Repetition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class MatiereRepetitionDTO {

    private Integer id ;
    private Integer repetition ;
    private Integer matiere ;
}
