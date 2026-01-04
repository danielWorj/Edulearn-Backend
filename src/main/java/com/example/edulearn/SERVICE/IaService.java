package com.example.edulearn.SERVICE;



import com.example.edulearn.ENTITY.Repetition.OffreRepetition;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class IaService {
    public final ChatClient chatClient;

    public IaService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateResumeForJOffre(OffreRepetition offre) {

        String synthesizedData = synthesizeOffreForPrompt(offre);

        String fullPrompt = synthesizedData +
                "\n\nPeux-tu me faire un résumé concis et attractif de cette offre de repetition " +
                "en mettant en avant les points clés qui intéresseraient un enseignant  " +
                "Le résumé doit faire environ 5-6 phrases.";

        var completion = Map.of("completion", Objects.requireNonNull(chatClient.prompt().user(fullPrompt).call().content()));

        return completion.get("completion");


    }

    public String getScoreMatchingOffreEnseignant(OffreRepetition job, String cvText){
        String synthesizedData = synthesizeOffreForPrompt(job);

        String fullPrompt =  synthesizedData.toString()+
                "\n\n Donne moi le score pour que ce cv: + " + cvText +
                " soit un bon cv pour cet offre de repetition : "+ synthesizedData +". Tu me renvoi un nombre entre 0 et 100.   ";

        var completion = Map.of("completion", Objects.requireNonNull(chatClient.prompt().user(fullPrompt).call().content()));

        return completion.get("completion");
    }



    public String pointsAmeliorationCvAndOffre(OffreRepetition job, String cvText){
        String synthesizedData = synthesizeOffreForPrompt(job);

        String fullPrompt =  synthesizedData.toString()+
                "\n\n Donne moi une liste de 05 points a ameliorer sur le cv suivant : + " + cvText +
                " Pour que ce cv soit un bon cv pour cet offre d'emploi. Tu me renvoi un Json de la liste de ces points    " +
                "L'objet pointAmelioration a un seul attribut intitule. Il n'y a pas de texte avant ou apres le json de la reponse. Mets juste le contenu du json. ";

        var completion = Map.of("completion", Objects.requireNonNull(chatClient.prompt().user(fullPrompt).call().content()));

        return completion.get("completion");
    }

    public String assistantTextuel(String prompt){
        var completion = Map.of("completion", Objects.requireNonNull(chatClient.prompt().user(prompt).call().content()));

        return completion.get("completion");
    }



    public String synthesizeOffreForPrompt(OffreRepetition job) {

        StringBuilder prompt = new StringBuilder();

        // 1. Informations principales
        prompt.append("Voici une offre de repetition à analyser :\n\n");
        prompt.append("**Titre du poste :** ").append(job.getIntitule()).append("\n");

        // 2. Description
        prompt.append("**Description du poste :**\n");
        prompt.append(job.getBio()).append("\n\n");

        return prompt.toString();
    }




}
