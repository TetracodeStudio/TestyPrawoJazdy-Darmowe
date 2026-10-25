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

| Numer pytania | Pytanie                                                                                                                                                   | Odpowiedź A                                         | Odpowiedź B                                     | Odpowiedź C                                                               | Pytanie ENG                                                                                                                             | Odpowiedź ENG A                                           | Odpowiedź ENG B                                            | Odpowiedź ENG C                                                      | Pytanie DE                                                                                                                                                   | Odpowiedź DE A                                                               | Odpowiedź DE B                                                 | Odpowiedź DE C                                                                       | Poprawna odp | Media             | Zakres struktury | Liczba punktów | Kategorie                   | Nazwa bloku | Nazwa media tłumaczenie migowe (PJM) treść pyt | Nazwa media tłumaczenie migowe (PJM) treść odp A | Nazwa media tłumaczenie migowe (PJM) treść odp B | Nazwa media tłumaczenie migowe (PJM) treść odp C | Nazwa media tłumaczenie migowe (SJM) treść pyt | Nazwa media tłumaczenie migowe (SJM) treść odp A | Nazwa media tłumaczenie migowe (SJM) treść odp B | Nazwa media tłumaczenie migowe (SJM) treść odp C |
| ------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------- | ----------------------------------------------- | ------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------- | ---------------------------------------------------------- | -------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------- | -------------------------------------------------------------- | ------------------------------------------------------------------------------------ | ------------ | ----------------- | ---------------- | -------------- | --------------------------- | ----------- | ---------------------------------------------- | ------------------------------------------------ | ------------------------------------------------ | ------------------------------------------------ | ---------------------------------------------- | ------------------------------------------------ | ------------------------------------------------ | ------------------------------------------------ |
| 99            | Czy w tej sytuacji masz obowiązek zatrzymać pojazd?                                                                                                       |                                                     |                                                 |                                                                           | Are you obliged to stop the vehicle in the presented situation?                                                                         |                                                           |                                                            |                                                                      | Bist du in der dargestellten Situation dazu verpflichtet, das Fahrzeug anzuhalten?                                                                           |                                                                              |                                                                |                                                                                      | T            | AK_D05_06_org.wmv | PODSTAWOWY       | 3              | A,B,C,D,T,AM,A1,A2,B1,C1,D1 | W16         |                                                |                                                  |                                                  |                                                  |                                                |                                                  |                                                  |                                                  |
| 100           | Czy w tej sytuacji masz obowiązek zatrzymać pojazd?                                                                                                       |                                                     |                                                 |                                                                           | Are you obliged to stop the vehicle in the presented situation?                                                                         |                                                           |                                                            |                                                                      | Sind Sie in der dargestellten Situation dazu verpflichtet, das Fahrzeug anzuhalten?                                                                          |                                                                              |                                                                |                                                                                      | T            | AK_D10_30_org.wmv | PODSTAWOWY       | 3              | A,B,C,D,T,AM,A1,A2,B1,C1,D1 | W16         | pjm100.wmv                                     |                                                  |                                                  |                                                  |                                                |                                                  |                                                  |                                                  |
| 109           | Czy w przedstawionej sytuacji masz prawo kontynuować jazdę?                                                                                               |                                                     |                                                 |                                                                           | Can you continue driving in this situation?                                                                                             |                                                           |                                                            |                                                                      | Darfst du in der dargestellten Situation die Fahrt fortsetzen?                                                                                               |                                                                              |                                                                |                                                                                      | N            | AK_D11_43org.wmv  | PODSTAWOWY       | 3              | A,B,C,D,T,AM,A1,A2,B1,C1,D1 | W9          | pjm109.wmv                                     |                                                  |                                                  |                                                  |                                                |                                                  |                                                  |                                                  |
| 110           | Czy w przedstawionej sytuacji masz prawo - mimo podawanego sygnału - skręcić w prawo?                                                                     |                                                     |                                                 |                                                                           | Are you allowed in this situation to turn right despite the light displayed?                                                            |                                                           |                                                            |                                                                      | Darfst du in der dargestellten Situation - trotz des gegebenen Signals - rechts abbiegen?                                                                    |                                                                              |                                                                |                                                                                      | N            | AK_D11_45org.wmv  | PODSTAWOWY       | 3              | A,B,C,D,T,AM,A1,A2,B1,C1,D1 | W9          |                                                |                                                  |                                                  |                                                  |                                                |                                                  |                                                  |                                                  |
| 290           | Jaką odległość należy zachować między jadącymi kolumnami samochodów ciężarowych?                                                                          | Nie mniejszą niż 800 metrów.                        | Nie mniejszą niż 500 metrów.                    | Nie mniejszą niż 200 metrów.                                              | What should be the safe following distance between columns of lorries?                                                                  | Not less than 800 metres.                                 | Not less than 500 metres.                                  | Not less than 200 metres.                                            | Wie groß soll der Abstand sein, der zwischen den fahrenden LKW- Kolonnen, die Straßentransport für eigenen Bedarf ausführen, zu halten ist?                  | Nicht kleiner als 800 meter.                                                 | Nicht kleiner als 500 meter.                                   | Nicht kleiner als 200 meter.                                                         | B            |                   | SPECJALISTYCZNY  | 3              | C                           | C4          |                                                |                                                  |                                                  |                                                  |                                                |                                                  |                                                  |                                                  |
| 293           | Kierujesz samochodem ciężarowym. Który z wymienionych czynników jesteś zobowiązany uwzględnić, aby zachować bezpieczny odstęp od poprzedzającego pojazdu? | Prędkość pojazdów.                                  | Moc silnika.                                    | Szerokość pojazdów.                                                       | You are driving a lorry. Which of the following factors should you take into account to keep a safe distance from the vehicle in front? | Speed of the vehicles.                                    | Power of the engine.                                       | Width of the vehicles.                                               | Sie fahren einen LKW. Welchen der genannten Faktoren haben Sie zu berücksichtigen, damit ein sicherer Abstand zu dem vorausfahrenden Fahrzeug gehalten wird? | Die Geschwindigkeit der Fahrzeuge.                                           | Die Motorleistung.                                             | Die Breite der Fahrzeuge.                                                            | A            |                   | SPECJALISTYCZNY  | 3              | C                           | C4          |                                                |                                                  |                                                  |                                                  |                                                |                                                  |                                                  |                                                  |
| 1864          | Jakiej kategorii prawo jazdy jest wymagane, gdy chcesz kierować czterokołowcem innym niż lekki?                                                           | B1.                                                 | A.                                              | AM.                                                                       | What category driving license should you have when driving a four-wheeled vehicle other than a light one?                               | B1.                                                       | A.                                                         | AM.                                                                  | Welcher Kategorie Führerschein soll man haben, wenn man ein anderes als leichtes Vierrad-Fahrzeug lenkt?                                                     | B1.                                                                          | A.                                                             | AM.                                                                                  | A            |                   | SPECJALISTYCZNY  | 1              | B,B1                        | B8          | pjm1864.wmv                                    | pjm1864a.wmv                                     | pjm1864b.wmv                                     | pjm1864c.wmv                                     |                                                |                                                  |                                                  |                                                  |
| 1866          | Który z obowiązków dotyczy właściciela samochodu osobowego?                                                                                               | Poddawanie pojazdu okresowym badaniom technicznym.  | Zawarcie umowy ubezpieczenia NW.                | Zawarcie umowy ubezpieczenia Autocasco.                                   | What obligation do you have as the owner of a passenger car?                                                                            | Submitting the vehicle to regular technical inspections.  | Concluding the accident insurance.                         | Concluding the motor hull insurance.                                 | Wozu ist man als PKW-Besitzer verfplichtet?                                                                                                                  | Das Fahrzeug muss periodischen technischen Untersuchungen unterzogen werden. | Zum Abschlus einer Unfallversicherung.                         | Zum Abschluss eines Kraftfahrzeug-Kaskoversicherungsvertrages.                       | A            |                   | SPECJALISTYCZNY  | 1              | B                           | B8          | pjm1866.wmv                                    | pjm1866a.wmv                                     | pjm1866b.wmv                                     | pjm1866c.wmv                                     |                                                |                                                  |                                                  |                                                  |
| 1876          | Kiedy powinieneś sprawdzać ciśnienie w ogumieniu samochodu osobowego?                                                                                     | Bezpośrednio przed rozpoczęciem podróży.            | Po zakończeniu jazdy.                           | Co najmniej dobę przed zaplanowaną podróżą.                               | When should you check the pressure in tires of a passenger car?                                                                         | Before the beginning of a journey.                        | After finishing driving.                                   | At least 24 hours before the planned journey.                        | Wann soll man den Reifenluftdruck in einem PKW überprüfen?                                                                                                   | Direkt vor dem Reisebeginn.                                                  | Nach der Beendigung der Fahrt.                                 | Mindestens 24 Stunden vor der eingeplanten Reise.                                    | A            |                   | SPECJALISTYCZNY  | 2              | B                           | B7          |                                                |                                                  |                                                  |                                                  |                                                |                                                  |                                                  |                                                  |
| 1877          | W jaki sposób wolno Ci przewozić ładunek w przyczepie ciągniętej przez samochód osobowy?                                                                  | Ładunek może zasłaniać niektóre światła przyczepy.  | Ładunek nie może naruszać stateczności pojazdu. | Ładunek nie musi być zabezpieczony przed zmianą położenia.                | How can you carry a load on a trailer pulled by a passenger car?                                                                        | The load may cover some lights of the trailer.            | The load must not disturb the steerability of the vehicle. | The load does not have to be secured against the change of position. | Auf welche Art und Weise darf  man  eine Ladung in einem Anhänger transportieren, der von einem PKW gezogen wird?                                            | Die Ladung darf manches Licht des Anhängers verdecken.                       | Die Ladung darf nicht die Stabilität des Fahrzeuges verletzen. | Die Ladung muss nicht gegen die Lageänderung abgesichert sein.                       | B            |                   | SPECJALISTYCZNY  | 2              | B                           | B7          | pjm1877.wmv                                    | pjm1877a.wmv                                     | pjm1877b.wmv                                     | pjm1877c.wmv                                     |                                                |                                                  |                                                  |                                                  |
| 1878          | W jaki sposób przewieziesz ładunek w przyczepie ciągniętej przez samochód osobowy?                                                                        | Zabezpieczę go przed przemieszczaniem.              | Umieszczę ładunek luźno na przyczepie.          | Nie zabezpieczę ładunku przed przemieszczaniem, jeżeli będę jechał wolno. | How will you carry a load on a trailer pulled by a passenger car?                                                                       | I will secure it against moving around.                   | I will place the load loosely on a trailer.                | I will not secure the load against moving around if I drive slowly.  | Auf welche Art und Weise wird eine Ladung im Anhänger transportiert, der von einem PKW gezogen wird?                                                         | Ich sichere die Ladung vor der Verlagerung ab.                               | Ich unterbringe die Ladung unverpackt auf dem Anhänger.        | Ich sichere die Ladung vor der Verlagerung nicht ab, sofern ich langsam fahren will. | A            |                   | SPECJALISTYCZNY  | 2              | B                           | B7          | pjm1878.wmv                                    | pjm1878a.wmv                                     | pjm1878b.wmv                                     | pjm1878c.wmv                                     |                                                |                                                  |                                                  |                                                  |
| 1879          | Jak przewieziesz ładunek sypki w przyczepie ciągniętej przez samochód osobowy?                                                                            | Niezasłonięty - umieszczony poniżej wysokości burt. | Nie przekraczajac prędkości 30 km/h.            | Zasłonięty w szczelnej skrzyni ładunkowej.                                | How will you carry a loose load on a trailer pulled by a passenger car?                                                                 | Not covered - placed below the height of the side boards. | Not exceeding the speed of 30 km/h.                        | Covered in a tight load-carrying platform.                           | Auf welche Art und Weise wird das Schüttgut in einem Anhänger transportiert, der von einem PKW gezogen wird?                                                 | Nicht zugedeckt - untergebracht unterhalb der Ladebordwand..                 | Ohne die Geschwindigkeit von 30 km/h zu überschreiten.         | In einer dichten Ladekiste verschlossen.                                             | C            |                   | SPECJALISTYCZNY  | 2              | B                           | B7          | pjm1879.wmv                                    | pjm1879a.wmv                                     | pjm1879b.wmv                                     | pjm1879c.wmv                                     |                                                |                                                  |                                                  |                                                  |
| 1880          | Jak powinieneś zabezpieczyć urządzenie, którym zamocowałeś ładunek w przyczepie ciągniętej przez samochód osobowy?                                        | Tak, żeby mocowanie nie rozluźniło się.             | Może swobodnie zwisać z przyczepy.              | Nie musi być zabezpieczone przed spadnięciem podczas jazdy.               | How should the device used for fixing the load on a trailer pulled by a passenger car be secured?                                       | It should be secured against loosening.                   | It may hang down freely.                                   | It does not have to be secured against falling down during driving.  | Wie soll die Einrichtung gesichert sein, mit der eine Ladung auf dem Anhänger befestigt wurde, der durch einen PKW gezogen wird?                             | Sie ist davor zu sichern, dass sie nicht locker wird.                        | Sie kann locker von dem Anhäger herunterhängen.                | Sie muss nicht davor gesichert sein, dass sie während der Fahrt herunterfällt.       | A            |                   | SPECJALISTYCZNY  | 2              | B                           | B7          | pjm1880.wmv                                    | pjm1880a.wmv                                     | pjm1880b.wmv                                     | pjm1880c.wmv                                     |                                                |                                                  |                                                  |                                                  |
| 1881          | W jaki sposób powinien być umieszczony ładunek na samochodzie osobowym?                                                                                   | Może nieznacznie zasłaniać tablice rejestracyjne.   | Może zasłaniać tylne światło przeciwmgłowe.     | Nie może ograniczać widoczności drogi.                                    | How should a load be placed on a passenger car?                                                                                         | It may slightly cover the registration plates.            | It may cover the rear fog light.                           | It must not limit the visibility of the road.                        | Auf welche Art und Weise ist eine Ladung auf einem PKW zu verstauen?                                                                                         | Die Ladung darf geringfügig die Nummernschilder verdecken.                   | Die Ladung darf das Rücknebellicht verdecken.                  | Die Ladung darf die Sicht nicht verdecken.                                           | C            |                   | SPECJALISTYCZNY  | 2              | B                           | B7          | pjm1881.wmv                                    | pjm1881a.wmv                                     | pjm1881b.wmv                                     | pjm1881c.wmv                                     |                                                |                                                  |                                                  |                                                  |---

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
