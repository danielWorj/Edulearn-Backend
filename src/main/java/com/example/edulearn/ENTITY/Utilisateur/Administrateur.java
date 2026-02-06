package com.example.edulearn.ENTITY.Utilisateur;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table
@Data
@DiscriminatorValue(value = "admin")
public class Administrateur extends Utilisateur{
    private String niveau ;
    public Administrateur() {

    }
}
