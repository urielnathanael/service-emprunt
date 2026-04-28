package com.bibliotheque.emprunt.exception;

/**
 * Exception levée quand le service-livre ne trouve pas un livre.
 */
public class LivreNotFoundException extends RuntimeException {

    private final Long livreId;

    public LivreNotFoundException(Long id) {
        super("Livre introuvable avec l'id : " + id);
        this.livreId = id;
    }

    public Long getLivreId() {
        return livreId;
    }
}
