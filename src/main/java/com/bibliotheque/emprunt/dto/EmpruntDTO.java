package com.bibliotheque.emprunt.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmpruntDTO {
    private Long id;
    private LocalDate dateEmprunt;
    private LocalDate dateRetourPrevue;
    private LocalDate dateRetourReelle;
    private Long livreId;
    private Long emprunteurId;
    private String statut;
}
