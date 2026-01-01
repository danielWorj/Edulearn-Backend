package com.example.edulearn.REPOSITORY.Evaluation;

import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.Composition;
import com.example.edulearn.ENTITY.Repetition.Repetition;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompositionRepository extends JpaRepository<Composition,Integer> {

    Composition findTopByOrderByIdDesc();

    List<Composition> findByEnseignant(Enseignant enseignant);
    List<Composition> findByMatiere(Matiere matiere);

}
