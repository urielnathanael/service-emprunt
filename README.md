# 📚 SERVICE-EMPRUNT — Guide de compréhension complet

## 🗂️ Structure du projet

```
service-emprunt/
├── pom.xml                                          ← Configuration Maven (dépendances)
└── src/main/
    ├── java/com/bibliotheque/emprunt/
    │   ├── ServiceEmpruntApplication.java           ← Point d'entrée (main)
    │   ├── entity/
    │   │   └── Emprunt.java                         ← Entité JPA (table en BDD)
    │   ├── dto/
    │   │   ├── EmpruntDTO.java                      ← Objet de transfert de données
    │   │   ├── LivreDTO.java                        ← DTO reçu de service-livre
    │   │   └── EmprunteurDTO.java                   ← DTO reçu de service-emprunteur
    │   ├── repository/
    │   │   └── EmpruntRepository.java               ← Accès base de données
    │   ├── client/
    │   │   ├── LivreClient.java                     ← Appels HTTP vers service-livre
    │   │   └── EmprunteurClient.java                ← Appels HTTP vers service-emprunteur
    │   ├── config/
    │   │   └── RestClientConfig.java                ← Configuration du client HTTP
    │   └── service/
    │       └── EmpruntService.java                  ← Logique métier
    │   └── controller/
    │       └── EmpruntController.java               ← Endpoints REST
    └── resources/
        └── application.properties                   ← Configuration
```

---

## 🏗️ Architecture en couches

```
Client HTTP (Postman)
        │ requête JSON
        ▼
[ EmpruntController ]      ← Couche Web : reçoit/retourne HTTP
        │ appelle
        ▼
[ EmpruntService    ]      ← Couche Métier : logique, validations
        │              │
        ▼              ▼
[ EmpruntRepository ]  [ LivreClient / EmprunteurClient ]
(BDD locale)           (appels HTTP vers autres services via Eureka)
```

---

## 🔑 Toutes les annotations expliquées

### Annotations Spring Core
| Annotation | Rôle |
|---|---|
| `@SpringBootApplication` | Point d'entrée : active l'auto-config + scan de composants |
| `@EnableDiscoveryClient` | S'enregistre dans Eureka + peut découvrir les autres services |
| `@Component` | Déclare un bean générique dans le contexte Spring |
| `@Service` | Bean de la couche service (spécialisation de @Component) |
| `@Repository` | Bean de la couche DAO + traduction des exceptions SQL |
| `@Configuration` | Classe de configuration contenant des @Bean |
| `@Bean` | Méthode qui retourne un objet géré par Spring |

### Annotations REST (Spring MVC)
| Annotation | Rôle |
|---|---|
| `@RestController` | = @Controller + @ResponseBody (retourne du JSON) |
| `@RequestMapping("/api/emprunts")` | Préfixe URL pour tout le contrôleur |
| `@GetMapping` | Gère les requêtes HTTP GET |
| `@PostMapping` | Gère les requêtes HTTP POST (création) |
| `@PutMapping` | Gère les requêtes HTTP PUT (mise à jour) |
| `@DeleteMapping` | Gère les requêtes HTTP DELETE (suppression) |
| `@PathVariable` | Extrait une valeur de l'URL : `/emprunts/{id}` → `id` |
| `@RequestBody` | Désérialise le corps JSON en objet Java |

### Annotations JPA/Hibernate
| Annotation | Rôle |
|---|---|
| `@Entity` | La classe représente une table en base de données |
| `@Table(name="emprunts")` | Précise le nom de la table SQL |
| `@Id` | Clé primaire de la table |
| `@GeneratedValue(strategy=IDENTITY)` | Auto-incrément MySQL |
| `@Column(nullable=false)` | Contrainte NOT NULL en base |

### Annotations Lombok
| Annotation | Code généré |
|---|---|
| `@Data` | getters + setters + toString + equals + hashCode |
| `@NoArgsConstructor` | constructeur vide (requis par JPA et Jackson) |
| `@AllArgsConstructor` | constructeur avec tous les paramètres |
| `@Builder` | pattern Builder (construction fluente) |
| `@RequiredArgsConstructor` | constructeur pour les champs `final` → injection par constructeur |
| `@Slf4j` | `private static final Logger log = ...` |

### Annotations Spring Cloud
| Annotation | Rôle |
|---|---|
| `@LoadBalanced` | Le client HTTP résout les noms via Eureka + fait du load balancing |

---

## 🌐 Communication inter-services

### Comment ça marche ?

```
service-emprunt          Eureka Server           service-livre
      │                       │                       │
      │ 1. "Où est            │                       │
      │    service-livre ?"   │                       │
      │──────────────────────►│                       │
      │                       │                       │
      │ 2. "Il est à          │                       │
      │    192.168.1.10:8081" │                       │
      │◄──────────────────────│                       │
      │                       │                       │
      │ 3. HTTP GET http://192.168.1.10:8081/api/livres/1
      │───────────────────────────────────────────────►│
      │                       │                       │
      │ 4. Réponse JSON {id:1, titre:"...", disponible:true}
      │◄───────────────────────────────────────────────│
```

### Pourquoi `http://service-livre` et pas `http://localhost:8081` ?

- Si on met `localhost:8081`, le code est **lié** à un port et une machine spécifique.
- Si service-livre change de port, ou si on le déploie sur un autre serveur, il faut recompiler service-emprunt.
- Avec le nom logique `service-livre` + Eureka, la résolution est **dynamique**.

---

## 📡 Endpoints exposés

| Méthode | URL | Description | Corps attendu | Réponse |
|---|---|---|---|---|
| GET | `/api/emprunts` | Liste tous les emprunts | — | 200 + JSON |
| GET | `/api/emprunts/{id}` | Un emprunt par ID | — | 200 + JSON |
| POST | `/api/emprunts` | Crée un emprunt | `{livreId, emprunteurId}` | 201 + JSON |
| PUT | `/api/emprunts/{id}/retour` | Retour d'un livre | — | 200 + JSON |
| DELETE | `/api/emprunts/{id}` | Supprime un emprunt | — | 204 |

---

## 🔄 Flux de création d'un emprunt (POST)

```
1. Client → POST /api/emprunts
   Corps: { "livreId": 1, "emprunteurId": 2 }

2. EmpruntController.createEmprunt(dto)
   ↓ appelle

3. EmpruntService.createEmprunt(dto)
   ↓ 3a. livreClient.findById(1)
         → GET http://service-livre/api/livres/1 (via Eureka)
         ← LivreDTO { id:1, titre:"...", disponible: true }
   
   ↓ 3b. Vérifie livre.isDisponible() == true
         (sinon → exception "livre non disponible")
   
   ↓ 3c. emprunteurClient.findById(2)
         → GET http://service-emprunteur/api/emprunteurs/2 (via Eureka)
         ← EmprunteurDTO { id:2, nom:"Dupont", ... }
   
   ↓ 3d. Vérifie que l'emprunteur existe
         (sinon → exception "emprunteur non trouvé")
   
   ↓ 3e. Construit l'entité Emprunt :
         { livreId:1, emprunteurId:2, dateEmprunt:TODAY,
           dateRetourPrevue:TODAY+15j, statut:"EN_COURS" }
   
   ↓ 3f. empruntRepository.save(emprunt)
         → INSERT INTO emprunts (...) VALUES (...)
         ← emprunt sauvegardé avec id=42
   
   ↓ 3g. livreClient.updateDisponibilite(1, false)
         → PUT http://service-livre/api/livres/1/disponibilite (body: false)
         (marque le livre comme non disponible)
   
   ↓ 3h. Retourne EmpruntDTO { id:42, livreId:1, emprunteurId:2, ... }

4. Controller retourne → 201 Created + JSON de l'emprunt créé
```

---

## 📝 Questions probables du prof et réponses

### Q : Pourquoi utilise-t-on des DTO plutôt que les entités ?
**R :** Pour 3 raisons : (1) **Sécurité** — on contrôle ce qu'on expose en JSON. (2) **Découplage** — l'API reste stable même si l'entité change. (3) **Éviter les problèmes de sérialisation** de proxies Hibernate (LazyInitializationException).

### Q : Qu'est-ce que `@LoadBalanced` ?
**R :** C'est une annotation Spring Cloud qui intercepte les appels HTTP et remplace les noms de services (`http://service-livre`) par leur vraie adresse IP récupérée depuis Eureka. Elle active aussi le load balancing si plusieurs instances du service existent.

### Q : Pourquoi `Long` et pas `long` pour l'ID ?
**R :** `Long` (objet) peut être `null` avant la persistance en base. `long` (primitif) ne peut pas être null. Avant qu'Hibernate génère l'ID, le champ est `null`.

### Q : Que fait `empruntRepository.save(emprunt)` exactement ?
**R :** Si `emprunt.getId() == null` → **INSERT** (création). Si `emprunt.getId() != null` → **UPDATE** (mise à jour). Hibernate détecte automatiquement si c'est une création ou une mise à jour.

### Q : Comment fonctionne Eureka ?
**R :** (1) Chaque service démarre et s'enregistre sur le serveur Eureka avec son nom logique et son IP:PORT. (2) Toutes les 30 secondes, chaque service envoie un "heartbeat" pour signaler qu'il est vivant. (3) Si un heartbeat manque 3 fois, Eureka retire le service de la liste. (4) Les autres services peuvent interroger Eureka pour trouver l'adresse d'un service par son nom.

### Q : Pourquoi `Optional<LivreDTO>` et pas `LivreDTO` directement ?
**R :** `Optional` force l'appelant à gérer explicitement le cas "livre non trouvé". Retourner `null` serait risqué (NullPointerException). `Optional.orElseThrow()` permet de lancer une exception descriptive si la valeur est absente.

### Q : Qu'est-ce que `@NoArgsConstructor` est obligatoire pour JPA ?
**R :** Hibernate utilise la **réflexion Java** pour reconstruire les objets depuis les résultats SQL. Il crée une instance vide avec le constructeur sans argument, puis remplit les champs. Sans `@NoArgsConstructor`, Hibernate ne peut pas instancier l'entité → exception au démarrage.

### Q : Quelle est la différence entre `@Service`, `@Repository`, `@Controller` ?
**R :** Techniquement, ce sont toutes des spécialisations de `@Component`. Elles ont le même effet : déclarer un bean. La différence est **sémantique** (lisibilité du code) et **fonctionnelle** : `@Repository` active la traduction des exceptions SQL, `@Controller` active la détection des mappings HTTP.

### Q : Pourquoi `dateRetourReelle` n'a pas `nullable=false` ?
**R :** Car elle est `null` au moment de la création de l'emprunt (le livre n'est pas encore rendu). Elle ne sera remplie qu'au moment du retour via `PUT /api/emprunts/{id}/retour`.

---

## 🚀 Ordre de lancement des services

1. **eureka-server** (port 8761) — doit être le premier, sinon les autres échouent à s'enregistrer
2. **service-livre** (port 8081)
3. **service-emprunteur** (port 8082)
4. **service-emprunt** (port 8083)
5. **gateway-service** (port 8080)

---

## 🧪 Exemples de requêtes Postman

### Créer un emprunt
```http
POST http://localhost:8083/api/emprunts
Content-Type: application/json

{
  "livreId": 1,
  "emprunteurId": 1
}
```

### Via la Gateway (recommandé)
```http
POST http://localhost:8080/api/emprunts
Content-Type: application/json

{
  "livreId": 1,
  "emprunteurId": 1
}
```

### Enregistrer un retour
```http
PUT http://localhost:8083/api/emprunts/1/retour
```

### Liste des emprunts
```http
GET http://localhost:8083/api/emprunts
```
