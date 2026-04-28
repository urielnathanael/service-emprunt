package com.bibliotheque.emprunt.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Service Emprunt API")
                        .version("1.0.0")
                        .description("""
                                ## Microservice de gestion des emprunts de livres
                                
                                Ce service permet de gérer les emprunts dans le système de bibliothèque.
                                
                                ### Fonctionnalités :
                                - Lister tous les emprunts
                                - Consulter un emprunt par ID
                                - Créer un nouvel emprunt
                                - Enregistrer un retour de livre
                                - Supprimer un emprunt
                                
                                ### Statuts possibles :
                                - `EN_COURS` : livre emprunté, retour non effectué
                                - `RENDU` : livre retourné dans les délais
                                - `RETARD` : livre retourné en retard
                                """)
                        .contact(new Contact()
                                .name("Bibliothèque Microservices")
                                .email("contact@bibliotheque.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8083")
                                .description("Serveur local de développement")
                ));
    }
}
