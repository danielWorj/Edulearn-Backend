package com.example.edulearn.CONTROLLER.MarketPlace;

import com.example.edulearn.DTO.MarketPlace.RessourceDTO;
import com.example.edulearn.ENTITY.MarketPlace.Ressource;
import com.example.edulearn.ENTITY.MarketPlace.TypeResource;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.example.edulearn.REPOSITORY.MarketPlace.RessourceRepository;
import com.example.edulearn.REPOSITORY.MarketPlace.TypeRessourceRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.EnseignantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
public class MarketPlaceControllerImpl implements MarketPlaceControllerInt{
    @Autowired
    private RessourceRepository ressourceRepository;
    @Autowired
    private TypeRessourceRepository typeRessourceRepository ;
    @Autowired
    private EnseignantRepository enseignantRepository;
    private static String folderFile = System.getProperty("user.dir")+"/src/main/resources/templates/platform/public/assets/file"; //chemin a déinir


    @Override
    public ResponseEntity<List<Ressource>> findAllRessource() {
        return ResponseEntity.ok(
                this.ressourceRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
        );
    }

    @Override
    public ResponseEntity<List<Ressource>> findAllRessourceByEnseignant(Integer id) {
        return ResponseEntity.ok(
                this.ressourceRepository.findByEnseignant(
                        this.enseignantRepository.findById(id).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<ServerResponse> createRessource(String ressource , MultipartFile fichier) throws IOException {
        RessourceDTO ressourceDTO = new ObjectMapper().readValue(ressource, RessourceDTO.class);

        Ressource ressourceDB = new Ressource();

        ressourceDB.setNom(ressourceDTO.getNom());
        ressourceDB.setDescription(ressourceDTO.getDescription());
        ressourceDB.setPrix(ressourceDTO.getPrix());
        ressourceDB.setEnseignant(
                this.enseignantRepository.findById(ressourceDTO.getEnseignant()).orElse(null)
        );

        ressourceDB.setTypeResource(
                this.typeRessourceRepository.findById(ressourceDTO.getTypeResource()).orElse(null)
        );

        String fileName = "UNKNOW FILE";

        if (!fichier.isEmpty()){
            //S'il n'y a pas de fichier
            fileName = fichier.getOriginalFilename(); // le fichier prend le nom du client

            ressourceDB.setUrl(fileName);

            System.out.println("le nom du fichier "+ fileName);

            Path path = Paths.get(folderFile,fileName);

            fichier.transferTo(path);

            System.out.println("diplome enregistre en base de donnee");
        }

        this.ressourceRepository.save(ressourceDB);

        ServerResponse serverResponse = new ServerResponse();
        serverResponse.setStatus(true);
        return ResponseEntity.ok(serverResponse);
    }

    @Override
    public ResponseEntity<List<TypeResource>> findAllTypeRessource() {
        return ResponseEntity.ok(
                this.typeRessourceRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
        );
    }
}
