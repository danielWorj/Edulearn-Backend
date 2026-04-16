package com.example.edulearn.REPOSITORY.Evaluation;

import com.example.edulearn.ENTITY.Academie.Matiere;
import com.example.edulearn.ENTITY.Evaluation.Eleve.Evaluation;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.Composition;
import com.example.edulearn.ENTITY.Utilisateur.Eleve.Eleve;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation,Integer> {
    @Query(value = "SELECT e FROM Evaluation e JOIN e.composition c JOIN c.repetition r JOIN r.offreRepetition o JOIN o.eleve el WHERE el=:eleve")
    List<Evaluation> findByEleve(@Param("eleve") Eleve eleve);


    @Query(value = "SELECT e FROM Evaluation e JOIN e.composition c JOIN c.matiere m JOIN c.repetition r JOIN r.offreRepetition o JOIN o.eleve el WHERE el=:eleve AND m=:matiere")
    List<Evaluation> findByEleveAndMatiere(@Param("eleve") Eleve eleve , @Param("matiere")Matiere matiere);
    Evaluation findByComposition(Composition composition);
    Evaluation findTopByOrderByIdDesc();
}
