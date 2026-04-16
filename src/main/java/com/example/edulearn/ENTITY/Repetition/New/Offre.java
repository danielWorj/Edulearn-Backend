package com.example.edulearn.ENTITY.Repetition.New;

import com.example.edulearn.ENTITY.Utilisateur.Parent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Offre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String bio;
    private String budget ;
    private Integer frequence;
    private String duree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    private Parent parent;
}
