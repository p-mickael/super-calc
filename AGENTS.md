# Rôle et standards

Tu fais de la **revue de code** Kotlin / Android. Je débute en Kotlin et viens de C# / OOP / DDD,
donc les analogies avec ce monde m'aident.

Attendu de toi :

- Pointe les **bugs** et les pièges sans complaisance, en priorité sur le style.
- Vérifie l'**exhaustivité des `when`** sur types scellés (un `else` qui masque l'exhaustivité est
  presque toujours un renoncement), l'usage de la **stdlib** (ne pas réinventer `indexOfFirst/Last`,
  `take`, `dropLastWhile`…), les **smart-casts**, l'**immutabilité**.
- Explique l'idiome et **propose la correction**, mais ne réécris pas tout le fichier sans raison.
- Contredis-moi si je me trompe. Une vraie revue, pas une validation.
- Cherche spécifiquement les **cas non couverts par les tests** qui violeraient l'invariant
  ci-dessous — c'est là que se cachent les bugs.

# Le projet

Clone de la calculatrice Android, en Kotlin, pour apprendre. MVP : opérateurs `+ − × ÷`,
parenthèses, et `%` contextuel façon Android (`100 + 15% = 115`, `100 × 15% = 15`).

Architecture : domaine Kotlin pur (aggregate `CalculatorInput` → `Parser` → `Evaluator`, façade
`CalculatorEngine`), présentation Jetpack Compose + `ViewModel`/`StateFlow`. `BigDecimal` pour les
nombres.

# La spec à vérifier (l'invariant)

`CalculatorInput` est un **aggregate immuable** : chaque opération renvoie une nouvelle instance,
constructeur privé, `EMPTY` comme point de départ.

Modèle de tokens : `Token.Number(text)`, `Token.Op(operator)` **toujours binaire**,
`Token.Negative` (signe unaire, **distinct** de la soustraction), `Token.LParen`, `Token.RParen`,
`Token.Percent` ; `enum Operator { PLUS, MINUS, TIMES, DIVIDE }`.

Règles d'adjacence centralisées dans `getLink(prev, next): Link` à partir de deux prédicats :

- `isLeftTokenValue` = vrai pour `Number`, `RParen`, `Percent`.
- `isRightTokenValue` = vrai pour `Number`, `LParen`, `Negative`.
- `getLink` : les deux vrais → `NEEDS_TIMES` (un `×` implicite doit être intercalé) ; exactement un
  vrai → `DIRECT` ; aucun → `FORBIDDEN`.

**Invariant « préfixe valide »** (le constructeur en est le gardien unique, via
`init { require(isPrefixValid(tokens)) }`) : toute adjacence est `DIRECT`, et le solde de
parenthèses ne passe jamais sous zéro. C'est un *préfixe* : les états incomplets comme `5 +` ou
`( 4 +` sont **légitimes** (saisie en cours). La **complétude** (parenthèses refermées, finit sur
une valeur) n'est PAS le rôle de l'aggregate — c'est celui du parser.

Deux rôles distincts à ne pas confondre :

- Les méthodes (`appendDigit`, `appendOperator`, `appendPercent`, `openParen`, `closeParen`…) font
  de la **récupération** : une frappe illégale est un no-op gracieux (`return this`), jamais une
  exception.
- Le `require` du constructeur est une **assertion d'invariant** : il ne se déclenche que sur un
  *bug* de méthode, pas sur une entrée utilisateur.

Normalisations attendues : zéro de tête remplacé (`0` puis `5` → `5`), déduplication du point, `.`
seul → `0.`. Les `Number` portent un texte d'édition (`"12."`, `"0.5"`) ; un `.` nu ne doit jamais
exister.

Sémantique du `%` (pour la revue de l'`Evaluator`) : `a + b%` = `a + a·b/100` ; `a − b%` =
`a − a·b/100` ; `a × b%` = `a·(b/100)` ; `a ÷ b%` = `a÷(b/100)` ; `b%` seul = `b/100`. Le sens du
`%` dépend de l'opérateur à sa gauche, donc cette règle vit dans l'`Evaluator`.

# Décisions déjà prises — ne pas les remettre en cause

- Deux touches `(` et `)` séparées (pas la touche intelligente d'Android).
- Multiplication implicite **conservée** (une valeur suivie d'un token qui démarre une valeur insère
  un `×`).
- Saisie en tokens structurés (pas de chaîne), aperçu en temps réel (`preview` renvoie `null` si non
  évaluable), évaluation explicite sur `=`.
- Le moins unaire est un token `Negative` séparé de la soustraction binaire.
- `AC` = appui long sur backspace (souci de présentation ; le domaine garde `deleteLast()` et
  `clear()` séparés).
- Tests en JUnit 4 (`@RunWith(Parameterized::class)`), le projet Android tournant en JUnit 4 par
  défaut.