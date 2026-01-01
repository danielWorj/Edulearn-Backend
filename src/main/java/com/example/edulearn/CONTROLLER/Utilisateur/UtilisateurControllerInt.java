package com.example.edulearn.CONTROLLER.Utilisateur;

import com.example.edulearn.DTO.Utilisateur.AuthData;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.example.edulearn.ENTITY.Utilisateur.Eleve.Eleve;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequestMapping("/edulearn/api/user")
@CrossOrigin("*")
public interface UtilisateurControllerInt {
    //AUTH
    @PostMapping("/login")
    ResponseEntity<AuthData> loginUser(@RequestParam("auth") String auth);
    //Enseignant
    @GetMapping("/enseignant/all")
    ResponseEntity<List<Enseignant>> findAllEnseignant();
    @GetMapping("/enseignant/all/byProfil/{idProfil}")
    ResponseEntity<List<Enseignant>> findAllEnseignantByProfilEnseignant(@PathVariable Integer idProfil);
    @GetMapping("/enseignant/all/byStatus/{idStatus}")
    ResponseEntity<List<Enseignant>> findAllEnseignantByStatus(@PathVariable Integer idStatus);
    @GetMapping("/enseignant/all/bySection/{idSection}")
    ResponseEntity<List<Enseignant>> findAllEnseignantBySection(@PathVariable Integer idSection);
    @GetMapping("/enseignant/count")
    ResponseEntity<Long> countEnseignant();
    @PostMapping("/enseignant/create")
    ResponseEntity<Integer> createEnseignantAccount(@RequestParam("enseignant") String enseignant,
                                                    @RequestParam("photo") MultipartFile photo,
                                                    @RequestParam("cv") MultipartFile cv,
                                                    @RequestParam("diplome") MultipartFile diplome) throws IOException;
    @GetMapping("/enseignant/findById/{id}")
    ResponseEntity<Enseignant> findEnseignantById(@PathVariable Integer id);

    @GetMapping("/enseignant/status/{id}/{idStatus}")
    ResponseEntity<ServerResponse> changeEnseignantStatus(@PathVariable Integer id, @PathVariable Integer idStatus);
    //Parent
    @PostMapping("/parent/create")
    ResponseEntity<Integer> createParent(@Param("parent") String parent,@RequestParam("photo") MultipartFile photo,@RequestParam("cni") MultipartFile cni) throws IOException;

    //Eleve
    @PostMapping("/eleve/create")
    ResponseEntity<Integer> createEleve(@Param("eleve") String eleve,@RequestParam("photo") MultipartFile photo) throws IOException;
    @GetMapping("/eleve/allbyparent/{id}")
    ResponseEntity<List<Eleve>> findByParent(@PathVariable Integer id);

    @GetMapping("/eleve/countbyParent/{id}")
    ResponseEntity<Long> countByParent(@PathVariable Integer id);

}
