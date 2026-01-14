package com.example.edulearn.CONTROLLER.Commentaire;

import com.example.edulearn.ENTITY.Commentaire.Commentaire;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/edulearn/api/commentaire")
@CrossOrigin("*")
public interface CommentaireControllerInt {
    @GetMapping("/all")
    ResponseEntity<List<Commentaire>> findAllCommentaire();
    @GetMapping("/all/byenseignant/{id}")
    ResponseEntity<List<Commentaire>> findAllCommentaireByEnseignant(@PathVariable Integer id);
    @GetMapping("/all/byparent/{id}")
    ResponseEntity<List<Commentaire>> findAllCommentaireByParent(@PathVariable Integer id);
    @PostMapping("/create")
    ResponseEntity<ServerResponse> createCommentaire(@Param("commentaire") String commentaire) throws JsonProcessingException;
    @PostMapping("/update")
    ResponseEntity<ServerResponse> updateCommentaire(@Param("commentaire") String commentaire) throws JsonProcessingException;
    @GetMapping("/delete/{id}")
    ResponseEntity<ServerResponse> deleteCommentaire(@PathVariable Integer id);


}
