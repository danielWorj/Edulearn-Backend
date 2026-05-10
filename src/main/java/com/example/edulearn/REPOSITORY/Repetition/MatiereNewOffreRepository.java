package com.example.edulearn.REPOSITORY.Repetition;

import com.example.edulearn.ENTITY.Repetition.New.MatiereNewOffre;
import com.example.edulearn.ENTITY.Repetition.New.Offre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatiereNewOffreRepository extends JpaRepository<MatiereNewOffre,Integer> {
    List<MatiereNewOffre> findByOffre(Offre offre);
}
