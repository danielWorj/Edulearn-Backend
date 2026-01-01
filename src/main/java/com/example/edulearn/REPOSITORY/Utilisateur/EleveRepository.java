package com.example.edulearn.REPOSITORY.Utilisateur;

import com.example.edulearn.ENTITY.Utilisateur.Eleve.Eleve;
import com.example.edulearn.ENTITY.Utilisateur.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EleveRepository extends JpaRepository<Eleve,Integer> {
    List<Eleve> findByParent(Parent parent);

    Eleve findTopByOrderByIdDesc();
    Long countByParent(Parent parent);

}
