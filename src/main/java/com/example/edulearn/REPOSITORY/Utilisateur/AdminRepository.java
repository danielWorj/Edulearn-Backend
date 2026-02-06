package com.example.edulearn.REPOSITORY.Utilisateur;

import com.example.edulearn.ENTITY.Utilisateur.Administrateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Administrateur, Integer> {
}
