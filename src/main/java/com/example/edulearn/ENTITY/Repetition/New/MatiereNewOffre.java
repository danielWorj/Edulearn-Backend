package com.example.edulearn.ENTITY.Repetition.New;

import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Repetition.OffreRepetition;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data

public class MatiereNewOffre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Offre offre ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Matiere matiere ;

}
