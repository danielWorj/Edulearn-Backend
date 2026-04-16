package com.example.edulearn.REPOSITORY.MarketPlace;

import com.example.edulearn.ENTITY.MarketPlace.Ressource;
import com.example.edulearn.ENTITY.MarketPlace.TypeResource;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RessourceRepository extends JpaRepository<Ressource,Integer> {
    List<Ressource> findByTypeResource(TypeResource typeResource);
    List<Ressource> findByEnseignant(Enseignant enseignant);
}
