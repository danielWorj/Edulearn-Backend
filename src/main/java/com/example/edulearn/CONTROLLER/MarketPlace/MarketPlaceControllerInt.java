package com.example.edulearn.CONTROLLER.MarketPlace;

import com.example.edulearn.ENTITY.MarketPlace.Ressource;
import com.example.edulearn.ENTITY.MarketPlace.TypeResource;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequestMapping("/edulearn/api/marketplace")
@CrossOrigin("*")
public interface MarketPlaceControllerInt {
    @GetMapping("/all")
    ResponseEntity<List<Ressource>> findAllRessource();
    @GetMapping("/all/byenseignant/{id}")
    ResponseEntity<List<Ressource>> findAllRessourceByEnseignant(@PathVariable Integer id);
    @PostMapping("/create")
    ResponseEntity<ServerResponse> createRessource(@RequestParam("ressource") String ressource , @RequestParam("fichier")MultipartFile fichier) throws IOException;


    //Type Ressource
    @GetMapping("/typeRessource/all")
    ResponseEntity<List<TypeResource>> findAllTypeRessource();
}
