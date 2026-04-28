package com.bibliotheque.emprunt.exception;

/**
 * Exception levée quand un emprunt est introuvable en base de données.
 */
public class EmpruntNotFoundException extends RuntimeException {

    private final Long empruntId;

    public EmpruntNotFoundException(Long id) {
        super("Emprunt introuvable avec l'id : " + id);
        this.empruntId = id;
    }

    public Long getEmpruntId() {
        return empruntId;
    }
}
