package com.bibliotheque.emprunt.service;

import com.bibliotheque.emprunt.client.EmprunteurClient;
import com.bibliotheque.emprunt.client.LivreClient;
import com.bibliotheque.emprunt.dto.EmpruntDTO;
import com.bibliotheque.emprunt.dto.EmprunteurDTO;
import com.bibliotheque.emprunt.dto.LivreDTO;
import com.bibliotheque.emprunt.entity.Emprunt;
import com.bibliotheque.emprunt.exception.*;
import com.bibliotheque.emprunt.repository.EmpruntRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmpruntService {

    private final EmpruntRepository empruntRepository;
    private final LivreClient livreClient;
    private final EmprunteurClient emprunteurClient;

    public List<EmpruntDTO> findAll() {
        return empruntRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EmpruntDTO findById(Long id) {
        return empruntRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new EmpruntNotFoundException(id));
    }

    public EmpruntDTO createEmprunt(EmpruntDTO dto) {
        // Vérification du livre via le service externe
        try {
            LivreDTO livre = livreClient.findById(dto.getLivreId())
                    .orElseThrow(() -> new LivreNotFoundException(dto.getLivreId()));

            if (!livre.isDisponible()) {
                throw new LivreIndisponibleException(livre.getId(), livre.getTitre());
            }

            EmprunteurDTO emprunteur = emprunteurClient.findById(dto.getEmprunteurId())
                    .orElseThrow(() -> new EmprunteurNotFoundException(dto.getEmprunteurId()));

            log.info("Livre '{}' disponible, emprunteur '{}' trouvé.", livre.getTitre(), emprunteur.getNom());

        } catch (LivreNotFoundException | LivreIndisponibleException | EmprunteurNotFoundException ex) {
            // Exceptions métier : on remonte directement, sans ignorer
            throw ex;
        } catch (Exception ex) {
            // Services externes inaccessibles : on continue en mode dégradé
            log.warn("Services externes indisponibles, emprunt créé sans vérification : {}", ex.getMessage());
        }

        LocalDate today = LocalDate.now();

        Emprunt emprunt = Emprunt.builder()
                .livreId(dto.getLivreId())
                .emprunteurId(dto.getEmprunteurId())
                .dateEmprunt(today)
                .dateRetourPrevue(today.plusDays(15))
                .dateRetourReelle(null)
                .statut("EN_COURS")
                .build();

        Emprunt saved = empruntRepository.save(emprunt);

        // Mise à jour de la disponibilité du livre (best-effort)
        try {
            livreClient.updateDisponibilite(dto.getLivreId(), false);
        } catch (Exception ex) {
            log.warn("Impossible de marquer le livre indisponible (service-livre absent) : {}", ex.getMessage());
        }

        return toDTO(saved);
    }

    public EmpruntDTO retourEmprunt(Long id) {
        Emprunt emprunt = empruntRepository.findById(id)
                .orElseThrow(() -> new EmpruntNotFoundException(id));

        if ("RENDU".equals(emprunt.getStatut()) || "RETARD".equals(emprunt.getStatut())) {
            throw new EmpruntDejaRenduException(id);
        }

        LocalDate today = LocalDate.now();
        String nouveauStatut = today.isAfter(emprunt.getDateRetourPrevue()) ? "RETARD" : "RENDU";

        emprunt.setDateRetourReelle(today);
        emprunt.setStatut(nouveauStatut);

        Emprunt updated = empruntRepository.save(emprunt);

        // Remise en disponibilité du livre (best-effort)
        try {
            livreClient.updateDisponibilite(emprunt.getLivreId(), true);
        } catch (Exception ex) {
            log.warn("Impossible de remettre le livre disponible (service-livre absent) : {}", ex.getMessage());
        }

        return toDTO(updated);
    }

    public void delete(Long id) {
        if (!empruntRepository.existsById(id)) {
            throw new EmpruntNotFoundException(id);
        }
        empruntRepository.deleteById(id);
    }

    // ─── Mappers ──────────────────────────────────────────────────────────────

    private EmpruntDTO toDTO(Emprunt emprunt) {
        return EmpruntDTO.builder()
                .id(emprunt.getId())
                .livreId(emprunt.getLivreId())
                .emprunteurId(emprunt.getEmprunteurId())
                .dateEmprunt(emprunt.getDateEmprunt())
                .dateRetourPrevue(emprunt.getDateRetourPrevue())
                .dateRetourReelle(emprunt.getDateRetourReelle())
                .statut(emprunt.getStatut())
                .build();
    }
}
