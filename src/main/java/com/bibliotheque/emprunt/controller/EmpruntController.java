package com.bibliotheque.emprunt.controller;

import com.bibliotheque.emprunt.dto.EmpruntDTO;
import com.bibliotheque.emprunt.service.EmpruntService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Emprunts", description = "API de gestion des emprunts de livres")
public class EmpruntController {

    private final EmpruntService empruntService;

    @Operation(
        summary = "Lister tous les emprunts",
        description = "Retourne la liste complète de tous les emprunts enregistrés dans le système"
    )
    @ApiResponse(responseCode = "200", description = "Liste des emprunts retournée avec succès",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmpruntDTO.class)))
    @GetMapping
    public ResponseEntity<List<EmpruntDTO>> getAllEmprunts() {
        List<EmpruntDTO> emprunts = empruntService.findAll();
        return ResponseEntity.ok(emprunts);
    }

    @Operation(
        summary = "Obtenir un emprunt par ID",
        description = "Retourne les détails d'un emprunt spécifique selon son identifiant"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Emprunt trouvé",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmpruntDTO.class))),
        @ApiResponse(responseCode = "404", description = "Emprunt non trouvé", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmpruntDTO> getEmpruntById(
            @Parameter(description = "ID de l'emprunt", required = true, example = "1")
            @PathVariable Long id) {
        EmpruntDTO emprunt = empruntService.findById(id);
        return ResponseEntity.ok(emprunt);
    }

    @Operation(
        summary = "Créer un nouvel emprunt",
        description = "Crée un emprunt pour un livre et un emprunteur donnés. Le livre doit être disponible. La date de retour prévue est fixée à 15 jours."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Emprunt créé avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmpruntDTO.class))),
        @ApiResponse(responseCode = "400", description = "Livre non disponible ou données invalides", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EmpruntDTO> createEmprunt(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Données de l'emprunt. Seuls livreId et emprunteurId sont nécessaires.",
                required = true,
                content = @Content(schema = @Schema(implementation = EmpruntDTO.class))
            )
            @RequestBody EmpruntDTO dto) {
        EmpruntDTO created = empruntService.createEmprunt(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
        summary = "Enregistrer le retour d'un livre",
        description = "Marque un emprunt comme rendu. Si la date de retour dépasse la date prévue, le statut sera RETARD, sinon RENDU."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Retour enregistré avec succès",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = EmpruntDTO.class))),
        @ApiResponse(responseCode = "404", description = "Emprunt non trouvé", content = @Content),
        @ApiResponse(responseCode = "400", description = "Emprunt déjà rendu", content = @Content)
    })
    @PutMapping("/{id}/retour")
    public ResponseEntity<EmpruntDTO> retourEmprunt(
            @Parameter(description = "ID de l'emprunt à clôturer", required = true, example = "1")
            @PathVariable Long id) {
        EmpruntDTO updated = empruntService.retourEmprunt(id);
        return ResponseEntity.ok(updated);
    }

    @Operation(
        summary = "Supprimer un emprunt",
        description = "Supprime définitivement un emprunt de la base de données"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Emprunt supprimé avec succès"),
        @ApiResponse(responseCode = "404", description = "Emprunt non trouvé", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmprunt(
            @Parameter(description = "ID de l'emprunt à supprimer", required = true, example = "1")
            @PathVariable Long id) {
        empruntService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
