package com.example.edulearn.DTO.Utilisateur;

import com.example.edulearn.ENTITY.Utilisateur.Utilisateur;
import lombok.Data;
import org.springframework.data.jpa.repository.JpaRepository;

@Data
public class ParentDTO extends UtilisateurDTO {
    private String profession ;
    private String cni ;


}
