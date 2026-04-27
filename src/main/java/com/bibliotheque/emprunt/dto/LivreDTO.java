package com.bibliotheque.emprunt.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LivreDTO {
    private Long id;
    private String titre;
    private String auteur;
    private String isbn;
    private boolean disponible;
}
