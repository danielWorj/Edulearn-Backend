package com.example.edulearn.REPOSITORY.Repetition;

import com.example.edulearn.ENTITY.Repetition.MatiereRepetition;
import com.example.edulearn.ENTITY.Repetition.Repetition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatiereRepetitionRepository extends JpaRepository<MatiereRepetition,Integer> {
    List<MatiereRepetition> findByRepetition(Repetition repetition);
}
