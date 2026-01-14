package com.example.edulearn.CONTROLLER.Commentaire;

import com.example.edulearn.DTO.Commentaire.CommentaireDTO;
import com.example.edulearn.ENTITY.Commentaire.Commentaire;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.example.edulearn.REPOSITORY.Commentaire.CommentaireRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.EnseignantRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.ParentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class CommentaireControllerImpl implements CommentaireControllerInt {
    @Autowired
    private CommentaireRepository commentaireRepository;
    @Autowired
    private EnseignantRepository enseignantRepository;
    @Autowired
    private ParentRepository parentRepository;

    @Override
    public ResponseEntity<List<Commentaire>> findAllCommentaire() {
        return ResponseEntity.ok(this.commentaireRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
    }

    @Override
    public ResponseEntity<List<Commentaire>> findAllCommentaireByEnseignant(Integer id) {
        return ResponseEntity.ok(this.commentaireRepository.findbyEnseignant(
            this.enseignantRepository.findById(id).orElse(null)
        ));
    }

    @Override
    public ResponseEntity<List<Commentaire>> findAllCommentaireByParent(Integer id) {
        return ResponseEntity.ok(this.commentaireRepository.findByParent(
                this.parentRepository.findById(id).orElse(null)
        ));
    }

    @Override
    public ResponseEntity<ServerResponse> createCommentaire(String commentaire) throws JsonProcessingException {
        CommentaireDTO commentaireDTO = new ObjectMapper().readValue(commentaire, CommentaireDTO.class);

        Commentaire commentaireDB = new Commentaire();

        commentaireDB.setContenu(commentaireDTO.getContenu());
        commentaireDB.setEnseignant(this.enseignantRepository.findById(commentaireDTO.getEnseignant()).orElse(null));
        commentaireDB.setParent(this.parentRepository.findById(commentaireDTO.getParent()).orElse(null));

        this.commentaireRepository.save(commentaireDB);
        return ResponseEntity.ok(new ServerResponse("Un commentaire a ete creee", true));

    }

    @Override
    public ResponseEntity<ServerResponse> updateCommentaire(String commentaire) throws JsonProcessingException {
        CommentaireDTO commentaireDTO = new ObjectMapper().readValue(commentaire, CommentaireDTO.class);

        Commentaire commentaireDB = new Commentaire();

        commentaireDB.setContenu(commentaireDTO.getContenu());
        commentaireDB.setEnseignant(this.enseignantRepository.findById(commentaireDTO.getEnseignant()).orElse(null));
        commentaireDB.setParent(this.parentRepository.findById(commentaireDTO.getParent()).orElse(null));

        this.commentaireRepository.save(commentaireDB);
        return ResponseEntity.ok(new ServerResponse("Un commentaire a ete creee", true));

    }

    @Override
    public ResponseEntity<ServerResponse> deleteCommentaire(Integer id) {
        this.commentaireRepository.deleteById(id);
        return ResponseEntity.ok(new ServerResponse("Un commentaire a ete creee", true));
    }
}
