package com.example.edulearn.REPOSITORY.Repetition;


import com.example.edulearn.ENTITY.Repetition.New.Offre;
import com.example.edulearn.ENTITY.Utilisateur.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OffreRepository extends JpaRepository<Offre,Integer> {
    Offre findTopByOrderByIdDesc();
    List<Offre> findByParent(Parent parent);

}
