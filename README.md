# EU Tiers Tagger (Fabric 1.21.11)

Mod **cliente** para Minecraft 1.21.11 (Fabric) que muestra los tiers de tu
EU Tierlist al lado del nombre de los jugadores, en la lista de tabuladores
(Tab) y mediante el comando `/eutiers <usuario>`.

Los datos salen de tu propio bot de Discord: el mod descarga el endpoint
`/player_rankings.json` que tu `bot.py` ya sirve, y empareja a cada jugador por
su nombre de Minecraft (IGN).

---

## 1. Compilar el mod

Necesitas **JDK 21** instalado.

```bash
# En la carpeta del proyecto:
./gradlew build        # Linux / macOS
gradlew.bat build      # Windows
```

El `.jar` final queda en:

```
build/libs/eutiers-tagger-1.0.0.jar
```

> Usa el que **no** termina en `-sources.jar`.

La primera compilación tarda (descarga Minecraft, Yarn y Fabric API).

### Si Gradle se queja de la versión

Las versiones están en `gradle.properties` y se sacan de
<https://fabricmc.net/develop>. Si en el futuro hay un build de Yarn más nuevo
para 1.21.11, o Loom pide otra versión de Gradle, ajusta ahí (o cambia
`distributionUrl` en `gradle/wrapper/gradle-wrapper.properties`).

---

## 2. Instalar

1. Instala **Fabric Loader** para 1.21.11 (instalador oficial de fabricmc.net).
2. Copia en tu carpeta `mods/`:
   - `eutiers-tagger-1.0.0.jar`
   - **Fabric API** para 1.21.11 (`fabric-api-0.141.4+1.21.11.jar` o superior).
3. Arranca el juego una vez para generar la configuración.

---

## 3. Configurar

Tras el primer arranque se crea:

```
.minecraft/config/eutiers-tagger.json
```

```jsonc
{
  "baseUrl": "https://YOUR-BOT-URL.up.railway.app",
  "showOnNametags": true,
  "showInTabList": true,
  "displayMode": "HIGHEST",
  "showModeName": true,
  "refreshIntervalSeconds": 120
}
```

- **baseUrl** — la URL pública de tu bot (la que te da Railway), **sin** barra
  final. El mod pedirá `baseUrl + "/player_rankings.json"`.
- **displayMode** — `"HIGHEST"` muestra el mejor tier del jugador, o pon un
  modo concreto: `SWORD`, `AXE`, `UHC`, `NETHPOT`, `POT`, `MACE`, `CRYSTAL`, `SMP`.
- **showModeName** — `true` → `[HT1 Sword]`; `false` → `[HT1]`.

Edita el archivo y reinicia el juego para aplicar los cambios.

### ¿Cuál es mi baseUrl en Railway?

En el panel de Railway, tu servicio tiene un dominio público
(Settings → Networking → *Public Domain*), del tipo
`https://eu-tierlist-production.up.railway.app`. Esa es la `baseUrl`
(Railway expone HTTPS en el 443, **no** hace falta poner el puerto 25332).

Comprueba que funciona abriéndolo en el navegador:
`https://TU-DOMINIO.up.railway.app/player_rankings.json`
Deberías ver el JSON con los jugadores.

---

## 4. Uso en el juego

- **Nametag / Tab**: el tier aparece solo al lado del nombre de los jugadores
  que estén en tu tierlist.
- **Comando**: `/eutiers <usuario>` muestra todos los tiers de esa persona.
  (Escríbelo en minúsculas: `/eutiers Steve`).

---

## Cómo funciona (resumen técnico)

- `TierManager` descarga `player_rankings.json` cada `refreshIntervalSeconds`
  en un hilo de fondo y lo cachea por IGN (en minúsculas).
- `PlayerListHudMixin` engancha `PlayerListHud#getPlayerName` → añade el tag en Tab.
- `EntityRendererMixin` engancha `EntityRenderer#getDisplayName` → añade el tag
  en el nombre flotante (en 1.21.11 el render de nombres cambió de firma, por eso
  se usa `getDisplayName` y no `renderLabelIfPresent`).

No hace falta tocar el bot: ya sirve el endpoint que el mod necesita.

## Versiones

| Componente      | Versión            |
|-----------------|--------------------|
| Minecraft       | 1.21.11            |
| Yarn mappings   | 1.21.11+build.4    |
| Fabric Loader   | 0.18.1             |
| Fabric API      | 0.141.4+1.21.11    |
| Loom            | 1.14-SNAPSHOT      |
| Java            | 21                 |
