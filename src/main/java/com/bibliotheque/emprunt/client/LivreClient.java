package com.bibliotheque.emprunt.client;

import com.bibliotheque.emprunt.dto.LivreDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class LivreClient {

    private final RestClient restClient;

    public LivreClient(RestClient.Builder builder,
                       @Value("${service.livre.url:http://localhost:8081}") String livreUrl) {
        this.restClient = builder.baseUrl(livreUrl).build();
    }

    public Optional<LivreDTO> findById(Long id) {
        try {
            LivreDTO livre = restClient.get()
                    .uri("/api/livres/{id}", id)
                    .retrieve()
                    .body(LivreDTO.class);
            return Optional.ofNullable(livre);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    public void updateDisponibilite(Long livreId, boolean disponible) {
        restClient.put()
                .uri("/api/livres/{id}/disponibilite", livreId)
                .body(disponible)
                .retrieve()
                .toBodilessEntity();
    }
}
