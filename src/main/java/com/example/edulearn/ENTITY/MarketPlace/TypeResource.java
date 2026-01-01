package com.example.edulearn.ENTITY.MarketPlace;

import jakarta.annotation.Generated;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table
@Data
public class TypeResource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id ;
    private String nom ;
    
    
}
