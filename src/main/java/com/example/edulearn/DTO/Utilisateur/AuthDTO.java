package com.example.edulearn.DTO.Utilisateur;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class AuthDTO {
    private String email ;
    private String password ;
}
