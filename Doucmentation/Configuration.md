### LibGDX Setup Tool
Téléchargement ` gdx-liftoff-1.14.0.0.jar`
- Lancement outil de configuration : `java -jar gdx-liftoff-1.14.0.0.jar`

**Project Name :** `Despotisme`  
**Package :** `com.despotisme`  
**Main Class :** `Despotisme`

### Lancement du jeu
`./gradlew lwjgl3:run`

Vérification de `gradle/gradle-properties-jvm.properties` :

```bash
toolchainUrl.WINDOWS.X86_64=https\://api.foojay.io/disco/v3.0/ids/2d57bdd1e17a18f83ff073919daa35ba/redirect
toolchainVersion=21
```

Car configuration de base :

```bash
Daemon JVM:    Compatible with Java 17, any vendor, nativeImageCapable=false (from gradle/gradle-daemon-jvm.properties)
```