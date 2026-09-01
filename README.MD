# Aquarium WoT - Android Client

Un client Android per la gestione remota e il monitoraggio di acquari intelligenti basati su architettura Web of Things (WoT).

Lo scopo è quello di un esercizio utile a studiare come realizzare app di controllo "di prossimità", ad esempio per dispositivi industriali che potrebbero non avere (per varie motivazioni contingenti) console o periferiche proprie.  

Questo progetto si aggancia direttamente ad un altro progetto d'esame ([WoT](https://github.com/tiralongofabio/Aquarium-WoT-BLE)) presentato poco tempo fa e riadattato per quanto necessario al funzionamento della nuova app.

NB: App testata su Android 14 e successivi

---

## Architettura e Stack Tecnologico

L'applicazione sposa il principio della **Clean Architecture** , garantendo isolamento delle competenze dei vari moduli e agevolando così la scalavilità e testabilità della soluzione.
La concorrenza è gestita interamente tramite **Kotlin Coroutines** e **Flow**, evitando l'uso dell'obsoleto `AsyncTask`.

Il ricorso a Foreground Service e Background Service è stato testato, valutato e poi accantonato, in quanto non funzionale alla demo (es. vincolo di delay di almeno 15 minuti per i processi di notifica in background)

### Moduli
*   **`:app`**: Modulo di entry-point. Contiene i flavor di build (`emulator`, `device`), necessari alle diverse gestione dei mock contingenti, l'integrazione Hilt e le regole R8/ProGuard (in parte poi demandate per competenza a `:domain` e `:data`.
*   **`:domain`**: Il cuore della business logic. Totalmente agnostico dal framework Android. Contiene i modelli dati puri (`WaterQuality`, `PumpState`), interfacce repository e Use Cases.
*   **`:data`**: Implementazione del livello dati. Gestisce il database locale (Room), il client HTTP (Retrofit/OkHttp), lo scanner BLE e il generatore TOTP.
*   **`:uicompose`**: Livello di presentazione costruito al 100% in **Jetpack Compose** (Material Design 3), con gestione del routing tramite Navigation Compose.

---

## Sicurezza e Privacy (Security by Design)

*   **Autenticazione TOTP Dinamica**: I dispositivi sono accoppiati tramite un Secret scambiato via QR-Code. Un `Interceptor` OkHttp genera e inietta dinamicamente un token TOTP (HMAC-SHA1) in ogni richiesta HTTP.
*   **Archiviazione Sicura**: Le credenziali dei dispositivi WoT (ID e Secret) non sono salvate in chiaro, ma crittografate tramite `EncryptedSharedPreferences` (AES256_GCM).
*   **Network Security Config**: Il traffico HTTP in chiaro è bloccato per le build di produzione. Sono permesse eccezioni mirate esclusivamente agli IP di test (es. localhost emulatore).
*   **Offuscamento R8**: Configurazione ProGuard avanzata per proteggere la logica applicativa dal reverse-engineering, mantenendo intatti unicamente i modelli di deserializzazione Gson.

---

## Accessibilità (A11y) e Inclusività

L'interfaccia non si limita all'estetica, ma è progettata per essere utilizzabile da chiunque, rispettando le linee guida WCAG:

*   **Tema ad Alto Contrasto (AAA)**: L'app intercetta dinamicamente le preferenze del sistema operativo (`Settings.Secure`). Se l'utente necessita di alto contrasto, l'app disabilita il tema Dark standard (anti-smearing) in favore di una palette a contrasto estremo (sfondo nero puro, testo bianco puro, colori d'allerta accessi).
*   **Semantica Avanzata per Screen Reader**: Utilizzo sistematico di `mergeDescendants = true` e `stateDescription` personalizzate. TalkBack raggruppa in modo intelligente i dati complessi (es. "Pompa Filtro: Attiva al 70 percento") evitando scorrimenti frammentati e frustranti per gli utenti non vedenti.
*   **Tipografia Cognitiva**: Font scalabili (SP) con interlinea (line-height) calcolata a 1.5x per favorire la leggibilità e supportare gli utenti con dislessia.
*   **Localizzazione (i18n)**: Supporto nativo completo per le lingue Inglese (fallback) e Italiano tramite `strings.xml`.

---

## Funzionalità Principali

*   **Monitoraggio Real-time**: Lettura e visualizzazione dello stato dell'acquario (Temperatura, pH, Livello O2).
*   **Controllo Attuatori**: Gestione della pompa filtro (accensione, spegnimento, ciclo di pulizia).
*   **Configurazione Range**: Interfaccia per la calibrazione delle soglie di tolleranza (limiti minimi/massimi).
*   **Registro/Display delle Notifiche**: un `AlertMonitorManager` popolato costantemente in background per il logging locale di allerte (Warning/Critical) basate sullo scostamento dai range ottimali.
*   **Connettività Ibrida**: (da testare) Supporto al discovery e fallback tramite Bluetooth Low Energy (BLE) quando la rete WoT principale non è disponibile.

---

## Build e CI/CD
(da testare)
Il progetto include un workflow **GitHub Actions** per la Continuous Integration.
Ad ogni push o pull request sul branch `main`, i runner effimeri di GitHub compilano automaticamente un `app-release-unsigned.apk` minificato da R8, caricandolo come artefatto pronto al download.

**Prerequisiti per la build locale:**
*   Android Studio Ladybug (o superiore)
*   JDK 17
*   Min SDK: 26 (Android 8.0)
*   Target SDK: 34 (Android 14)

```bash
# Per compilare la build di debug:
./gradlew assembleDebug

# Per compilare la build di rilascio (offuscata):
./gradlew assembleRelease
```
---

## Per la Demo

### Apparato-WoT
Avviare in **VS-Code** un clone del progetto ([Aquarium-WoT-BLE
](https://github.com/tiralongofabio/Aquarium-WoT-BLE))
e procedere all'avvio
```bash
# Installa dipendenze
npm install

# Compila TypeScript
npm run build

# Terminal 1: Avvia il mock server Modbus
npm run mock

# Terminal 2: Avvia il sistema
npm start

# Apri il browser per monitoraggio su interfaccia locale
# http://localhost:3000
```
### Applicazione Mobile

Acquisire in **Android Studio** un clone del progetto [Aquarium-WoT-App](https://github.com/tiralongofabio/Aquarium-WoT-App)

in caso di deploy su emulatore, importare l'immagine del QR-Code generata dall'interfaccia locale dell'Aplicazione WoT;
l'App si collegherà all'IP locale via HTTP, senza necesstià di connessione bluetooth

in caso di deploy su dispositivo android reale è necessario utilizzare anche un secondo device (mock dell'interfaccia BLE dell'apparato WoT), in questo caso simulato con un secondo smatphone su cui è già installata e configurata con i giusti end-point un'applicazione ad hoc (nRF Connect);
la connessione alle API avverrà quidni via http, su rete locale, ma solo a patto di aver prima stabilito la connessione bluetooth

### Abilitazioni di rete

Prima del deploy dell'applicazione, verificare che Device moble e Emulatore WoT siano nella stessa subnet e l'indirizzo IP (del PC) su cui sono esposte le API WoT

Nel PC che emula l'apparato WoT
 - WIN+R
 - wf.msc
 - Abilitazione delle regola firewall in entrata "TEST APP WoT" (disabilitare a fine demo)


NB: per questa demo, al fine di semplificare le regole e rotte di rete, gli IP target (http, locali) non sono acquisiti tramite QR-Code, ma hardcoded.  

Da Androind Studio, verificare/impstare l'IP target corretto in
 - data/di/NetworkModule.kt
 - app/src/main/res/xml/network_security_config.xml


