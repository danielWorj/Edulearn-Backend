package com.example.edulearn.DTO.Commentaire;

import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.example.edulearn.ENTITY.Utilisateur.Parent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class CommentaireDTO {
    private Integer id ;
    @Lob
    private String contenu ;
    private Integer enseignant;
    private Integer parent;
}
