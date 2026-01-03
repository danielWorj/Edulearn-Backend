package com.example.edulearn.REPOSITORY.Evaluation;

import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.Composition;
import com.example.edulearn.ENTITY.Repetition.Repetition;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompositionRepository extends JpaRepository<Composition,Integer> {

    Composition findTopByOrderByIdDesc();

    @Query(value = "SELECT c FROM Composition c JOIN c.repetition r JOIN r.enseignant e WHERE e=:en")
    List<Composition> findByEnseignant(@Param("en") Enseignant en);
    List<Composition> findByRepetition(Repetition repetition);


}
