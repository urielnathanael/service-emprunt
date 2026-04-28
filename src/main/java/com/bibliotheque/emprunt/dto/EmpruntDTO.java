package com.bibliotheque.emprunt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Données d'un emprunt de livre")
public class EmpruntDTO {

    @Schema(description = "Identifiant unique de l'emprunt", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Date à laquelle l'emprunt a été effectué", example = "2026-04-27", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate dateEmprunt;

    @Schema(description = "Date prévue pour le retour (automatiquement fixée à J+15)", example = "2026-05-12", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate dateRetourPrevue;

    @Schema(description = "Date réelle du retour du livre (null si pas encore rendu)", example = "2026-05-10", nullable = true)
    private LocalDate dateRetourReelle;

    @Schema(description = "Identifiant du livre emprunté", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long livreId;

    @Schema(description = "Identifiant de l'emprunteur", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long emprunteurId;

    @Schema(description = "Statut de l'emprunt : EN_COURS, RENDU, RETARD", example = "EN_COURS", accessMode = Schema.AccessMode.READ_ONLY)
    private String statut;
}
