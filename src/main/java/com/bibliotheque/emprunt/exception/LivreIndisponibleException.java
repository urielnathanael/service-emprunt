package com.bibliotheque.emprunt.exception;

/**
 * Exception levée quand un emprunt est demandé sur un livre non disponible.
 */
public class LivreIndisponibleException extends RuntimeException {

    private final Long livreId;
    private final String titreLivre;

    public LivreIndisponibleException(Long livreId, String titreLivre) {
        super("Le livre '" + titreLivre + "' (id=" + livreId + ") n'est pas disponible à l'emprunt");
        this.livreId = livreId;
        this.titreLivre = titreLivre;
    }

    public Long getLivreId() {
        return livreId;
    }

    public String getTitreLivre() {
        return titreLivre;
    }
}
