package com.example.edulearn.CONTROLLER.IA;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/edulearn/api/ia/")
@CrossOrigin("*")
public interface IaControllerInt {
    @GetMapping("/assistant")
    ResponseEntity<String> assistantIA();
    @PostMapping("/assistant-textuel")
    ResponseEntity<String> assistanceTextuelle(@RequestParam("prompt") String prompt) throws JsonProcessingException;
}
