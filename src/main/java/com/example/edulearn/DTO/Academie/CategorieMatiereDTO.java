package com.example.edulearn.DTO.Academie;

import com.example.edulearn.ENTITY.Academie.Section;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class CategorieMatiereDTO {

    private Integer id ;
    private String intitule ;
    private Integer section ;
}
