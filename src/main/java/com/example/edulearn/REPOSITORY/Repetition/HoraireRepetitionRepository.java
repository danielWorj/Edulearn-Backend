package com.example.edulearn.REPOSITORY.Repetition;

import com.example.edulearn.ENTITY.Repetition.HoraireRepetition;
import com.example.edulearn.ENTITY.Repetition.Repetition;
import jakarta.persistence.Entity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface HoraireRepetitionRepository extends JpaRepository<HoraireRepetition,Integer> {
    List<HoraireRepetition> findByRepetition(Repetition repetition);
}
