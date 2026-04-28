package com.bibliotheque.emprunt.exception;

/**
 * Exception levée quand on tente de rendre un emprunt déjà clôturé.
 */
public class EmpruntDejaRenduException extends RuntimeException {

    private final Long empruntId;

    public EmpruntDejaRenduException(Long empruntId) {
        super("L'emprunt avec l'id " + empruntId + " a déjà été rendu");
        this.empruntId = empruntId;
    }

    public Long getEmpruntId() {
        return empruntId;
    }
}
