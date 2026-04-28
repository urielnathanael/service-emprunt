package com.bibliotheque.emprunt.controller;

import com.bibliotheque.emprunt.dto.EmpruntDTO;
import com.bibliotheque.emprunt.exception.*;
import com.bibliotheque.emprunt.service.EmpruntService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmpruntController.class)
@DisplayName("Tests unitaires - EmpruntController")
class EmpruntControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmpruntService empruntService;

    private ObjectMapper objectMapper;
    private EmpruntDTO empruntDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        empruntDTO = EmpruntDTO.builder()
                .id(1L)
                .livreId(10L)
                .emprunteurId(20L)
                .dateEmprunt(LocalDate.now())
                .dateRetourPrevue(LocalDate.now().plusDays(15))
                .dateRetourReelle(null)
                .statut("EN_COURS")
                .build();
    }

    // =========================================================================
    // GET /api/emprunts
    // =========================================================================
    @Nested
    @DisplayName("GET /api/emprunts")
    class GetAll {

        @Test
        @DisplayName("200 - Retourne la liste de tous les emprunts")
        void getAll_retourne200_avecListe() throws Exception {
            when(empruntService.findAll()).thenReturn(List.of(empruntDTO));

            mockMvc.perform(get("/api/emprunts"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].statut").value("EN_COURS"))
                    .andExpect(jsonPath("$[0].livreId").value(10));
        }

        @Test
        @DisplayName("200 - Retourne une liste vide quand il n'y a aucun emprunt")
        void getAll_retourne200_listeVide() throws Exception {
            when(empruntService.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/emprunts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // =========================================================================
    // GET /api/emprunts/{id}
    // =========================================================================
    @Nested
    @DisplayName("GET /api/emprunts/{id}")
    class GetById {

        @Test
        @DisplayName("200 - Retourne l'emprunt quand il existe")
        void getById_retourne200() throws Exception {
            when(empruntService.findById(1L)).thenReturn(empruntDTO);

            mockMvc.perform(get("/api/emprunts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.livreId").value(10))
                    .andExpect(jsonPath("$.emprunteurId").value(20))
                    .andExpect(jsonPath("$.statut").value("EN_COURS"));
        }

        @Test
        @DisplayName("404 - Retourne NOT_FOUND quand l'emprunt est introuvable")
        void getById_retourne404_siInconnu() throws Exception {
            when(empruntService.findById(99L))
                    .thenThrow(new EmpruntNotFoundException(99L));

            mockMvc.perform(get("/api/emprunts/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("99")));
        }
    }

    // =========================================================================
    // POST /api/emprunts
    // =========================================================================
    @Nested
    @DisplayName("POST /api/emprunts")
    class Create {

        @Test
        @DisplayName("201 - Crée un emprunt avec succès")
        void create_retourne201() throws Exception {
            EmpruntDTO request = EmpruntDTO.builder()
                    .livreId(10L)
                    .emprunteurId(20L)
                    .build();

            when(empruntService.createEmprunt(any(EmpruntDTO.class))).thenReturn(empruntDTO);

            mockMvc.perform(post("/api/emprunts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.statut").value("EN_COURS"));
        }

        @Test
        @DisplayName("404 - Retourne NOT_FOUND si le livre est introuvable")
        void create_retourne404_siLivreIntrouvable() throws Exception {
            EmpruntDTO request = EmpruntDTO.builder().livreId(99L).emprunteurId(20L).build();

            when(empruntService.createEmprunt(any()))
                    .thenThrow(new LivreNotFoundException(99L));

            mockMvc.perform(post("/api/emprunts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("400 - Retourne BAD_REQUEST si le livre est indisponible")
        void create_retourne400_siLivreIndisponible() throws Exception {
            EmpruntDTO request = EmpruntDTO.builder().livreId(10L).emprunteurId(20L).build();

            when(empruntService.createEmprunt(any()))
                    .thenThrow(new LivreIndisponibleException(10L, "Clean Code"));

            mockMvc.perform(post("/api/emprunts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("LIVRE_INDISPONIBLE"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Clean Code")));
        }

        @Test
        @DisplayName("404 - Retourne NOT_FOUND si l'emprunteur est introuvable")
        void create_retourne404_siEmprunteurIntrouvable() throws Exception {
            EmpruntDTO request = EmpruntDTO.builder().livreId(10L).emprunteurId(99L).build();

            when(empruntService.createEmprunt(any()))
                    .thenThrow(new EmprunteurNotFoundException(99L));

            mockMvc.perform(post("/api/emprunts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }
    }

    // =========================================================================
    // PUT /api/emprunts/{id}/retour
    // =========================================================================
    @Nested
    @DisplayName("PUT /api/emprunts/{id}/retour")
    class Retour {

        @Test
        @DisplayName("200 - Enregistre le retour avec succès")
        void retour_retourne200() throws Exception {
            EmpruntDTO rendu = EmpruntDTO.builder()
                    .id(1L).livreId(10L).emprunteurId(20L)
                    .statut("RENDU").dateRetourReelle(LocalDate.now()).build();

            when(empruntService.retourEmprunt(1L)).thenReturn(rendu);

            mockMvc.perform(put("/api/emprunts/1/retour"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statut").value("RENDU"))
                    .andExpect(jsonPath("$.dateRetourReelle").isNotEmpty());
        }

        @Test
        @DisplayName("404 - Retourne NOT_FOUND si l'emprunt est inconnu")
        void retour_retourne404_siInconnu() throws Exception {
            when(empruntService.retourEmprunt(99L))
                    .thenThrow(new EmpruntNotFoundException(99L));

            mockMvc.perform(put("/api/emprunts/99/retour"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("400 - Retourne BAD_REQUEST si l'emprunt est déjà rendu")
        void retour_retourne400_siDejaRendu() throws Exception {
            when(empruntService.retourEmprunt(2L))
                    .thenThrow(new EmpruntDejaRenduException(2L));

            mockMvc.perform(put("/api/emprunts/2/retour"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("EMPRUNT_DEJA_RENDU"))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("2")));
        }
    }

    // =========================================================================
    // DELETE /api/emprunts/{id}
    // =========================================================================
    @Nested
    @DisplayName("DELETE /api/emprunts/{id}")
    class DeleteEmprunt {

        @Test
        @DisplayName("204 - Supprime l'emprunt avec succès")
        void delete_retourne204() throws Exception {
            doNothing().when(empruntService).delete(1L);

            mockMvc.perform(delete("/api/emprunts/1"))
                    .andExpect(status().isNoContent());

            verify(empruntService).delete(1L);
        }

        @Test
        @DisplayName("404 - Retourne NOT_FOUND si l'emprunt est inconnu")
        void delete_retourne404_siInconnu() throws Exception {
            doThrow(new EmpruntNotFoundException(99L)).when(empruntService).delete(99L);

            mockMvc.perform(delete("/api/emprunts/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.path").value("/api/emprunts/99"));
        }
    }
}
