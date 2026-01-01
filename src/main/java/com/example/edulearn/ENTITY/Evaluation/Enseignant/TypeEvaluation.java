package com.example.edulearn.ENTITY.Evaluation.Enseignant;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class TypeEvaluation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;
    private String intitule ; // CC / SN / TP
}
