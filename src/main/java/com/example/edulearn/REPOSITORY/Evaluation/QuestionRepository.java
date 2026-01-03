package com.example.edulearn.REPOSITORY.Evaluation;

import com.example.edulearn.ENTITY.Evaluation.Enseignant.Composition;
import com.example.edulearn.ENTITY.Evaluation.Enseignant.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question,Integer> {
    List<Question> findByComposition(Composition composition);

    Question findTopByOrderByIdDesc();
}
