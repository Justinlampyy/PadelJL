# PadelJL — Garmin (Connect IQ)

Losstaande app (geen synchronisatie met de telefoon) — je telt de wedstrijd rechtstreeks op het horloge.

## 📲 Installeren op je Garmin Epix 2

Geen SDK, geen developer-account, geen command line nodig — er staat een kant-en-klaar bestand klaar op de releases-pagina.

1. Ga op je computer naar de **[Releases-pagina](../../releases/latest)** van deze repo.
2. Download onder "Assets" het bestand **`PadelJL-epix2.prg`**.
3. Sluit je Epix 2 via USB aan op je computer — hij verschijnt als een schijf/drive.
4. Kopieer `PadelJL-epix2.prg` naar de map `GARMIN/APPS/` op die schijf.
5. Koppel het horloge los. De app verschijnt in het app-menu (in Connect Mobile op je telefoon moet je 'm mogelijk nog even "toestaan" bij de eerste sync).

### Bediening

Werkt zowel met de fysieke knoppen als met tikken/vegen op het touchscreen:

- **UP** (of veeg omlaag) → punt voor WIJ
- **DOWN** (of veeg omhoog) → punt voor ZIJ
- **MENU** (lang indrukken) → Ongedaan maken / Stoppen
- **BACK** → vraagt om te stoppen (voorkomt per ongeluk afsluiten)

## 🛠️ Zelf bouwen / aanpassen (voor developers)

Nodig als je de broncode wilt wijzigen, voor een ander Garmin-model wilt bouwen, of zelf een nieuwe `.prg` wilt maken.

1. Installeer de gratis **[Connect IQ SDK Manager](https://developer.garmin.com/connect-iq/sdk/)** (Windows/macOS/Linux) en laat 'm de SDK downloaden.
2. Ga in de SDK Manager naar het tabblad **Devices**, log in met een gratis Connect IQ developer-account, en download je doeltoestel (voor de Epix 2 zoek je specifiek op **"epix2"** — niet op "epix", dat is een ander, ouder model).
3. Genereer eenmalig een developer-key:
   ```bash
   openssl genrsa -out developer_key.pem 4096
   openssl pkcs8 -topk8 -inform PEM -outform DER -in developer_key.pem -out developer_key.der -nocrypt
   ```
4. Compileer vanuit de `garmin/`-map (vervang `epix2` door jouw model als nodig):
   ```bash
   monkeyc -f monkey.jungle -o bin/PadelJL-epix2.prg -y developer_key.der -d epix2
   ```
   - "Unknown product id"-fout? Voeg je horlogemodel toe aan `garmin/manifest.xml` onder `<iq:products>`.
5. Testen zonder horloge: start de simulator (`connectiq`) en laad de app erin (`monkeydo bin/PadelJL-epix2.prg epix2`).
6. Op het horloge zetten: zie de installatiestappen hierboven, met jouw eigen gebouwde `.prg`-bestand.

## Bekende beperkingen

- Geen opslag van wedstrijdgeschiedenis (bewust simpel gehouden, "losstaand").
- Geen synchronisatie met de telefoon-app.
- Getest via de compiler (bouwt succesvol, geen bekende bugs in de scorelogica — 1-op-1 overgenomen uit de al uitgebreid geteste Android-versie), maar nog niet op een fysiek horloge gedraaid. Loop je tegen iets aan, meld het.
