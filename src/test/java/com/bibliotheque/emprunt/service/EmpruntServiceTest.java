package com.bibliotheque.emprunt.service;

import com.bibliotheque.emprunt.client.EmprunteurClient;
import com.bibliotheque.emprunt.client.LivreClient;
import com.bibliotheque.emprunt.dto.EmpruntDTO;
import com.bibliotheque.emprunt.dto.EmprunteurDTO;
import com.bibliotheque.emprunt.dto.LivreDTO;
import com.bibliotheque.emprunt.entity.Emprunt;
import com.bibliotheque.emprunt.exception.*;
import com.bibliotheque.emprunt.repository.EmpruntRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - EmpruntService")
class EmpruntServiceTest {

    @Mock private EmpruntRepository empruntRepository;
    @Mock private LivreClient       livreClient;
    @Mock private EmprunteurClient  emprunteurClient;

    @InjectMocks
    private EmpruntService empruntService;

    // ─── Données de test réutilisables ──────────────────────────────────────

    private Emprunt empruntEnCours;
    private Emprunt empruntRendu;
    private LivreDTO livreDisponible;
    private LivreDTO livreIndisponible;
    private EmprunteurDTO emprunteur;
    private EmpruntDTO requestDTO;

    @BeforeEach
    void setUp() {
        LocalDate today = LocalDate.now();

        empruntEnCours = Emprunt.builder()
                .id(1L)
                .livreId(10L)
                .emprunteurId(20L)
                .dateEmprunt(today.minusDays(5))
                .dateRetourPrevue(today.plusDays(10))
                .dateRetourReelle(null)
                .statut("EN_COURS")
                .build();

        empruntRendu = Emprunt.builder()
                .id(2L)
                .livreId(10L)
                .emprunteurId(20L)
                .dateEmprunt(today.minusDays(20))
                .dateRetourPrevue(today.minusDays(5))
                .dateRetourReelle(today.minusDays(6))
                .statut("RENDU")
                .build();

        livreDisponible = LivreDTO.builder()
                .id(10L)
                .titre("Clean Code")
                .auteur("Robert C. Martin")
                .isbn("978-0132350884")
                .disponible(true)
                .build();

        livreIndisponible = LivreDTO.builder()
                .id(10L)
                .titre("Clean Code")
                .auteur("Robert C. Martin")
                .isbn("978-0132350884")
                .disponible(false)
                .build();

        emprunteur = EmprunteurDTO.builder()
                .id(20L)
                .nom("Alice Dupont")
                .email("alice@mail.com")
                .telephone("0600000001")
                .build();

        requestDTO = EmpruntDTO.builder()
                .livreId(10L)
                .emprunteurId(20L)
                .build();
    }

    // =========================================================================
    // findAll()
    // =========================================================================
    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("Retourne une liste de DTOs quand des emprunts existent")
        void findAll_retourneListeDTOs() {
            when(empruntRepository.findAll()).thenReturn(List.of(empruntEnCours, empruntRendu));

            List<EmpruntDTO> result = empruntService.findAll();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getStatut()).isEqualTo("RENDU");
        }

        @Test
        @DisplayName("Retourne une liste vide quand il n'y a aucun emprunt")
        void findAll_retourneListeVide() {
            when(empruntRepository.findAll()).thenReturn(List.of());

            List<EmpruntDTO> result = empruntService.findAll();

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // findById()
    // =========================================================================
    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Retourne le DTO quand l'emprunt existe")
        void findById_retourneDTO_siExiste() {
            when(empruntRepository.findById(1L)).thenReturn(Optional.of(empruntEnCours));

            EmpruntDTO result = empruntService.findById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getStatut()).isEqualTo("EN_COURS");
            assertThat(result.getLivreId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Lève EmpruntNotFoundException si l'id est inconnu")
        void findById_leveException_siInconnu() {
            when(empruntRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> empruntService.findById(99L))
                    .isInstanceOf(EmpruntNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // =========================================================================
    // createEmprunt()
    // =========================================================================
    @Nested
    @DisplayName("createEmprunt()")
    class CreateEmprunt {

        @Test
        @DisplayName("Crée un emprunt avec succès quand le livre est disponible")
        void createEmprunt_succes_livreDisponible() {
            when(livreClient.findById(10L)).thenReturn(Optional.of(livreDisponible));
            when(emprunteurClient.findById(20L)).thenReturn(Optional.of(emprunteur));
            when(empruntRepository.save(any(Emprunt.class))).thenAnswer(inv -> {
                Emprunt e = inv.getArgument(0);
                e = Emprunt.builder()
                        .id(1L).livreId(e.getLivreId()).emprunteurId(e.getEmprunteurId())
                        .dateEmprunt(e.getDateEmprunt()).dateRetourPrevue(e.getDateRetourPrevue())
                        .dateRetourReelle(null).statut(e.getStatut()).build();
                return e;
            });

            EmpruntDTO result = empruntService.createEmprunt(requestDTO);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getStatut()).isEqualTo("EN_COURS");
            assertThat(result.getLivreId()).isEqualTo(10L);
            assertThat(result.getDateEmprunt()).isEqualTo(LocalDate.now());
            assertThat(result.getDateRetourPrevue()).isEqualTo(LocalDate.now().plusDays(15));

            verify(empruntRepository).save(any(Emprunt.class));
            verify(livreClient).updateDisponibilite(10L, false);
        }

        @Test
        @DisplayName("Lève LivreNotFoundException quand le livre est introuvable")
        void createEmprunt_leveLivreNotFoundException() {
            when(livreClient.findById(10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> empruntService.createEmprunt(requestDTO))
                    .isInstanceOf(LivreNotFoundException.class)
                    .hasMessageContaining("10");

            verify(empruntRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lève LivreIndisponibleException quand le livre n'est pas disponible")
        void createEmprunt_leveLivreIndisponibleException() {
            when(livreClient.findById(10L)).thenReturn(Optional.of(livreIndisponible));

            assertThatThrownBy(() -> empruntService.createEmprunt(requestDTO))
                    .isInstanceOf(LivreIndisponibleException.class)
                    .hasMessageContaining("Clean Code");

            verify(empruntRepository, never()).save(any());
        }

        @Test
        @DisplayName("Lève EmprunteurNotFoundException quand l'emprunteur est introuvable")
        void createEmprunt_leveEmprunteurNotFoundException() {
            when(livreClient.findById(10L)).thenReturn(Optional.of(livreDisponible));
            when(emprunteurClient.findById(20L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> empruntService.createEmprunt(requestDTO))
                    .isInstanceOf(EmprunteurNotFoundException.class)
                    .hasMessageContaining("20");

            verify(empruntRepository, never()).save(any());
        }

        @Test
        @DisplayName("Crée l'emprunt en mode dégradé si les services externes sont inaccessibles")
        void createEmprunt_modeDégrade_siServicesIndisponibles() {
            when(livreClient.findById(10L)).thenThrow(new RuntimeException("Connexion refusée"));
            when(empruntRepository.save(any())).thenAnswer(inv -> {
                Emprunt e = inv.getArgument(0);
                return Emprunt.builder().id(5L).livreId(e.getLivreId())
                        .emprunteurId(e.getEmprunteurId()).dateEmprunt(e.getDateEmprunt())
                        .dateRetourPrevue(e.getDateRetourPrevue()).statut(e.getStatut()).build();
            });

            EmpruntDTO result = empruntService.createEmprunt(requestDTO);

            assertThat(result.getStatut()).isEqualTo("EN_COURS");
            verify(empruntRepository).save(any());
        }
    }

    // =========================================================================
    // retourEmprunt()
    // =========================================================================
    @Nested
    @DisplayName("retourEmprunt()")
    class RetourEmprunt {

        @Test
        @DisplayName("Marque l'emprunt RENDU si retour dans les délais")
        void retourEmprunt_statutRENDU_siDansDelais() {
            when(empruntRepository.findById(1L)).thenReturn(Optional.of(empruntEnCours));
            when(empruntRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            EmpruntDTO result = empruntService.retourEmprunt(1L);

            assertThat(result.getStatut()).isEqualTo("RENDU");
            assertThat(result.getDateRetourReelle()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Marque l'emprunt RETARD si retour hors délais")
        void retourEmprunt_statutRETARD_siHorsDelais() {
            Emprunt empruntEnRetard = Emprunt.builder()
                    .id(3L).livreId(10L).emprunteurId(20L)
                    .dateEmprunt(LocalDate.now().minusDays(20))
                    .dateRetourPrevue(LocalDate.now().minusDays(5)) // déjà dépassé
                    .statut("EN_COURS")
                    .build();

            when(empruntRepository.findById(3L)).thenReturn(Optional.of(empruntEnRetard));
            when(empruntRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            EmpruntDTO result = empruntService.retourEmprunt(3L);

            assertThat(result.getStatut()).isEqualTo("RETARD");
        }

        @Test
        @DisplayName("Lève EmpruntNotFoundException si l'emprunt est inconnu")
        void retourEmprunt_leveException_siInconnu() {
            when(empruntRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> empruntService.retourEmprunt(99L))
                    .isInstanceOf(EmpruntNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("Lève EmpruntDejaRenduException si le statut est déjà RENDU")
        void retourEmprunt_leveException_siDejaRendu() {
            when(empruntRepository.findById(2L)).thenReturn(Optional.of(empruntRendu));

            assertThatThrownBy(() -> empruntService.retourEmprunt(2L))
                    .isInstanceOf(EmpruntDejaRenduException.class)
                    .hasMessageContaining("2");

            verify(empruntRepository, never()).save(any());
        }

        @Test
        @DisplayName("Met à jour la disponibilité du livre après retour")
        void retourEmprunt_metAJourDisponibilite() {
            when(empruntRepository.findById(1L)).thenReturn(Optional.of(empruntEnCours));
            when(empruntRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            empruntService.retourEmprunt(1L);

            verify(livreClient).updateDisponibilite(10L, true);
        }
    }

    // =========================================================================
    // delete()
    // =========================================================================
    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Supprime l'emprunt quand il existe")
        void delete_succes_siExiste() {
            when(empruntRepository.existsById(1L)).thenReturn(true);

            assertThatCode(() -> empruntService.delete(1L)).doesNotThrowAnyException();

            verify(empruntRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Lève EmpruntNotFoundException si l'id est inconnu")
        void delete_leveException_siInconnu() {
            when(empruntRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> empruntService.delete(99L))
                    .isInstanceOf(EmpruntNotFoundException.class)
                    .hasMessageContaining("99");

            verify(empruntRepository, never()).deleteById(any());
        }
    }
}
