package com.example.edulearn.CONTROLLER.IA;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/ia/")
@CrossOrigin("*")
public interface IaControllerInt {
    @GetMapping("/assistant")
    ResponseEntity<String> assistantIA();

    @PostMapping("/assistant")
    ResponseEntity<String> assistanceTextuelle(@RequestParam("prompt") String prompt);
}
