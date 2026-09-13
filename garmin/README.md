# PadelJL — Garmin (Connect IQ)

⚠️ **Experimenteel.** Deze versie is geschreven maar **nog nooit gecompileerd of getest** — er is hier geen Garmin-horloge of Connect IQ SDK beschikbaar om dat te doen. De scorelogica (games/sets/tiebreak/golden point) is 1-op-1 overgenomen uit de al uitgebreid geteste Android-versie (`mobile/src/main/java/com/example/padeljl/PhoneMatchEngine.kt`), maar de rest (schermen, knop-afhandeling, menu's) is alleen "op papier" gecontroleerd, niet gedraaid. Verwacht dus dat je bij het compileren nog kleine dingen moet fixen.

Losstaande app (geen synchronisatie met de telefoon) — je telt de wedstrijd rechtstreeks op het horloge.

## Wat je nodig hebt

- Windows, macOS of Linux (geen Mac nodig, in tegenstelling tot Apple Watch)
- [Connect IQ SDK Manager](https://developer.garmin.com/connect-iq/sdk/) — gratis, geen developer-account nodig voor lokaal gebruik/sideloaden
- Een Garmin-horloge met Connect IQ-ondersteuning (of gebruik de ingebouwde simulator zonder horloge)

## Installeren & compileren

1. Download en installeer de **Connect IQ SDK Manager**: https://developer.garmin.com/connect-iq/sdk/
2. Open de SDK Manager, accepteer de licentie, en laat 'm de laatste SDK + device-bibliotheken downloaden (eenmalig, duurt even).
3. Genereer een developer-key (eenmalig, alleen nodig om lokaal te bouwen/sideloaden):
   ```bash
   openssl genrsa -out developer_key.pem 4096
   openssl pkcs8 -topk8 -inform PEM -outform DER -in developer_key.pem -out developer_key.der -nocrypt
   ```
4. Compileer (pas het pad naar de SDK en je device-model aan):
   ```bash
   monkeyc -f monkey.jungle -o bin/PadelJL.prg -y developer_key.der -d fenix7
   ```
   - Krijg je een fout over een "unknown product id"? Voeg jouw exacte model toe aan `manifest.xml` onder `<iq:products>` — makkelijkst via de "Monkey C: Edit Products" opdracht in VS Code (met de Monkey C-extensie), die laat je uit een lijst van alle échte geldige device-ID's kiezen.
5. **Testen zonder horloge**: start de simulator en laad de app erin:
   ```bash
   connectiq
   monkeydo bin/PadelJL.prg fenix7
   ```

## Op het horloge zetten (sideloaden)

1. Sluit het horloge via USB aan op je computer (verschijnt als een schijf/drive).
2. Kopieer `bin/PadelJL.prg` naar de map `GARMIN/APPS/` op die schijf.
3. Koppel het horloge los, de app verschijnt in het app-menu.

## Bediening

Gebouwd met Garmin's cross-device `BehaviorDelegate`, dus dit werkt zowel met fysieke knoppen als met tikken/vegen op touchscreens:

- **UP** (of veeg omlaag) → punt voor WIJ
- **DOWN** (of veeg omhoog) → punt voor ZIJ
- **MENU** (lang indrukken) → Ongedaan maken / Stoppen
- **BACK** → vraagt om te stoppen (voorkomt per ongeluk afsluiten)

## Bekende beperkingen / nog te doen

- Geen opslag van wedstrijdgeschiedenis (bewust simpel gehouden, "losstaand").
- Geen synchronisatie met de telefoon-app.
- De lijst met ondersteunde toestellen in `manifest.xml` is een startpunt — voeg je eigen model toe indien nodig.
- Nog niet gecompileerd/getest door iemand — meld het als je tegen iets aanloopt.
