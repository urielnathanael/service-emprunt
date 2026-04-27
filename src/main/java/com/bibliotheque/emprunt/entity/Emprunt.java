package com.bibliotheque.emprunt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "emprunts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emprunt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate dateEmprunt;

    @Column(nullable = false)
    private LocalDate dateRetourPrevue;

    private LocalDate dateRetourReelle;

    @Column(nullable = false)
    private Long livreId;

    @Column(nullable = false)
    private Long emprunteurId;

    @Column(nullable = false)
    private String statut;
}
