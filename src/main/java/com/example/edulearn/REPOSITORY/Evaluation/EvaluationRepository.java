package com.example.edulearn.REPOSITORY.Evaluation;

import com.example.edulearn.ENTITY.Evaluation.Eleve.Evaluation;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.Composition;
import com.example.edulearn.ENTITY.Utilisateur.Eleve.Eleve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation,Integer> {
    @Query(value = "SELECT e FROM Evaluation e JOIN e.composition c JOIN c.repetition r JOIN r.offreRepetition o JOIN o.eleve el WHERE el=:eleve")
    List<Evaluation> findByEleve(Eleve eleve);
    Evaluation findByComposition(Composition composition);
    Evaluation findTopByOrderByIdDesc();
}
