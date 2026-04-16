package com.example.edulearn.REPOSITORY.Utilisateur;


import com.example.edulearn.ENTITY.Status.StatusEnseignant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusEnseignantRepository extends JpaRepository<StatusEnseignant, Integer> {
}
