package com.example.edulearn.CONTROLLER.MarketPlace;

import com.example.edulearn.ENTITY.MarketPlace.New.Support;
import com.example.edulearn.ENTITY.MarketPlace.Ressource;
import com.example.edulearn.ENTITY.MarketPlace.TypeResource;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequestMapping("/edulearn/api/marketplace")
@CrossOrigin("*")
public interface MarketPlaceControllerInt {


    //NEW
    @GetMapping("/new/all")
    ResponseEntity<List<Support>> findAllSupport();
    @GetMapping("/new/findbyid/{id}")
    ResponseEntity<Support> findSupportById(@PathVariable Integer id);
    @PostMapping("/new/create")
    ResponseEntity<ServerResponse> createSupport(@RequestParam("support") String support , @RequestParam("file") MultipartFile file) throws IOException;
    @PostMapping("/new/update")
    ResponseEntity<ServerResponse> updateSupport(@RequestParam("support") String support);
    @GetMapping("/new/delete/{id}")
    ResponseEntity<ServerResponse> deleteSupport(@PathVariable Integer id);





    ///ANCIENS
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
