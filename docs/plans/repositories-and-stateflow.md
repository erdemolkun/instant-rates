# Plan: repositories as the source of truth, StateFlow in the ViewModels

Status: proposed · Written 2026-11-13

The screens are Compose with a ViewModel each, but the data they show still comes from global
singletons that the ViewModels poke directly. This plan moves rates, sources and alarms behind
repositories that expose `Flow`s, has the ViewModels turn those flows into one `StateFlow` of UI
state each, and removes the singletons it replaces. It's step 1 of the architecture follow-ups;
dependency injection with Hilt is step 2 and builds on it.

## Outcome

- **Data layer:** `RatesRepository`, `SourcesRepository` and `AlarmsRepository` are the only owners
  of their data. Each exposes it as a `StateFlow` or `Flow` and has suspend functions for changes.
- **Alarms:** they're read with a Room query that returns a `Flow`, so the UI and the background
  check always see the database as it is. The hand-written cache goes away.
- **UI layer:** each ViewModel exposes a single `StateFlow<…UiState>` built with `combine` +
  `stateIn`, and each screen reads it with `collectAsStateWithLifecycle()`.
- **Wiring:** a small `AppContainer` in `App` builds the repositories once, and ViewModels get them
  through their constructors. No ViewModel or repository calls `App.getInstance()`.
- **Removed:** `ProvidersManager`, `RatesHolder`, `AppScope`, and the state half of `SourcesManager`.
- **Testing:** the ViewModels and the alarm check get JVM unit tests with fake repositories.

## Scope

- **In scope:** rates, sources and alarms; the three ViewModels and their screens; the
  background/foreground switch in `AppVisibilityTracker` and `RatePollingService`.
- **Out of scope**, each its own later step:
  - **Step 2:** a DI framework (Hilt). This plan only adds constructor injection plus `AppContainer`,
    so step 2 is a mechanical swap.
  - **Step 3:** enums or sealed types instead of `Int` constants, and immutable rate models.
  - **Step 4:** DataStore instead of `SharedPreferences`, and WorkManager instead of the foreground
    service.
  - **Step 5:** a single activity with Navigation Compose.
  - **Polling interval:** `TimeIntervalManager` keeps its job and only gains a `StateFlow` for the
    selected interval (see step 2 of the approach). It moves to DataStore in step 4.

## Current state

| Data | Owner today | How the UI gets it | Problem |
|---|---|---|---|
| Latest rates | `RatesHolder` (global, last 10 per source) and `ProvidersManager.ratesEvents` | `LandingViewModel` copies them into its own map; `ChartViewModel` reads `RatesHolder` once, then collects events | Two copies of the same state, and the screen decides when polling starts (`LandingViewModel.init` calls `ProvidersManager.startSources()`) |
| Enabled sources | `SourcesManager` (global, mutable `CurrencySource.isEnabled`) | Read directly, plus a `sourceUpdates` signal | Mutable objects shared by everything; the source picker dialog lives in the same object |
| Alarms | `AlarmsRepository` with a hand-written cache over `LocalAlarmsDataSource` | `AlarmsViewModel` loads once and then edits its own list | The cache can be cold, which is why `LandingViewModel` preloads alarms so the polling service starts; the ViewModel mutates `Alarm.isEnabled` in place |
| Alarm check | `ProvidersManager.checkAlarms`, run on every fetch | n/a | Mixed into the rate publisher; depends on getting the previous rates out of `RatesHolder` before storing new ones |
| App-wide work | `AppScope` (global, never cancelled) | n/a | A hidden dependency of every class that uses it |

All three ViewModels hold Compose `mutableStateOf`. `AlarmsViewModel` is built through a factory;
the other two use `by viewModels()` and reach into singletons. No ViewModel has a JVM unit test.
The screens are covered only by the device tests (`LandingScreenTest`, `AlarmsScreenTest`,
`ScreensSmokeTest`, `AlarmsDeviceTest`).

## Target design

```
UI layer      LandingScreen     ChartScreen      AlarmsScreen
                  │ collectAsStateWithLifecycle()      │
              LandingViewModel  ChartViewModel   AlarmsViewModel
                  │ combine(...).stateIn(viewModelScope, WhileSubscribed(5_000), initial)
Data layer    RatesRepository ◄── SourcesRepository    AlarmsRepository ◄── AlarmDao (Flow)
                  │                                         ▲
                  └── providers (polling)   AlarmChecker ───┘ collects rate events
App           AppContainer: builds everything once, owns applicationScope
```

### `AlarmsRepository`

- Adds `@Query("SELECT * FROM tables") fun observeAll(): Flow<List<Alarm>>` to `AlarmDao`.
- Exposes `val alarms: StateFlow<List<Alarm>>`, built with
  `stateIn(applicationScope, SharingStarted.Eagerly, emptyList())`. It starts when the app starts,
  so the background check never sees a cold cache.
- Exposes `alarmsEnabled: StateFlow<Boolean>` (the master switch), backed by `Prefs`.
- Writes are suspend functions: `add`, `delete(id)`, `setEnabled(id, enabled)` and
  `setAlarmsEnabled(enabled)`. `setEnabled` uses an `UPDATE … WHERE _id = :id` query, so nothing
  mutates an `Alarm` in place.
- `hasAnyActive()` reads `alarms.value`, `alarmsEnabled.value` and the sources.
- Removed: the cache, `refreshAlarms()`, `AlarmsDataSource`, `LocalAlarmsDataSource` (the DAO is
  used directly), the singleton `getInstance()`, and the alarm preloading in `LandingViewModel`.
- The Room schema doesn't change: one new query, no new columns.

### `SourcesRepository`

- Holds the sources as `StateFlow<List<CurrencySource>>`, where `CurrencySource` becomes an
  immutable `data class` (`isEnabled` is a `val`).
- `setEnabled(types: Set<Int>)` saves to `Prefs` and emits a new list. The `sourceUpdates` signal
  goes away; collecting `sources` replaces it.
- Keeps the pure helpers (`isAvgType`, `chartIndex`, names) as functions on the source or in the
  repository.
- The source picker dialog moves to the UI layer: each screen shows `MultiChoiceDialog` from its
  own state and calls `viewModel.setEnabledSources(...)`.

### `RatesRepository`

- Owns the providers (moved from `ProvidersManager.init`).
- Exposes `val latestRates: StateFlow<Map<Int, RatesEvent<*>>>` (latest result per source), which
  replaces `RatesHolder` and `LandingViewModel.latestRates`.
- Exposes `val rateEvents: SharedFlow<RatesEvent<*>>` (every fetch, no replay), which feeds the chart
  and the alarm check.
- `refresh()` replaces `triggerUpdate()`.
- **Polling is driven by app state, not by a screen.** The repository collects `sources` and the
  app's foreground/background mode (a `StateFlow` set by `AppVisibilityTracker`). It starts or
  stops the providers itself, including "only sources with alarms" in background mode, which
  replaces `stopNonAlarmSources()`.

### `AlarmChecker`

- Collects `rateEvents` in `applicationScope`, and keeps the previous event per source itself, so
  it no longer depends on reading `RatesHolder` before the new rates are stored.
- For each crossing, it deletes the alarm through `AlarmsRepository` and calls `NotificationHelper`.
- It's a plain class, so the crossing logic gets a JVM unit test.

### ViewModels and screens

- `LandingViewModel(rates, sources, intervals)` exposes
  `uiState = combine(sources.sources, rates.latestRates, intervals.selection) { … }`, with
  `stateIn(viewModelScope, WhileSubscribed(5_000), initial)`. `buildRows` becomes a pure function
  of its inputs.
- `AlarmsViewModel(alarms, sources)` exposes
  `uiState = combine(alarms.alarms, alarms.alarmsEnabled, sources.sources)`. Its actions just call
  the repository; `onAlarmAdded` goes away, because a saved alarm arrives through the flow.
- `ChartViewModel(rates, sources, intervals, rateType)` keeps MPAndroidChart's mutable `LineData`,
  since the chart library needs it, and exposes `StateFlow<ChartUiState(version, latestX)>` for the
  redraw signal.
- The screens use `collectAsStateWithLifecycle()`. This needs
  `androidx.lifecycle:lifecycle-runtime-compose`, which isn't a dependency yet.
- ViewModels are created with `viewModelFactory { initializer { … } }` from `AppContainer`.

## Approach

Each step is its own commit, built and tested in CI, including the emulator job, before the next
starts. The app keeps working after every step.

1. **`AppContainer` and the alarms flow**
   - Add `AppContainer` with `applicationScope` (replacing `AppScope`), the database and
     `AlarmsRepository`.
   - Add the DAO `Flow` query and rewrite `AlarmsRepository` as above.
   - Move `AlarmsViewModel` and `AlarmsScreen` to `StateFlow`.
   - Remove the alarm preloading from `LandingViewModel`.
   - Tests: `AlarmsViewModel` unit test with a fake repository; DAO `Flow` test on the device.
2. **Sources**
   - Add `SourcesRepository` with an immutable `CurrencySource`.
   - Move the source picker dialog into the screens.
   - Give `TimeIntervalManager` a `selection: StateFlow<String>`.
   - Update every reader of `SourcesManager`.
3. **Rates**
   - Add `RatesRepository` and `AlarmChecker`.
   - Let `AppVisibilityTracker` set the app mode instead of calling providers.
   - Move `LandingViewModel` and `ChartViewModel` to `StateFlow`.
   - Delete `ProvidersManager` and `RatesHolder`.
   - Tests: `LandingViewModel` and `AlarmChecker` unit tests with fakes.
4. **Cleanup**
   - Delete `AppScope` and what's left of `SourcesManager`.
   - Remove the remaining `App.getInstance()` uses outside `AppContainer`.
   - Update the device tests that reached into singletons (`AlarmsDeviceTest`, `SourcesProbeTest`).

### Test setup

- Add `kotlinx-coroutines-test` for `runTest` and `Dispatchers.setMain`.
- Fakes are small in-memory implementations of each repository. To keep them simple, the
  repositories get interfaces (`RatesRepository`, `AlarmsRepository`, `SourcesRepository`) with one
  real implementation each.
- Flow assertions use `first { … }` and `backgroundScope`, so no extra library like Turbine is
  needed.

## Decisions to confirm

1. **Manual `AppContainer` now, Hilt in step 2** (recommended). The alternative is adding Hilt now,
   which makes this change bigger and mixes two refactors.
2. **`StateFlow` in the ViewModels** instead of Compose `mutableStateOf` (recommended). It can be
   tested without Compose, and it's what the architecture guide uses.
3. **Interfaces for the three repositories,** so tests can use fakes. The alternative is fakes
   built by subclassing, which needs `open` classes.
4. **Keep `Alarm` as it is** (a Room entity with `@JvmField` fields) and stop mutating it.
   Converting it to an immutable data class belongs to step 3, together with the rate models.

## Risks

- **The background service start depends on alarms being loaded.** Today this is papered over by
  preloading. With `SharingStarted.Eagerly` in `applicationScope`, Room emits within milliseconds of
  app start, but in theory `hasAnyActive()` could read an empty list. `AlarmsDeviceTest`'s
  background test covers this path and must keep passing. If it doesn't, `onAppBackground` can
  briefly await the first emission. It must still start the service inside the `onStop` window.
- **Alarm crossings depend on ordering.** `AlarmChecker` must compare each fetch with the previous
  one for the same source. Keeping the previous event per source inside the checker makes this
  explicit, and the unit test pins it down.
- **Polling ownership moves** from `LandingViewModel.init` to the repository. If the mode
  switching is wrong, polling could stop on the main screen or keep running in the background
  without alarms. The emulator job checks rates appear; `AlarmsDeviceTest` checks the background
  service.
- **MPAndroidChart's `LineData` stays mutable.** That's an accepted exception: the chart library
  owns it, and only `ChartViewModel` changes it, on the main thread.

## Done when

- No ViewModel or repository calls `App.getInstance()`, and `ProvidersManager`, `RatesHolder`,
  `AppScope` and the state in `SourcesManager` are gone.
- Each ViewModel exposes one `StateFlow` of UI state, and each screen reads it with
  `collectAsStateWithLifecycle()`.
- `AlarmsViewModel`, `LandingViewModel` and `AlarmChecker` have JVM unit tests.
- CI is green, including the emulator job: debug and minified builds, the instrumented tests, and
  rates on screen.
