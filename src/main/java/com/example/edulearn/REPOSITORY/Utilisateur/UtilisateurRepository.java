package com.example.edulearn.REPOSITORY.Utilisateur;

import com.example.edulearn.ENTITY.Utilisateur.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur,Integer> {
    Utilisateur findByEmailAndPassword(String email , String password);
}
