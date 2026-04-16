package com.example.edulearn.DTO.Evaluation;

import com.example.edulearn.ENTITY.Evaluation.Eleve.Evaluation;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.Question;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.ReponsePossible;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class ReponseEleveDTO {
    private Integer id ;
    private Integer evaluation;
    private Integer question;
    private Integer reponseChoisie;
}
