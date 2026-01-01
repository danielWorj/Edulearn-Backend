package com.example.edulearn.REPOSITORY.Evaluation;

import com.example.edulearn.ENTITY.Evaluation.Eleve.Evaluation;
import com.example.edulearn.ENTITY.Utilisateur.Eleve.Eleve;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluationRepository extends JpaRepository<Evaluation,Integer> {
    List<Evaluation> findByEleve(Eleve eleve);

    Evaluation findTopByOrderByIdDesc();
}
