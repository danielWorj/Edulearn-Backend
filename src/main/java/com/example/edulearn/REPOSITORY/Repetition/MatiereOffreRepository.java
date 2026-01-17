package com.example.edulearn.REPOSITORY.Repetition;

import com.example.edulearn.ENTITY.Repetition.MatiereOffre;
import com.example.edulearn.ENTITY.Repetition.MatiereRepetition;
import com.example.edulearn.ENTITY.Repetition.OffreRepetition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatiereOffreRepository extends JpaRepository<MatiereOffre,Integer> {
    List<MatiereOffre> findByOffreRepetition(OffreRepetition offreRepetition);
}
