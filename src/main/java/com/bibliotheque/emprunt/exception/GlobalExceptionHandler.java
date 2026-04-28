package com.bibliotheque.emprunt.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Gestionnaire global d'exceptions : intercepte toutes les exceptions
 * et retourne une réponse JSON uniforme avec le bon code HTTP.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ─── 404 NOT FOUND ────────────────────────────────────────────────────────

    @ExceptionHandler(EmpruntNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmpruntNotFound(
            EmpruntNotFoundException ex, HttpServletRequest request) {

        log.warn("Emprunt non trouvé : {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(LivreNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLivreNotFound(
            LivreNotFoundException ex, HttpServletRequest request) {

        log.warn("Livre non trouvé : {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(EmprunteurNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEmprunteurNotFound(
            EmprunteurNotFoundException ex, HttpServletRequest request) {

        log.warn("Emprunteur non trouvé : {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    // ─── 400 BAD REQUEST ──────────────────────────────────────────────────────

    @ExceptionHandler(LivreIndisponibleException.class)
    public ResponseEntity<ErrorResponse> handleLivreIndisponible(
            LivreIndisponibleException ex, HttpServletRequest request) {

        log.warn("Tentative d'emprunt sur un livre indisponible : {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "LIVRE_INDISPONIBLE", ex.getMessage(), request);
    }

    @ExceptionHandler(EmpruntDejaRenduException.class)
    public ResponseEntity<ErrorResponse> handleEmpruntDejaRendu(
            EmpruntDejaRenduException ex, HttpServletRequest request) {

        log.warn("Tentative de retour sur un emprunt déjà rendu : {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "EMPRUNT_DEJA_RENDU", ex.getMessage(), request);
    }

    // ─── 503 SERVICE UNAVAILABLE ──────────────────────────────────────────────

    @ExceptionHandler(ServiceExterneException.class)
    public ResponseEntity<ErrorResponse> handleServiceExterne(
            ServiceExterneException ex, HttpServletRequest request) {

        log.error("Service externe indisponible : {}", ex.getMessage());
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_INDISPONIBLE", ex.getMessage(), request);
    }

    // ─── 500 INTERNAL SERVER ERROR ────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("Erreur interne inattendue : {}", ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Une erreur interne est survenue. Veuillez réessayer plus tard.",
                request
        );
    }

    // ─── Méthode utilitaire ───────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String error, String message, HttpServletRequest request) {

        ErrorResponse body = new ErrorResponse(
                status.value(),
                error,
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }
}
