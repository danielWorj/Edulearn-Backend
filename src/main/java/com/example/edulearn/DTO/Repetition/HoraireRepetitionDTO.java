package com.example.edulearn.DTO.Repetition;

import com.example.edulearn.ENTITY.Repetition.Repetition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.time.LocalTime;

@Data
public class HoraireRepetitionDTO {
    private Integer id ;
    private String jour ;
    private String timeStart;
    private String timeEnd;
    private Integer repetition;
}
