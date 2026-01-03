package com.example.edulearn.CONTROLLER.Utilisateur;

import com.example.edulearn.DTO.Utilisateur.*;
import com.example.edulearn.ENTITY.Response.ServerResponse;
import com.example.edulearn.ENTITY.Status.StatusEnseignant;
import com.example.edulearn.ENTITY.Utilisateur.Eleve.Eleve;
import com.example.edulearn.ENTITY.Utilisateur.Eleve.Etablissement;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.ProfilEnseignant;
import com.example.edulearn.ENTITY.Utilisateur.Parent;
import com.example.edulearn.ENTITY.Utilisateur.Utilisateur;
import com.example.edulearn.REPOSITORY.Academie.EtablissementRepository;
import com.example.edulearn.REPOSITORY.Academie.FiliereRepository;
import com.example.edulearn.REPOSITORY.Academie.NiveauRepository;
import com.example.edulearn.REPOSITORY.Academie.SectionRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Controller
public class UtilisateurControllerImpl implements UtilisateurControllerInt{
    @Autowired
    private EnseignantRepository enseignantRepository ;
    @Autowired
    private StatusEnseignantRepository statusEnseignantRepository;
    @Autowired
    private DiplomeRepository diplomeRepository;
    @Autowired
    private ProfilEnseignantRepository profilEnseignantRepository;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private ParentRepository parentRepository;
    @Autowired
    private EleveRepository eleveRepository;
    @Autowired
    private EtablissementRepository etablissementRepository;
    @Autowired
    private NiveauRepository niveauRepository;
    @Autowired
    private FiliereRepository filiereRepository;
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    private static String folderFile = System.getProperty("user.dir")+"/src/main/resources/templates/platform/public/assets/file"; //chemin a déinir

    @Override
    public ResponseEntity<AuthData> loginUser(String auth) throws JsonProcessingException {

        AuthDTO authDTO = new ObjectMapper().readValue(auth, AuthDTO.class);

        Utilisateur utilisateur = this.utilisateurRepository.findByEmailAndPassword(authDTO.getEmail(), authDTO.getPassword());

        AuthData authData = new AuthData();

        if (Objects.nonNull(utilisateur)){

            authData.setId(utilisateur.getId());
            authData.setRole(utilisateur.getRole());

        }else{
            authData=null ;
        }

        return ResponseEntity.ok(authData);
    }

    @Override
    public ResponseEntity<List<Enseignant>> findAllEnseignant() {
        return ResponseEntity.ok(
                this.enseignantRepository.findAll(Sort.by(Sort.Direction.DESC , "id"))
        );
    }

    @Override
    public ResponseEntity<List<Enseignant>> findAllEnseignantByProfilEnseignant(Integer idProfil) {
        return ResponseEntity.ok(
                this.enseignantRepository.findByProfilEnseignant(
                        this.profilEnseignantRepository.findById(idProfil).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<List<Enseignant>> findAllEnseignantByStatus(Integer idStatus) {
        return ResponseEntity.ok(
                this.enseignantRepository.findByStatusEnseignant(
                        this.statusEnseignantRepository.findById(idStatus).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<List<Enseignant>> findAllEnseignantBySection(Integer idSection) {
        return ResponseEntity.ok(
                this.enseignantRepository.findBySection(
                        this.sectionRepository.findById(idSection).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<Long> countEnseignant() {
        return ResponseEntity.ok(
                this.enseignantRepository.count()
        );
    }

    @Override
    public ResponseEntity<Integer> createEnseignantAccount(String enseignant , MultipartFile photo , MultipartFile cv , MultipartFile diplome) throws IOException {
        EnseignantDTO enseignantDTO = new ObjectMapper().readValue(enseignant, EnseignantDTO.class);

        Enseignant enseignantDB = new Enseignant();

        enseignantDB.setNomComplet(enseignantDTO.getNomComplet());
        enseignantDB.setTelephone(enseignantDTO.getTelephone());
        enseignantDB.setEmail(enseignantDTO.getEmail());
        enseignantDB.setRole(2);
        enseignantDB.setBio(enseignantDTO.getBio());
        enseignantDB.setSpecialite(enseignantDTO.getSpecialite());
        enseignantDB.setAnneeexperience(enseignantDTO.getAnneeexperience());
        enseignantDB.setDateNaissance(LocalDate.parse(enseignantDTO.getDateNaissance()));
        enseignantDB.setEmail(enseignantDB.getEmail());
        enseignantDB.setPassword(enseignantDB.getPassword());
        enseignantDB.setTarifHoraire((enseignantDTO.getTarifHoraire()));
        enseignantDB.setStatus(false);
        enseignantDB.setLocalisation(enseignantDTO.getLocalisation());
        enseignantDB.setStatusEnseignant(this.statusEnseignantRepository.findById(1).orElse(null));//1- c'est l'id du statut en attente
        enseignantDB.setDateInscription(LocalDate.now());
        enseignantDB.setPassword(enseignantDTO.getPassword());

        enseignantDB.setDiplome(this.diplomeRepository.findById(enseignantDTO.getDiplome()).orElse(null));

        String fileName = "UNKNOW IMAGE";

        if (!photo.isEmpty()){
            //S'il n'y a pas de fichier
            fileName = photo.getOriginalFilename(); // le fichier prend le nom du client

            enseignantDB.setPhoto(fileName);

            System.out.println("le nom du fichier "+ fileName);

            Path path = Paths.get(folderFile,fileName);

            photo.transferTo(path);

            System.out.println("Photo enregistre en base de donnee");
        }

        if (!cv.isEmpty()){
            //S'il n'y a pas de fichier
            fileName = cv.getOriginalFilename(); // le fichier prend le nom du client

            enseignantDB.setCv(fileName);

            System.out.println("le nom du fichier "+ fileName);

            Path path = Paths.get(folderFile,fileName);

            cv.transferTo(path);

            System.out.println("cv enregistre en base de donnee");
        }

        if (!diplome.isEmpty()){
            //S'il n'y a pas de fichier
            fileName = diplome.getOriginalFilename(); // le fichier prend le nom du client

            enseignantDB.setDiplomeurl(fileName);

            System.out.println("le nom du fichier "+ fileName);

            Path path = Paths.get(folderFile,fileName);

            diplome.transferTo(path);

            System.out.println("diplome enregistre en base de donnee");
        }

        enseignantDB.setProfilEnseignant(
                this.profilEnseignantRepository.findById(enseignantDTO.getProfilEnseignant()).orElse(null)
        );

        enseignantDB.setSection(
                this.sectionRepository.findById(enseignantDTO.getSection()).orElse(null)
        );

        this.enseignantRepository.save(enseignantDB);

        ServerResponse serverResponse = new ServerResponse();

        serverResponse.setStatus(true);
        serverResponse.setMessage("L'enseignant a bien ete cree");

        Integer idEnseignant = this.enseignantRepository.findTopByOrderByIdDesc().getId();

        return ResponseEntity.ok(idEnseignant);
    }

    @Override
    public ResponseEntity<Enseignant> findEnseignantById(Integer id) {
        return ResponseEntity.ok(this.enseignantRepository.findById(id).orElse(null));
    }

    @Override
    public ResponseEntity<ServerResponse> changeEnseignantStatus(Integer id, Integer idStatus) {
        ServerResponse serverResponse = new ServerResponse();

        StatusEnseignant statusEnseignant = this.statusEnseignantRepository.findById(idStatus).orElse(null);

        Enseignant enseignant = this.enseignantRepository.findById(id).orElse(null);

        enseignant.setStatusEnseignant(statusEnseignant);

        this.enseignantRepository.save(enseignant);

        return ResponseEntity.ok(serverResponse);
    }

    @Override
    public ResponseEntity<Integer> createParent(String parent, MultipartFile photo , MultipartFile cni) throws IOException {
        ParentDTO parentDTO = new ObjectMapper().readValue(parent, ParentDTO.class);

        Parent parentDB = new Parent();

        parentDB.setEmail(parentDTO.getEmail());
        parentDB.setNomComplet(parentDTO.getNomComplet());
        parentDB.setProfession(parentDTO.getProfession());
        parentDB.setTelephone(parentDTO.getTelephone());
        parentDB.setLocalisation(parentDTO.getLocalisation());
        parentDB.setPassword(parentDTO.getPassword());
        parentDB.setRole(3);
        parentDB.setDateInscription(LocalDate.now());
        parentDB.setStatus(false);
        parentDB.setLocalisation(parentDTO.getLocalisation());

        String fileName = "UNKNOW FILE";
        if (!photo.isEmpty()){
            //S'il n'y a pas de fichier
            fileName = photo.getOriginalFilename(); // le fichier prend le nom du client

            parentDB.setPhoto(fileName);

            System.out.println("le nom du fichier "+ fileName);

            Path path = Paths.get(folderFile,fileName);

            photo.transferTo(path);

            System.out.println("photi enregistre en base de donnee");
        }

        String fileNameCNI ="UNKNOW CNI";
        if (!cni.isEmpty()){
            //S'il n'y a pas de fichier
            fileNameCNI = photo.getOriginalFilename(); // le fichier prend le nom du client

            parentDB.setCni(fileNameCNI);

            System.out.println("le nom du fichier "+ fileNameCNI);

            Path path = Paths.get(folderFile,fileNameCNI);

            cni.transferTo(path);

            System.out.println("diplome enregistre en base de donnee");
        }




        this.parentRepository.save(parentDB);

        Integer idParent = this.parentRepository.findTopByOrderByIdDesc().getId();

        ServerResponse serverResponse = new ServerResponse();

        serverResponse.setMessage("Parent supprime");
        serverResponse.setStatus(true);

        return ResponseEntity.ok(idParent);
    }

    @Override
    public ResponseEntity<Integer> createEleve(String eleve, MultipartFile photo) throws IOException {
        EleveDTO eleveDTO = new ObjectMapper().readValue(eleve , EleveDTO.class);
        Parent parent = this.parentRepository.findById(eleveDTO.getParent()).orElse(null);

        Eleve eleveDB = new Eleve();

        eleveDB.setNomComplet(eleveDTO.getNomComplet());
        eleveDB.setEmail(parent.getEmail());
        eleveDB.setRole(4);
        eleveDB.setTelephone(parent.getTelephone());
        eleveDB.setLocalisation(parent.getLocalisation());
        eleveDB.setPassword(eleveDTO.getPassword());
        eleveDB.setStatus(false);
        eleveDB.setNiveau(this.niveauRepository.findById(eleveDTO.getNiveau()).orElse(null));
        eleveDB.setFiliere(this.filiereRepository.findById(eleveDTO.getFiliere()).orElse(null));
        eleveDB.setRedoublant(Boolean.valueOf(eleveDTO.getRedoublant()));
        eleveDB.setParent(parent);
        eleveDB.setDateNaissance(LocalDate.parse(eleveDTO.getDateNaissance()));

        String fileName = "UNKNOW FILE";
        if (!photo.isEmpty()){
            //S'il n'y a pas de fichier
            fileName = photo.getOriginalFilename(); // le fichier prend le nom du client

            eleveDB.setPhoto(fileName);

            System.out.println("le nom du fichier "+ fileName);

            Path path = Paths.get(folderFile,fileName);

            photo.transferTo(path);

            System.out.println("photo enregistre en base de donnee");
        }

        this.eleveRepository.save(eleveDB);

        Eleve lastEleve = this.eleveRepository.findTopByOrderByIdDesc();

        Integer id = lastEleve.getId();

        return ResponseEntity.ok(id);
    }

    @Override
    public ResponseEntity<List<Eleve>> findByParent(Integer id) {
        return ResponseEntity.ok(
                this.eleveRepository.findByParent(
                        this.parentRepository.findById(id).orElse(null)
                )
        );
    }

    @Override
    public ResponseEntity<Long> countByParent(Integer id) {
        return ResponseEntity.ok(
                this.eleveRepository.countByParent(
                        this.parentRepository.findById(id).orElse(null)
                )
        );
    }
}
