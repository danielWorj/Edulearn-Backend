package com.example.edulearn.ENTITY.Academie;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table
public class CategorieMatiere {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;
    private String intitule ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Section section ;

    public CategorieMatiere(String intitule, Section section) {
        this.intitule = intitule;
        this.section = section;
    }

    public CategorieMatiere() {
    }
}
