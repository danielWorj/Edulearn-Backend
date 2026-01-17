package com.example.edulearn.REPOSITORY.Commentaire;

import com.example.edulearn.ENTITY.Commentaire.Commentaire;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.example.edulearn.ENTITY.Utilisateur.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentaireRepository extends JpaRepository<Commentaire,Integer> {
    List<Commentaire> findByEnseignant(Enseignant enseignant);
    List<Commentaire> findByParent(Parent parent);
}
