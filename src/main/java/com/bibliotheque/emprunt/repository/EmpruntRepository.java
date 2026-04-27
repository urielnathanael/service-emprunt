package com.bibliotheque.emprunt.repository;

import com.bibliotheque.emprunt.entity.Emprunt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmpruntRepository extends JpaRepository<Emprunt, Long> {
    List<Emprunt> findByEmprunteurId(Long emprunteurId);
    List<Emprunt> findByLivreId(Long livreId);
    List<Emprunt> findByStatut(String statut);
    List<Emprunt> findByEmprunteurIdAndStatut(Long emprunteurId, String statut);
}
