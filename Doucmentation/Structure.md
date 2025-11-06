### core (Module de projet)
Contient le code `Java` (logique, classes, gameplay).

### lwjgl3 (Module de projet)
Lanceur Desktop. `LWJGL3` est la bibliothèque qui crée la fenêtre du jeu.
Contient le point d'entrée `Lwjgl3Launcher`.
Configuration de la fenêtre (taille, titre, icône).

### assets (Module de projet)
Contient les fichiers de ressources du jeu (images, sons, maps, polices de caractères, fichiers `JSON` de configuration).

### build.gradle (Fichier de configuration)
Script principal, définissant les versions de `libGDX`, les dépendances communes à tous les modules, la configuration des tâches Gradle (compilation et tests).
Chaque module possède son propre fichier de configuration `build.gradle`.

### settings.gradle (Fichier de configuration)
Définit les modules inclus dans le projet.

### gradle.properties (Fichier de configuration)
Stockage des variables de configuration globales utilisées dans `build.gradle` (versions de dépendances, options JVM).

### gradlew(Linux) / gradlew.bat(Microsoft) (Scripts Gradle Wrapper)
Wrapper Gradle, permettent l'exécution des tâches Gradle (`./gradlew run`, `./gradlew test`).