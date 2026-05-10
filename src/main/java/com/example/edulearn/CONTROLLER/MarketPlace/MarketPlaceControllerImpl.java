package com.example.edulearn.CONTROLLER.MarketPlace;

import com.example.edulearn.DTO.MarketPlace.RessourceDTO;
import com.example.edulearn.DTO.MarketPlace.SupportDTO;
import com.example.edulearn.ENTITY.MarketPlace.New.Support;
import com.example.edulearn.ENTITY.MarketPlace.Ressource;
import com.example.edulearn.ENTITY.MarketPlace.TypeResource;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.example.edulearn.REPOSITORY.Academie.FiliereRepository;
import com.example.edulearn.REPOSITORY.Academie.MatiereRepository;
import com.example.edulearn.REPOSITORY.Academie.NiveauRepository;
import com.example.edulearn.REPOSITORY.MarketPlace.RessourceRepository;
import com.example.edulearn.REPOSITORY.MarketPlace.SupportRepository;
import com.example.edulearn.REPOSITORY.MarketPlace.TypeRessourceRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.EnseignantRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@Controller
public class MarketPlaceControllerImpl implements MarketPlaceControllerInt{
    @Autowired
    private RessourceRepository ressourceRepository;
    @Autowired
    private TypeRessourceRepository typeRessourceRepository ;
    @Autowired
    private EnseignantRepository enseignantRepository;
    @Autowired
    private SupportRepository supportRepository;
    @Autowired
    private MatiereRepository matiereRepository;
    @Autowired
    private NiveauRepository niveauRepository;
    @Autowired
    private FiliereRepository filiereRepository;



    private static ObjectMapper objectmapper = new ObjectMapper();
    private static String folderFile = System.getProperty("user.dir")+"/src/main/resources/templates/platform/public/assets/file"; //chemin a déinir

    private static String folderFileSupport = System.getProperty("user.dir")+"/src/main/resources/templates/educia/public/assets/file/support"; //chemin a déinir


    @Override
    public ResponseEntity<List<Support>> findAllSupport() {
        return ResponseEntity.ok(this.supportRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
    }

    @Override
    public ResponseEntity<Support> findSupportById(Integer id) {
        return ResponseEntity.ok(this.supportRepository.findById(id).orElse(null));
    }

    @Override
    public ResponseEntity<ServerResponse> createSupport(String support, MultipartFile file) throws IOException {
        SupportDTO supportDTO = objectmapper.readValue(support, SupportDTO.class);

        Support supportDB = new Support();

        System.out.println("Le dto obtenu est : "+ supportDTO.toString());

        supportDB.setTitle(supportDTO.getTitle());
        supportDB.setMatiere(this.matiereRepository.findById(supportDTO.getMatiere()).orElse(null));
        supportDB.setDate(LocalDate.now());
        supportDB.setStatut(true);
        supportDB.setResume(supportDTO.getResume());
        supportDB.setEnseignant(this.enseignantRepository.findById(supportDTO.getEnseignant()).orElse(null));
        supportDB.setFiliere(this.filiereRepository.findById(supportDTO.getFiliere()).orElse(null));
        supportDB.setNiveau(this.niveauRepository.findById(supportDTO.getNiveau()).orElse(null));
        supportDB.setType(this.typeRessourceRepository.findById(supportDTO.getType()).orElse(null));
        supportDB.setMatiere(this.matiereRepository.findById(supportDTO.getMatiere()).orElse(null));
        supportDB.setPrix(supportDTO.getPrix());
        this.supportRepository.save(supportDB);

        String fileName = "";
        if (!file.isEmpty()){
            //S'il n'y a pas de fichier
            fileName = file.getOriginalFilename(); // le fichier prend le nom du client

            System.out.println("le nom du fichier "+ fileName);

            supportDB.setFile(fileName);

            Path path = Paths.get(folderFileSupport,fileName);

            file.transferTo(path);

            System.out.println("support enregistre en base de donnee");
        }


        return ResponseEntity.ok(new ServerResponse("Supportr creee avec succes", true));
    }

    @Override
    public ResponseEntity<ServerResponse> updateSupport(String support) {
        return null;
    }

    @Override
    public ResponseEntity<ServerResponse> deleteSupport(Integer id) {
        this.supportRepository.deleteById(id);
        return ResponseEntity.ok(new ServerResponse("Support supprime", true));
    }

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
