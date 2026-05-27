# Showtime - Projekat 2

Pocetni Compose Multiplatform projekat za RMA Projekat 2.

U ovom commitu postoji samo osnovna struktura projekta:

- `androidApp` - Android ulaz u aplikaciju
- `desktopApp` - JVM/Desktop ulaz u aplikaciju
- `composeApp` - zajednicki Compose kod

Za sada aplikacija prikazuje samo pocetni ekran. Sledeci commitovi ce postepeno dodavati core sloj, network, DataStore, Room, repository, MVI ekrane, katalog, kviz i profil.

## Pokretanje

Android:

```powershell
./gradlew :androidApp:installDebug
```

Desktop:

```powershell
./gradlew :desktopApp:run
```
