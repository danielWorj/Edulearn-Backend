package com.example.edulearn.CONTROLLER.IA;

import com.example.edulearn.DTO.IA.PromptDTO;
import com.example.edulearn.ENTITY.Repetition.OffreRepetition;
import com.example.edulearn.ENTITY.Utilisateur.Enseignant.Enseignant;
import com.example.edulearn.REPOSITORY.Repetition.OffreRepetitionRepository;
import com.example.edulearn.REPOSITORY.Utilisateur.EnseignantRepository;
import com.example.edulearn.SERVICE.IaService;
import com.example.edulearn.SERVICE.MatchingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class IAControllerImpl implements IaControllerInt{
    @Autowired
    private IaService iaService;
    @Autowired
    private MatchingService matchingService;
    @Autowired
    private OffreRepetitionRepository offreRepetitionRepository;
    @Autowired
    private EnseignantRepository enseignantRepository;

    @Override
    public ResponseEntity<String> assistantIA() {
        String prompt = "J'ai un devoir sur la traite negriere au cameroun , fais moi ce devoir succintement.";
        //String reponse = this.iaService.assistantTextuel(prompt);
        //System.out.println(reponse);
        String rep = "**Introduction**\n" +
                "\n" +
                "La traite négrière est un chapitre sombre de l’histoire humaine qui a eu des répercussions profondes sur les sociétés africaines, y compris celle du Cameroun. Ce phénomène a débuté au XVe siècle et a perduré jusqu’au XIXe siècle, marquant des millions de vies et transformant les structures sociales, économiques et culturelles des pays touchés.\n" +
                "\n" +
                "**Contexte historique**\n" +
                "\n" +
                "Au Cameroun, la traite négrière a été facilitée par la demande croissante de main-d'œuvre dans les colonies européennes, notamment en Amérique. Les Européens, en particulier les Portugais, les Britanniques et les Français, ont établi des comptoirs le long de la côte atlantique pour commercer avec les chefs locaux. Ces échanges ont souvent impliqué des raids et des captures de populations locales.\n" +
                "\n" +
                "**Modes de la traite**\n" +
                "\n" +
                "La traite au Cameroun s’est déroulée principalement par le biais de deux modes : la traite intérieure et la traite côtière. La traite intérieure impliquait des captures par des groupes ethniques concurrents ou des alliances entre chefs locaux. Les esclaves étaient ensuite transportés vers la côte pour être vendus aux négriers.\n" +
                "\n" +
                "**Conséquences de la traite négrière**\n" +
                "\n" +
                "Les conséquences de la traite négrière au Cameroun ont été dévastatrices. Sur le plan démographique, elle a entraîné une perte significative de population, avec des millions d'hommes, de femmes et d'enfants réduits en esclavage. Sur le plan social, les structures familiales et communautaires ont été profondément affectées, provoquant des conflits entre les groupes ethniques. Économiquement, la traite a favorisé l’essor de certaines régions côtières au détriment de l’intérieur du pays.\n" +
                "\n" +
                "**Résistance et abolition**\n" +
                "\n" +
                "Malgré les horreurs de la traite, il y a eu des formes de résistance, tant sur le terrain que par des révoltes. Au XIXe siècle, avec la montée des mouvements abolitionnistes en Europe, la traite négrière a commencé à être remise en question. Le Cameroun a vu la fin de la traite avec l’arrivée des puissances coloniales, qui ont mis en place des lois contre l’esclavage, même si les pratiques esclavagistes ont persisté sous d’autres formes.\n" +
                "\n" +
                "**Conclusion**\n" +
                "\n" +
                "La traite négrière au Cameroun est un sujet complexe qui mérite une attention particulière. Ses impacts se font encore sentir aujourd'hui, tant sur le plan social qu'économique. Comprendre cette période est essentiel pour appréhender les défis contemporains du pays et favoriser un dialogue autour de l’histoire et de la mémoire collective.\n" +
                "\n" +
                "---\n" +
                "\n" +
                "N'hésite pas à développer certains points selon les exigences de ton devoir et à ajouter des références si nécessaire.";

            System.out.println(rep);
        return ResponseEntity.ok(rep);
    }

    @Override
    public ResponseEntity<String> assistanceTextuelle(String prompt) throws JsonProcessingException {
        PromptDTO promptDTO = new ObjectMapper().readValue(prompt, PromptDTO.class);
        String reponse = this.iaService.assistantTextuel(promptDTO.getPrompt());
        System.out.println(reponse);
        return ResponseEntity.ok(reponse);

    }

    @Override
    public ResponseEntity<String> getScoreCorrespondance() {
        OffreRepetition job = this.offreRepetitionRepository.findById(2).orElse(null);
        Enseignant enseignant = this.enseignantRepository.findById(2).orElse(null);

        Integer score = this.matchingService.calculateMatching(enseignant,job);
        return ResponseEntity.ok("Le score obtenu est :"+score);
    }
}
