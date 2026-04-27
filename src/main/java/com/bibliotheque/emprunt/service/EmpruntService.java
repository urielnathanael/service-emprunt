package com.bibliotheque.emprunt.service;

import com.bibliotheque.emprunt.client.EmprunteurClient;
import com.bibliotheque.emprunt.client.LivreClient;
import com.bibliotheque.emprunt.dto.EmpruntDTO;
import com.bibliotheque.emprunt.dto.EmprunteurDTO;
import com.bibliotheque.emprunt.dto.LivreDTO;
import com.bibliotheque.emprunt.entity.Emprunt;
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
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé avec l'id : " + id));
    }

    public EmpruntDTO createEmprunt(EmpruntDTO dto) {
        try {
            LivreDTO livre = livreClient.findById(dto.getLivreId())
                    .orElseThrow(() -> new RuntimeException("Livre non trouvé avec l'id : " + dto.getLivreId()));

            if (!livre.isDisponible()) {
                throw new RuntimeException("Le livre '" + livre.getTitre() + "' n'est pas disponible");
            }

            EmprunteurDTO emprunteur = emprunteurClient.findById(dto.getEmprunteurId())
                    .orElseThrow(() -> new RuntimeException("Emprunteur non trouvé avec l'id : " + dto.getEmprunteurId()));

            log.info("Livre '{}' disponible, emprunteur '{}' trouvé.", livre.getTitre(), emprunteur.getNom());
        } catch (RuntimeException e) {
            if (e.getMessage().contains("non trouvé") || e.getMessage().contains("pas disponible")) {
                throw e;
            }
            log.warn("Services externes indisponibles, création de l'emprunt sans vérification : {}", e.getMessage());
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

        try {
            livreClient.updateDisponibilite(dto.getLivreId(), false);
        } catch (Exception e) {
            log.warn("Impossible de mettre à jour la disponibilité du livre (service-livre absent) : {}", e.getMessage());
        }

        return toDTO(saved);
    }

    public EmpruntDTO retourEmprunt(Long id) {
        Emprunt emprunt = empruntRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé avec l'id : " + id));

        if ("RENDU".equals(emprunt.getStatut())) {
            throw new RuntimeException("Cet emprunt a déjà été rendu");
        }

        LocalDate today = LocalDate.now();
        String nouveauStatut = today.isAfter(emprunt.getDateRetourPrevue()) ? "RETARD" : "RENDU";

        emprunt.setDateRetourReelle(today);
        emprunt.setStatut(nouveauStatut);

        Emprunt updated = empruntRepository.save(emprunt);

        try {
            livreClient.updateDisponibilite(emprunt.getLivreId(), true);
        } catch (Exception e) {
            log.warn("Impossible de remettre le livre disponible (service-livre absent) : {}", e.getMessage());
        }

        return toDTO(updated);
    }

    public void delete(Long id) {
        if (!empruntRepository.existsById(id)) {
            throw new RuntimeException("Emprunt non trouvé avec l'id : " + id);
        }
        empruntRepository.deleteById(id);
    }

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

    private Emprunt toEntity(EmpruntDTO dto) {
        return Emprunt.builder()
                .livreId(dto.getLivreId())
                .emprunteurId(dto.getEmprunteurId())
                .dateEmprunt(dto.getDateEmprunt())
                .dateRetourPrevue(dto.getDateRetourPrevue())
                .dateRetourReelle(dto.getDateRetourReelle())
                .statut(dto.getStatut())
                .build();
    }
}
