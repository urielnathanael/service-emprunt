package com.bibliotheque.emprunt.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Structure de réponse uniforme pour toutes les erreurs de l'API.
 */
@Schema(description = "Réponse d'erreur standard de l'API")
public class ErrorResponse {

    @Schema(description = "Code HTTP de l'erreur", example = "404")
    private int status;

    @Schema(description = "Type d'erreur", example = "NOT_FOUND")
    private String error;

    @Schema(description = "Message descriptif de l'erreur", example = "Emprunt introuvable avec l'id : 5")
    private String message;

    @Schema(description = "Chemin de la requête ayant causé l'erreur", example = "/api/emprunts/5")
    private String path;

    @Schema(description = "Horodatage de l'erreur", example = "2026-04-27T16:35:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public ErrorResponse(int status, String error, String message, String path) {
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.path      = path;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public int getStatus()             { return status; }
    public String getError()           { return error; }
    public String getMessage()         { return message; }
    public String getPath()            { return path; }
    public LocalDateTime getTimestamp(){ return timestamp; }
}
