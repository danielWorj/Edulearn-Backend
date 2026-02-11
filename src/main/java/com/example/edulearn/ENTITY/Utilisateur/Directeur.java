package com.example.edulearn.ENTITY.Utilisateur;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table
@Data
@DiscriminatorValue(value = "parent")
public class Directeur extends Utilisateur{
    private String profession ;
    private String cni ;

    public Directeur() {
    }
}
