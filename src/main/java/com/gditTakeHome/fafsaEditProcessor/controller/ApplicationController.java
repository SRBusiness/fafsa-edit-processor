package com.gditTakeHome.fafsaEditProcessor.controller;

import com.gditTakeHome.fafsaEditProcessor.dto.ApplicationRequest;
import com.gditTakeHome.fafsaEditProcessor.dto.ValidationResponse;
import com.gditTakeHome.fafsaEditProcessor.service.EditProcessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final EditProcessorService editProcessorService;

    public ApplicationController(EditProcessorService editProcessorService) {
        this.editProcessorService = editProcessorService;
    }

    @PostMapping("/validate")
    public ResponseEntity<ValidationResponse> validate(@RequestBody ApplicationRequest request) {
        ValidationResponse response = editProcessorService.process(request);
        return ResponseEntity.ok(response);
    }
}
