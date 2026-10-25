# 🚗 Testy Prawo Jazdy — Kotlin Multiplatform App

[![Kotlin Multiplatform](https://img.shields.io/badge/Kotlin-Multiplatform-blue.svg?logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![Compose Multiplatform](https://img.shields.io/badge/Compose-Multiplatform-teal.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Platform Support](https://img.shields.io/badge/Platform-Android%20%7C%20Windows%20%7C%20macOS%20%7C%20Linux-brightgreen.svg)]()

**Testy Prawo Jazdy** to nowoczesna, wieloplatformowa aplikacja (Android, Windows, macOS, Linux) przeznaczona do nauki i symulacji państwowego egzaminu na prawo jazdy (zgodnie z wymogami WORD). Aplikacja została zbudowana przy użyciu **Kotlin Multiplatform** oraz **Compose Multiplatform**, co zapewnia spójny interfejs oraz wysoką wydajność na wszystkich urządzeniach.

---

## 📋 Spis treści

- [✨ Funkcjonalności](#-funkcjonalności)
- [📐 Architektura i Technologie](#-architektura-i-technologie)
- [📥 Jak wgrywać i importować dane](#-jak-wgrywać-i-importować-dane)
  - [1. Struktura pliku pytań (.xlsx)](#1-struktura-pliku-pytań-xlsx)
  - [2. Struktura multimediów (.zip)](#2-struktura-multimediów-zip)
  - [3. Instrukcja importu krok po kroku](#3-instrukcja-importu-krok-po-kroku)
- [📂 Struktura przechowywania danych](#-struktura-przechowywania-danych)
- [🛠️ Kompilacja i Uruchomienie](#-kompilacja-i-uruchomienie)
- [🔄 Mechanizm Aktualizacji](#-mechanizm-aktualizacji)
- [📄 Licencja](#-licencja)

---

## ✨ Funkcjonalności

- **🎯 Symulacja Egzaminu Państwowego (WORD)**:
  - Dokładne odzwierciedlenie zasad egzaminu: **32 pytania** (20 podstawowych TAK/NIE + 12 specjalistycznych A/B/C).
  - Waga punktowa pytań (1, 2 lub 3 punkty) — łącznie **74 punkty**.
  - Próg zdawalności: **minimum 68 punktów**.
- **📚 Tryb Nauki i Praktyki**:
  - Przeglądanie pełnej bazy pytań według kategorii (**A, A1, A2, AM, B, B1, C, C1, D, D1, T**).
  - Wyświetlanie poprawnych odpowiedzi, statystyk opanowania pytań oraz śledzenie postępów.
- **🎬 Obsługa Multimediów**:
  - Odtwarzanie zdjęć, ilustracji oraz wideo w wysokiej rozdzielczości (formaty `.mp4`, `.wmv`, `.mkv` i inne).
  - Integracja z silnikiem **VLCJ** (Desktop) oraz natywnymi odtwarzaczami Android.
- **🌐 Wielojęzyczność i PJM**:
  - Obsługa pytań w języku polskim, angielskim, niemieckim oraz ukraińskim.
  - Obsługa wideo z Polskim Językiem Migowym (PJM).
- **📊 Historia i Statystyki**:
  - Śledzenie procentu zdawalności, średniego wyniku oraz historii podejść do egzaminu.
- **🎨 Personalizacja**:
  - Obsługa motywu jasnego, ciemnego oraz dostosowanego do systemu.
  - Różnorodne palety kolorystyczne (Niebieska, Zielona, Fioletowa, Pomarańczowa, Czerwona, Szara).
- **💾 Elastyczny Magazyn Danych**:
  - Przechowywanie danych w wybranym katalogu na dysku/pamięci urządzenia (idealne rozwiązanie dla dużych baz mediów sięgających 8+ GB).

---

## 📐 Architektura i Technologie

- **Język**: Kotlin 2.x (Multiplatform)
- **UI Framework**: Compose Multiplatform (Material 3)
- **Asynchroniczność**: Kotlin Coroutines & StateFlow
- **Serializacja**: `kotlinx.serialization` (JSON)
- **Multimediów & Pliki**: Okio, VLCJ (Desktop), ExoPlayer/Natywne dekodery (Android)
- **Parsery**: Natywny parser XML/XLSX oraz natywny bezbiblioteczny unzipper archiwów ZIP (strumieniowe rozpakowywanie dużych archiwów).

---

## 📥 Jak wgrywać i importować dane

Aplikacja pozwala na dynamiczne wgrywanie bazy pytań oraz plików multimedialnych bez konieczności ponownej kompilacji programu.

### 1. Struktura pliku pytań (.xlsx)

Baza pytań importowana jest z arkusza Excel (`.xlsx`) lub pliku `.zip` zawierającego rozpakowany arkusz. Parser odczytuje dane z arkusza wedle poniższego schematu kolumn:

| Kolumna | Nazwa / Opis | Przykładowa wartość | Klasa / Typ |
| :---: | :--- | :--- | :--- |
| **A / B** | Numer / ID pytania | `1234` | String |
| **C** | Treść pytania (PL) | `Czy w tej sytuacji masz pierwszeństwo przejazdu?` | String |
| **D** | Odpowiedź A (dla pytań ABC) | `Skręcić w lewo` | String (opcjonalnie) |
| **E** | Odpowiedź B (dla pytań ABC) | `Kontynuować jazdę prostoliniowo` | String (opcjonalnie) |
| **F** | Odpowiedź C (dla pytań ABC) | `Zatrzymać pojazd` | String (opcjonalnie) |
| **G** | Poprawna odpowiedź | `TAK`, `NIE`, `A`, `B` lub `C` | CorrectAnswer Enum |
| **H** | Nazwa pliku multimedialnego | `D17_1080.wmv` lub `obraz1.jpg` | String (opcjonalnie) |
| **I** | Zakres pytania | `PODSTAWOWY` lub `SPECJALISTYCZNY` | QuestionScope Enum |
| **J** | Punktacja za pytanie | `1`, `2` lub `3` | Int |
| **K** | Kategorie (oddzielone przecinkami) | `A, A1, B, B1, C` | List<String> |
| **L** | Nazwa pliku z migowym (PJM) | `pjm_1234.mp4` | String (opcjonalnie) |
| **P** | Pytanie w języku angielskim | `Do you have the right of way in this situation?` | String (opcjonalnie) |
| **T** | Pytanie w języku niemieckim | `Haben Sie in dieser Situation Vorfahrt?` | String (opcjonalnie) |
| **X** | Pytanie w języku ukraińskim | `Чи маєте ви перевагу в цій ситуації?` | String (opcjonalnie) |

---

### 2. Struktura multimediów (.zip)

Wszystkie materiały wideo oraz zdjęcia wykorzystywane w pytaniach należy umieścić w archiwum plikowym `.zip` (lub podzielonym na części archiwum wieloczęściowym `.zip`).

- **Format plików mediów**: `.jpg`, `.jpeg`, `.png`, `.wmv`, `.mp4`, `.mkv`, `.avi`.
- Nazwy plików w archiwum powinny odpowiadać wartościom w kolumnie **H** (lub **L**) arkusza Excel.
- Parser automatycznie normalizuje nazwy plików oraz spłaszcza zagnieżdżone katalogi (np. `dane/pliki/media.wmv` zostanie automatycznie przypisane jako `media.wmv`).

---

### 3. Instrukcja importu krok po kroku

1. Uruchom aplikację i przejdź do **Ustawień** (ikona zębatki w prawym górnym rogu) lub ekranu **Import danych**.
2. **(Opcjonalnie)** W sekcji **Katalog przechowywania danych** wybierz folder na dysku, w którym mają być zapisane rozpakowane multimedia.
3. W sekcji **Baza pytań**:
   - Kliknij **Wybierz plik pytań** i wskaż Twój plik arkusza `.xlsx` lub archiwum `.zip`.
4. W sekcji **Pliki multimedialne**:
   - Kliknij **Wybierz części multimediów** i zaznacz plik(i) `.zip` zawierające zdjęcia oraz wideo.
5. Kliknij przycisk **Rozpocznij import**.
6. Aplikacja przeanalizuje i zapisze baze pytań w formacie JSON oraz rozpakuje pliki multimedialne do wybranego katalogu dyskowego.

---

## 📂 Struktura przechowywania danych

Po załadowaniu bazy danych aplikacja przechowuje swoje pliki robocze w ustalonym katalogu (domyślnie w pamięci lokalnej aplikacji lub w wybranej ścieżce użytkownika):

```text
📁 katalog_danych/
├── 📄 config.json           # Ustawienia motywu, palety i wybranej ścieżki
├── 📄 questions.json        # Przetworzona baza pytań (JSON)
├── 📄 exam_history.json     # Zapisana historia wyników egzaminów
├── 📄 learning_progress.json# Zapisane id opanowanych pytań w trybie nauki
└── 📁 media/                # Rozpakowane pliki graficzne i wideo
    ├── 🏞️ obraz1.jpg
    ├── 🎬 D17_1080.wmv
    └── 🎬 pjm_1234.mp4
```

---

## 🛠️ Kompilacja i Uruchomienie

### Wymagania wstępne:
- **JDK 17** lub nowszy
- **Android Studio** (wersja Ladybug / 2024.2 lub nowsza) z wtyczką Kotlin Multiplatform

### 📱 Android

Kompilacja i uruchomienie na podłączonym urządzeniu lub emulatorze:

```bash
# Zbudowanie pliku APK
./gradlew :app:assembleDebug

# Uruchomienie aplikacji na emulatorze/urządzeniu
./gradlew :app:installDebug
```

---

### 💻 Desktop (Windows / macOS / Linux)

Uruchomienie wersji klasycznej na komputerze:

```bash
# Uruchomienie w trybie deweloperskim
./gradlew :app:run

# Paczkowanie instalatora (np. .exe na Windows, .dmg na macOS, .deb na Linux)
./gradlew :app:packageReleaseApp
```

---

## 🔄 Mechanizm Aktualizacji

Aplikacja posiada wbudowany mechanizm sprawdzania aktualizacji drogą internetową:
- Plik oprogramowania weryfikuje wpis w konfiguracji zdalnej `update.json` z repozytorium GitHub Releases.
- W przypadku dostępności nowszej wersji (`versionCode`) użytkownik otrzymuje powiadomienie z możliwością pobrania pliku instalacyjnego `.apk` (Android) lub `.exe` (Desktop).

---

## 📄 Licencja

Projekt dystrybuowany jest na zasadach licencji Open Source.  
Created with ❤️ using Kotlin Multiplatform.
