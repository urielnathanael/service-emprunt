package com.bibliotheque.emprunt.exception;

/**
 * Exception levée quand le service-emprunteur ne trouve pas un emprunteur.
 */
public class EmprunteurNotFoundException extends RuntimeException {

    private final Long emprunteurId;

    public EmprunteurNotFoundException(Long id) {
        super("Emprunteur introuvable avec l'id : " + id);
        this.emprunteurId = id;
    }

    public Long getEmprunteurId() {
        return emprunteurId;
    }
}
