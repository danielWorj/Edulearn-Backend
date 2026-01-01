package com.example.edulearn.DTO.Evaluation;

import com.example.edulearn.ENTITY.Evaluation.Enseignant.Question;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class ReponsePossibleDTO {
    private Integer id ;
    private String reponse ;
    private Boolean correcte ;
    private Integer question;
}
