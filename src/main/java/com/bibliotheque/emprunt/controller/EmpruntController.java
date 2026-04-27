package com.bibliotheque.emprunt.controller;

import com.bibliotheque.emprunt.dto.EmpruntDTO;
import com.bibliotheque.emprunt.service.EmpruntService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/emprunts")
@RequiredArgsConstructor
@Slf4j
public class EmpruntController {

    private final EmpruntService empruntService;

    @GetMapping
    public ResponseEntity<List<EmpruntDTO>> getAllEmprunts() {
        List<EmpruntDTO> emprunts = empruntService.findAll();
        return ResponseEntity.ok(emprunts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmpruntDTO> getEmpruntById(@PathVariable Long id) {
        EmpruntDTO emprunt = empruntService.findById(id);
        return ResponseEntity.ok(emprunt);
    }

    @PostMapping
    public ResponseEntity<EmpruntDTO> createEmprunt(@RequestBody EmpruntDTO dto) {
        EmpruntDTO created = empruntService.createEmprunt(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/retour")
    public ResponseEntity<EmpruntDTO> retourEmprunt(@PathVariable Long id) {
        EmpruntDTO updated = empruntService.retourEmprunt(id);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmprunt(@PathVariable Long id) {
        empruntService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
