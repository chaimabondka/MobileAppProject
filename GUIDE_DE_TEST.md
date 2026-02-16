# Guide de test – Event Booking App

Ce document décrit comment tester **toutes les fonctionnalités** de l’application.

---

## Prérequis

- **Connexion internet** (sauf pour le test mode hors ligne)
- **Clé imgBB** configurée dans `lib/manager/imgbb_config.dart` (pour ajout d’événement avec image)
- **Émulateur Android** ou appareil physique
- Au moins **2 comptes** (un participant, un organisateur) pour tester inscriptions et scan QR

---

## 1. Démarrage et authentification

### 1.1 Splash et redirection
- [ ] Lancer l’app → écran splash puis redirection automatique
- [ ] Si **déjà connecté** → accès à l’écran d’accueil (liste des événements)
- [ ] Si **non connecté** → écran de connexion

### 1.2 Connexion (Login)
- [ ] Saisir un email **non vérifié** → message demandant de vérifier l’email
- [ ] Saisir un mauvais email/mot de passe → message d’erreur
- [ ] Saisir des identifiants valides → accès à l’écran d’accueil
- [ ] Lien « Register » → ouvre l’écran d’inscription

### 1.3 Inscription (Sign up)
- [ ] Remplir nom, email, téléphone, mot de passe
- [ ] Email déjà utilisé → message d’erreur
- [ ] Inscription réussie → redirection vers l’écran de connexion (ou accès direct selon le flux)
- [ ] Lien pour aller vers Login → retour à la connexion

### 1.4 Déconnexion
- [ ] Depuis l’écran d’accueil : icône **déconnexion** (haut à droite) → retour à l’écran de connexion
- [ ] Depuis le **drawer** (menu) : « Logout » → dialogue de confirmation → déconnexion

---

## 2. Navigation principale (participant)

Barre du bas (5 onglets) :

| Onglet      | Écran              | À tester |
|------------|---------------------|----------|
| Historique | Événements assistés | [ ] Liste des événements auxquels vous avez participé ; affichage du QR par événement |
| Recherche  | Recherche           | [ ] Recherche par nom / critères ; résultats cohérents |
| Accueil    | Événements à venir  | [ ] Liste des événements ; filtres / tri si présents |
| Favoris    | Favoris             | [ ] Liste des événements mis en favoris |
| Profil     | (ou Favoris selon build) | [ ] Si onglet Profil : accès au profil |

---

## 3. Événements (participant)

### 3.1 Liste des événements (écran d’accueil)
- [ ] Ouvrir le **drawer** (menu hamburger) → options : Profile, All Events, My Events, Favourites, History, Logout
- [ ] Liste des événements chargée depuis Firebase
- [ ] Un événement **en cours** → clic ouvre un **bottom sheet** (détails + Register / Deregister)
- [ ] Bouton **Register** → inscription à l’événement (avec génération QR si implémenté)
- [ ] Bouton **Deregister** → désinscription
- [ ] Badge / indicateur « Registered » sur les événements déjà inscrits

### 3.2 Détail d’un événement
- [ ] Depuis « All Events » (drawer) : onglet « All Events » / « Registered Events » ; clic sur un événement → écran détail
- [ ] Affichage : image, nom, description, dates, horaires, prix (ou FREE)
- [ ] Si **organisateur** de l’événement : message « You are organizer of this event »
- [ ] Si **participant** et événement en cours : bouton **Register** ou **Deregister** selon statut
- [ ] Clic sur l’image → agrandissement (dialog)
- [ ] Bouton retour → retour à la liste

### 3.3 Favoris
- [ ] Depuis une carte d’événement : icône **cœur** → ajout / retrait des favoris
- [ ] Onglet **Favoris** (barre du bas) → liste des événements favoris uniquement
- [ ] Clic sur un favori → détail (même comportement que ci‑dessus)

### 3.4 Historique (événements assistés)
- [ ] Onglet **Historique** → liste des événements auxquels vous avez participé
- [ ] Affichage du **QR code** par événement (pour vérification à l’entrée)
- [ ] Vérifier que le QR s’affiche correctement (données participant + événement)

---

## 4. Recherche

- [ ] Onglet **Recherche** (barre du bas)
- [ ] Saisir un mot‑clé (nom d’événement, lieu, etc.) → résultats filtrés
- [ ] Aucun résultat → message ou liste vide approprié
- [ ] Clic sur un résultat → détail de l’événement

---

## 5. Organisateur : créer et gérer des événements

### 5.1 Accès « My Events »
- [ ] Drawer → **My Events** → liste des événements **organisés par vous**
- [ ] Si aucun événement : message / illustration « no event » + bouton pour en ajouter

### 5.2 Ajouter un événement
- [ ] Depuis **My Events** : bouton **Add Event** (ou équivalent) → écran **Add Event**
- [ ] Remplir : nom, description, **affiche (image)** , dates, horaires, lieu, catégorie, prix, etc.
- [ ] **Image** : choix galerie / caméra → upload via **imgBB** (vérifier que la clé imgBB est bien configurée)
- [ ] Champs obligatoires vides → messages de validation
- [ ] Soumission → événement créé dans Firebase ; retour à la liste « My Events » ou détail

### 5.3 Modifier un événement
- [ ] Depuis **My Events** ou **détail d’un événement dont vous êtes organisateur** : bouton **Edit**
- [ ] Modifier des champs (nom, date, image, etc.) → sauvegarder
- [ ] Vérifier en rafraîchissant la liste / le détail que les changements sont visibles

### 5.4 Supprimer un événement (si implémenté)
- [ ] Depuis détail ou liste : option **Delete** → confirmation → événement supprimé et disparaît des listes

---

## 6. Scan QR (organisateur / contrôle à l’entrée)

- [ ] Depuis un écran où l’organisateur gère son événement (ex. détail ou liste « My Events ») : bouton **Scanner QR** / icône QR
- [ ] Lancer le scan → caméra s’ouvre
- [ ] Scanner le **QR d’un participant** (généré dans « Historique » ou détail après inscription)
- [ ] Vérification : message « Participant registered » ou équivalent si le participant est bien inscrit à cet événement
- [ ] Scanner un QR d’un **autre événement** ou **non enregistré** → message d’erreur approprié

---

## 7. Profil

- [ ] Drawer → **Profile** → écran profil avec photo, nom, email, téléphone
- [ ] **Changer la photo** : bouton / icône sur l’avatar → choix galerie/caméra → photo mise à jour (Firebase Storage ou équivalent)
- [ ] **Modifier** nom, téléphone (si champs éditables) → sauvegarder → données mises à jour
- [ ] Retour → drawer ou écran précédent

---

## 8. Mode hors ligne

- [ ] Avec l’app ouverte : **couper le WiFi et la données** (ou mode avion)
- [ ] L’app doit afficher l’**écran hors ligne** (OfflineScreen) avec message explicite
- [ ] **Rétablir la connexion** → retour automatique à l’écran précédent (ou accueil) sans crash

---

## 9. Récapitulatif par écran

| Écran / Fonctionnalité      | Actions à tester |
|-----------------------------|------------------|
| Splash                      | Redirection selon état connexion |
| Login                       | Succès, erreur, lien Sign up |
| Sign up                     | Création compte, lien Login |
| Accueil (EventScreen)       | Liste événements, drawer, bottom sheet Register/Deregister |
| All Events                  | Liste complète, filtre « Registered », détail |
| Détail événement            | Infos, Register/Deregister, favori, edit (organisateur) |
| Favoris                     | Liste, retrait favori |
| Historique                  | Liste participations, affichage QR |
| Recherche                   | Recherche texte, résultats, ouverture détail |
| My Events                   | Liste organisée, Add Event, Edit, (Delete) |
| Add / Edit Event            | Formulaire, validation, image (imgBB), sauvegarde |
| Scan QR                     | Scan QR participant, validation / erreur |
| Profile                     | Affichage, changement photo, édition infos |
| Drawer                      | Tous les liens (Profile, All Events, My Events, Favourites, Logout) |
| Offline                     | Détection perte connexion, écran dédié, retour après reconnexion |

---

## 10. Tests rapides (smoke test)

1. **Login** → **Accueil** → ouvrir **drawer** → **My Events** → **Add Event** → remplir et ajouter une image → **Save**.
2. Retour **Accueil** → cliquer sur un événement → **Register** → aller dans **Historique** → vérifier le **QR**.
3. **Drawer** → **Profile** → changer la photo de profil.
4. Couper internet → vérifier écran **hors ligne** → rétablir → vérifier retour normal.

Si ces 4 scénarios passent, les fonctionnalités principales sont opérationnelles.

---

## 11. Tests automatisés (optionnel)

Pour aller plus loin, vous pouvez ajouter des tests Flutter :

- **Tests unitaires** : contrôleurs (auth, events, profile), services (imgBB).
- **Tests widget** : écrans login, détail événement, formulaire Add Event.
- **Tests d’intégration** : parcours complet « sign up → login → add event → register → scan QR ».

Exemple pour lancer les tests :

```bash
flutter test
```

---

*Document généré pour Event Booking App – à adapter selon les évolutions du projet.*
