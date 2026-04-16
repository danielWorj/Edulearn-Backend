package com.example.edulearn.ENTITY.Commentaire;

import jakarta.persistence.*;

@Entity
@Table
public class TypeCommentaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;
    private String intitule ;
}
