package com.example.edulearn.REPOSITORY.Evaluation;

import com.example.edulearn.ENTITY.Evaluation.Enseignant.Question;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.ReponsePossible;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReponsePossibleRepository extends JpaRepository<ReponsePossible,Integer> {
    List<ReponsePossible> findByQuestion(Question question);
    ReponsePossible findByQuestionAndCorrecteIsTrue(Question question);
}
