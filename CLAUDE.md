# ConvertCalc

Calculatrice Android avec un mode conversion de devises (API [Frankfurter](https://frankfurter.dev/)).

## Architecture

Trois couches, sans framework de DI — composition manuelle dans `CalcApplication` (instances `by lazy`) :

- `domain/` — logique métier pure, aucune dépendance Android/Compose/DataStore.
  - `calculator/` : `Token` → `Parser` → `Expression` → `Evaluator`. Chaque étape a son propre sealed type de résultat (`ParseResult`, `EvaluationResult`, `CalculatorResult`) plutôt que des exceptions pour les cas attendus (division par zéro, expression incomplète).
  - `conversion/` : taux de change et conversion.
  - `history/` : historique d'expressions, groupement par jour local, rétention 30 jours.
- `infra/` — implémentations concrètes des interfaces du domain (`ExpressionHistoryStoreImpl`, `RatesRepositoryImpl`, `AppPreferenceStoreImpl`) + accès réseau/disque bruts (`FetchRates.kt`, `DataStore.kt`).
- `ui/screen/` — Compose + un seul `CalculatorViewModel` exposant un `StateFlow<UiState>` unique. Les actions UI passent par `CalculatorActions` (bundle de lambdas), pas d'accès direct au ViewModel depuis les composants enfants.

## Conventions établies

- **Précision décimale** : toute arithmétique passe par `BigDecimal` avec `MathContext.DECIMAL64` (16 chiffres significatifs) — `BigDecimal` est exact par défaut et sans borne, donc chaque opération (`+ - × ÷`) doit explicitement passer le `MathContext`, pas seulement la division.
- **Modélisation par sealed interface/class**, jamais d'enum + `when` avec des champs optionnels. Voir `Token`, `Expression`, `EvaluationResult`.
- **Séparation domain/infra stricte pour la sérialisation** : les types domain (`HistoryEntry`, `Token`...) ne portent pas d'annotations `kotlinx.serialization`. Chaque store a ses propres DTOs `Stored*` privés avec mapping explicite `toDomain()`/`fromDomain()` (voir `ExpressionHistoryStoreImpl`).
- **DataStore<Preferences> partagé** : une seule instance (`infra/DataStore.kt`), réutilisée par tous les stores. Ne pas créer de second `preferencesDataStore(...)` — ça crash au runtime (instances multiples sur le même fichier).
- **ViewModel testable par seams de constructeur**, pas de mocking framework : `clock: Clock`, `timeZoneProvider: () -> TimeZone`, `externalScope: CoroutineScope?` sont injectables pour les tests, avec des `Fake*` écrits à la main dans les fichiers de test plutôt que Mockk/Mockito.
- **Compose Material3 avant réimplémentation** : vérifier le comportement natif d'un composant (`ModalNavigationDrawer`, etc.) avant d'ajouter une correction manuelle par-dessus — cas vécu avec le scrim du drawer d'historique, doublon d'un comportement déjà géré nativement dès `gesturesEnabled = true`.

## Stack

- Kotlin 2.4.0, AGP 9.2.1, Compose BOM 2026.02.01, minSdk 26 (java.time disponible nativement, pas de desugaring nécessaire).
- Réseau : Ktor Client (`ktor-client-android`) + `kotlinx-serialization-json`.
- Dates : `kotlinx-datetime` pour la logique métier (jours locaux, fuseaux), `java.time` uniquement pour le formatage d'affichage.
- Tests : JUnit4, `@RunWith(Parameterized::class)` pour les fonctions pures à cas multiples (`EvaluatorTests`, `ParserTests`, `CalculatorInputTests`).

## Fonctionnalité non évidente

Appui long sur la touche retour arrière → efface tout l'état (expression + résultat).
