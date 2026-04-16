package com.example.edulearn.DTO.Evaluation;

import com.example.edulearn.ENTITY.Evaluation.Enseignant.Composition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class QuestionDTO {
    private Integer id ;
    private String enonce ;
    private Integer composition ;
    private Integer points ;
}
