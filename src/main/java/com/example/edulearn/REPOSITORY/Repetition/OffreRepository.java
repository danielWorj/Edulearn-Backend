package com.example.edulearn.REPOSITORY.Repetition;


import com.example.edulearn.ENTITY.Repetition.New.Offre;
import com.example.edulearn.ENTITY.Utilisateur.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OffreRepository extends JpaRepository<Offre,Integer> {
    Offre findTopByOrderByIdDesc();

}
