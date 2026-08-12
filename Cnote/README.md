# Cnote — Step 4: testo formattabile + Web Clipper

## Novità di questo step

### Testo formattabile
- Seleziona una parte di titolo o testo, poi tocca **B** (grassetto), *i* (corsivo) o la goccia colorata (colore testo)
- Tasto "occhio" in alto: passa da modifica a **anteprima** (vedi il risultato formattato vero, senza i simboli)
- Il titolo è sempre più grande e in grassetto rispetto al testo della nota

⚠️ La formattazione è basata su piccoli simboli inseriti nel testo stesso (es. `**parola**` per il grassetto), quindi mentre scrivi vedrai questi simboli; passa in anteprima per vedere il risultato reale. Non è annidabile (non puoi avere grassetto+colore sulla stessa parola contemporaneamente).

### Web Clipper
- Da un browser qualsiasi (Chrome, ecc.), tocca "Condividi" su una pagina → scegli **Cnote**
- Ti verrà chiesto come salvarla:
  - **Solo link**: salva il link e basta
  - **Pagina completa (offline)**: scarica testo, immagini e link della pagina, così resta consultabile anche se la pagina originale viene rimossa in futuro
- Nella nota creata trovi un pulsante **"Apri come pagina web"** che la mostra come se fossi nel browser (usando la copia salvata sul telefono, non internet)

⚠️ **Nuovo aumento di versione del database** (3 → 4) per i campi del web clipper. Le note di prova salvate finora andranno perse al primo avvio di questa versione (come per gli step precedenti).

## Cosa manca ancora
Nessuno step pianificato rimanente — le 4 funzionalità richieste inizialmente sono complete. Possibili rifiniture future: migrazione dati non distruttiva, editor formattazione più visuale (senza simboli), gestione di condivisioni con testo HTML anziché solo plain text.


## 🌐 Compilare l'APK online, senza installare nulla (consigliato se non vuoi Android Studio)

Questo progetto include già un file `.github/workflows/build-apk.yml` che fa compilare l'APK gratuitamente dai server di GitHub. Basta caricare i file su GitHub, senza installare nulla sul PC. Vedi la guida passo passo che ti ho mandato in chat.

## Come aprire e compilare il progetto (in alternativa, con Android Studio)

1. Installa **Android Studio** (gratuito): https://developer.android.com/studio
2. Apri Android Studio → **Open** → seleziona la cartella `Cnote` (quella che contiene questo README)
3. Alla prima apertura, Android Studio scarica automaticamente Gradle e le dipendenze (serve connessione internet, ci vuole qualche minuto la prima volta)
4. Collega uno smartphone Android (con debug USB attivo) oppure usa un emulatore, poi premi il tasto ▶ **Run**

## Per ottenere il file .apk
- Menu **Build → Build Bundle(s) / APK(s) → Build APK(s)**
- L'APK generato si trova in: `app/build/outputs/apk/debug/app-debug.apk`
- Puoi installarlo sul telefono trasferendo quel file e aprendolo (assicurati che l'installazione da "origini sconosciute" sia permessa)

## Requisiti minimi
- Android 8.0 (API 26) o superiore

## Struttura del progetto
```
Cnote/
 ├─ app/src/main/java/com/cnote/app/
 │   ├─ data/          → Note.kt, NoteDao.kt, AppDatabase.kt, NoteRepository.kt (Room)
 │   ├─ ui/             → NoteViewModel.kt
 │   ├─ ui/screens/     → NoteListScreen.kt, NoteEditScreen.kt
 │   ├─ ui/components/  → NoteCard.kt, FabMenu.kt
 │   ├─ ui/theme/       → Color.kt, Theme.kt, Type.kt
 │   └─ MainActivity.kt
 └─ app/src/main/res/
```

Quando sei pronto, procediamo con lo **Step 2: Taccuini personalizzati**.
