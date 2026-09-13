# PadelJL

Een simpele padel-scoreteller voor Android — op je telefoon en op je Wear OS-horloge, met live synchronisatie tussen beide.

- **Live Score** — laat je horloge de telling doen, de telefoon toont de stand groot in landscape.
- **Zelf Tellen** — tel de wedstrijd rechtstreeks op je telefoon, voor als je het horloge een keer niet om hebt.
- **Historie** — eindstanden en spelersnamen van eerdere wedstrijden.
- Volledige game/set/tiebreak-logica volgens de officiële padelregels (incl. golden point en tiebreak-serveerrotatie).

Er is ook een **losstaande Garmin-versie** (Connect IQ), o.a. voor de Epix 2 — zie de [Garmin-sectie hieronder](#-garmin-horloge) en [`garmin/README.md`](garmin/README.md) voor de volledige details.

## 📲 Installeren op je Android-telefoon

De app staat niet in de Play Store — je installeert 'm rechtstreeks via een APK-bestand ("sideloaden"). Dat is veilig zolang je het bestand van deze repo haalt.

1. Ga op je telefoon naar de **[Releases-pagina](../../releases/latest)** van deze repo.
2. Download onder "Assets" het bestand **`PadelJL.apk`**.
3. Open het gedownloade bestand. Android vraagt de eerste keer om toestemming om apps van deze bron te installeren:
   - Ga naar **Instellingen → Apps → Speciale toegang → Onbekende apps installeren**
   - Zet dit aan voor de app waarmee je het bestand hebt gedownload (bijv. Chrome of "Bestanden")
4. Tik op **Installeren**.
5. Klaar — open PadelJL vanuit je app-lijst.

### Op je Wear OS-horloge

Wear OS heeft geen simpele "tik om te installeren"-optie voor bestanden zoals een telefoon — daarvoor is ADB nodig (dit werkt op vrijwel elk Wear OS-horloge):

1. Download op je computer het bestand **`PadelJL-Wear.apk`** van dezelfde **[Releases-pagina](../../releases/latest)**.
2. Zet op het horloge **Ontwikkelaarsopties** aan: Instellingen → Over → tik 7x op "Build-nummer".
3. Zet in Ontwikkelaarsopties **Wifi-foutopsporing** aan; het horloge toont een IP-adres en poort.
4. Verbind vanaf je computer:
   ```bash
   adb connect <ip-adres>:<poort>
   adb install PadelJL-Wear.apk
   ```

Zorg dat je telefoon en horloge al gekoppeld zijn via de Wear OS-app, zodat de twee apps met elkaar kunnen synchroniseren.

## ⌚ Garmin-horloge

Voor de **Garmin Epix 2** staat er een kant-en-klaar bestand klaar — geen SDK of command line nodig:

1. Ga naar de **[Releases-pagina](../../releases/latest)** en download **`PadelJL-epix2.prg`**.
2. Sluit je Epix 2 via USB aan op je computer.
3. Kopieer `PadelJL-epix2.prg` naar de map `GARMIN/APPS/` op de horloge-schijf.
4. Koppel los — de app verschijnt in het app-menu.

Bediening: **UP** = punt voor WIJ, **DOWN** = punt voor ZIJ, **MENU** (lang indrukken) = ongedaan maken/stoppen, **BACK** = vraagt om te stoppen. Werkt zowel met fysieke knoppen als met tikken/vegen op touchscreen-modellen.

Ander Garmin-model, of zelf de broncode aanpassen? Zie [`garmin/README.md`](garmin/README.md) voor de developer-instructies (SDK, compileren, etc.).

## 🛠️ Zelf bouwen (voor developers)

```bash
git clone <deze-repo-url>
cd PadelJL
./gradlew :mobile:assembleDebug :wear:assembleDebug
```

De APK's staan daarna in `mobile/build/outputs/apk/debug/` en `wear/build/outputs/apk/debug/`.

## 📄 Licentie

MIT © 2026 Justinlampy — zie [LICENSE](LICENSE).
