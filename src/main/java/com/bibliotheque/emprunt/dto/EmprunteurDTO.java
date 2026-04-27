package com.bibliotheque.emprunt.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmprunteurDTO {
    private Long id;
    private String nom;
    private String email;
    private String telephone;
}
