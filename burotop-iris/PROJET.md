# Burotop Iris – Notes de projet

## Décision (réunion du 2026-11-02)
Burotop Iris **ne souhaite pas** la plateforme complète ni le dashboard :
ils disposent d'un système interne qu'ils ne veulent **surtout pas toucher** (sécurité).

Pistes qui les intéressent :
1. **Site web vitrine** : consultation des articles uniquement (pas de commande, pas de back-office).
2. **Logiciel de SAV** (service après-vente) : à cadrer.

## Contraintes
- Aucune connexion au système interne de Burotop. Le catalogue est alimenté par un
  export fourni par eux (CSV/JSON), mis à jour à leur initiative.
- Pas de données clients ni sensibles dans le site vitrine.
- Le SAV doit être autonome (sa propre base), sans accès à leur système.

## État
- Rien de codé. (Un squelette Vite + React + PWA avait été commencé, perdu à la
  réinitialisation du conteneur car non commité.)
- Branding : bleu marine (logo), orange et gris (icône), slogan « Notre expertise à votre service ».
- SAV, questions à poser : tickets, suivi de réparations, garanties, notifications clients,
  utilisateurs (atelier / accueil / clients) ?
