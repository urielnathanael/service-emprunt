package com.bibliotheque.emprunt.exception;

/**
 * Exception levée quand un microservice externe (livre ou emprunteur) est inaccessible.
 */
public class ServiceExterneException extends RuntimeException {

    private final String nomService;

    public ServiceExterneException(String nomService, String message) {
        super("Erreur lors de l'appel au service '" + nomService + "' : " + message);
        this.nomService = nomService;
    }

    public ServiceExterneException(String nomService, Throwable cause) {
        super("Le service '" + nomService + "' est actuellement indisponible", cause);
        this.nomService = nomService;
    }

    public String getNomService() {
        return nomService;
    }
}
