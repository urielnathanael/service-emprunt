package com.bibliotheque.emprunt.client;

import com.bibliotheque.emprunt.dto.EmprunteurDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
public class EmprunteurClient {

    private final RestClient restClient;

    public EmprunteurClient(RestClient.Builder builder,
                            @Value("${service.emprunteur.url:http://localhost:8082}") String emprunteurUrl) {
        this.restClient = builder.baseUrl(emprunteurUrl).build();
    }

    public Optional<EmprunteurDTO> findById(Long id) {
        try {
            EmprunteurDTO emprunteur = restClient.get()
                    .uri("/api/emprunteurs/{id}", id)
                    .retrieve()
                    .body(EmprunteurDTO.class);
            return Optional.ofNullable(emprunteur);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
