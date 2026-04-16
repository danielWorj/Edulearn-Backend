package com.example.edulearn.REPOSITORY.Evaluation;

import com.example.edulearn.ENTITY.Evaluation.Eleve.Evaluation;
import com.example.edulearn.ENTITY.Evaluation.Eleve.ReponseEleve;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReponseEleveRepository extends JpaRepository<ReponseEleve,Integer> {
    ReponseEleve findTopByOrderByIdDesc();
    List<ReponseEleve> findByEvaluation(Evaluation evaluation);
    ReponseEleve findByQuestion(Question question);
    List<ReponseEleve> findByEvaluationAndQuestion(Evaluation evaluation, Question question);
}
