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

    List<Composition> findByMatiere(Matiere matiere);
    @Query(value = "SELECT c FROM Composition c JOIN c.repetition r JOIN r.offreRepetition o JOIN o.eleve e WHERE e.id=:id AND c.archived=true")
    List<Composition> findAllCompositionNonArchivedByEleve(@Param("id") Integer id);

    @Query(value = "SELECT c FROM Composition c JOIN c.matiere m JOIN c.repetition r WHERE m.id=:id AND c.archived=true")
    List<Composition> findAllCompositionNonArchivedByMatiere(@Param("id") Integer id);


}
