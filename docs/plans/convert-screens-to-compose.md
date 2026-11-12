# Plan: convert all screens to Jetpack Compose

Status: done · Written and carried out 2026-11-12

The app is a hybrid. Every dialog and the navigation sheet are already Compose, but the three
screens are still XML layouts driven by activities. This plan converts those screens one at a time,
then removes the View-based code and libraries left behind.

## Outcome

All three screens are Compose, and the app has no XML layouts left. Decisions taken: Material 3
components (the bottom bar's FAB floats above the bar with no notch), a ViewModel per screen
reading the RxJava streams, one activity per screen as before, and the hidden clear-chart
action dropped. The chart went one step further than step 3: it uses MPAndroidChart's
`MPChartCompose` `LineChart` with a composable marker, and its data lives in `ChartViewModel`.
AppCompat, Material Components, CardView, RecyclerView and SwipeRefreshLayout are removed; the
window theme is the platform's Material theme.

## Scope

- **In scope:** the three screens (main, chart, alarms), their layouts, adapters and View helpers,
  and the theme and dependency cleanup that follows.
- **Out of scope:** non-UI code. Providers, `ProvidersManager`, `RatesHolder`, `SourcesManager`,
  `TimeIntervalManager`, `AlarmsRepository`, Room, the polling service and notifications stay as
  they are. The Compose screens read from the same RxJava streams. Nothing outside the UI changes
  unless a screen strictly needs it.

## Current state

### Already Compose

| Piece | Where |
|---|---|
| Theme (Material 3, app colours, neutral surfaces) | `ui/compose/RatesTheme.kt` |
| Hosting Compose dialogs from activities | `ui/compose/ComposeDialogHost.kt` |
| Source, interval and confirm dialogs | `ui/compose/Dialogs.kt` |
| Add-alarm dialog | `alarm/AddAlarmDialog.kt` |
| Navigation bottom sheet | `NavigationBottomSheet.kt` |

The Compose compiler, BOM, Material 3, Activity Compose and tooling are already set up, so the
skill's setup and theming steps (5 and 6) only need small additions.

### Screens still on Views

| Screen | Layouts | View code | Complexity (1–5) | View dependencies |
|---|---|---|---|---|
| Alarms (`AlarmsActivity`) | `activity_alarms`, `item_alarm`, `toolbar_actionbar_with_switch` | `AlarmsActivity`, `AlarmsAdapter`, `SlideInItemAnimator` | 3 | RecyclerView, CardView, SwitchCompat, Toolbar, FAB |
| Main (`LandingActivity`) | `activity_landing`, `layout_simple_rate_card`, `layout_no_provider` | `LandingActivity` (317 lines) | 4 | CoordinatorLayout, BottomAppBar, FAB, CardView, HorizontalScrollView, spans |
| Chart (`ChartActivity`) | `activity_chart`, `view_marker`, `toolbar_actionbar` | `ChartActivity` (292 lines) | 4 | MPAndroidChart `LineChart`, `MarkerView`, SwipeRefreshLayout, Toolbar |
| (none) | `activity_splash` | none | — | Unused, as is its only image `ic_orco_512` |

Shared base classes:
- `BaseActivity`: `setContentView`, toolbar setup, edge-to-edge padding.
- `BaseServiceActivity`: switches to the faster UI polling interval on resume, and disposes subscriptions.

**Architecture:** there are no ViewModels and no DI. Activities subscribe to RxJava subjects
(`ProvidersManager.ratesEventPublishSubject`, `SourcesManager.sourceUpdates`,
`TimeIntervalManager.intervalUpdates`) and update views directly.

**Rotation:** every activity declares `configChanges="orientation|screenSize|locale"`, so rotation
doesn't recreate them. Compose state survives rotation without extra work, and the plan keeps that.

## Approach

Following the migration skill's order (leaf-first, lowest risk first): first the shared foundations,
then Alarms, Main and Chart, then cleanup. Each screen is its own change, built, tested and
compared visually before the next starts. Activities stay as entry points throughout. Notifications
and the polling service open `LandingActivity` through `PendingIntent`s, and keeping three
activities means no navigation rework is needed.

For each screen:
1. **Baseline:** run `ScreensSmokeTest`, which already saves `landing`, `chart_usd` and `alarms`
   screenshots, and keep the screenshots as the visual reference (skill step 4).
2. **Composables:** write stateless composables that take state and callbacks, with `@Preview`s
   for each UI state (empty, loading, populated, disabled) in light and dark.
3. **State holder:** add a screen `ViewModel` that subscribes to the existing Rx streams in `init`,
   disposes them in `onCleared`, and exposes Compose `State` (see decision 2).
4. **Wire up:** the activity calls `setContent { RatesTheme { Screen(viewModel) } }`.
5. **Compare:** check the Compose preview and the emulator against the baseline, and iterate until
   layout and styling match. Differences that come from Material 3 components are listed below.
6. **Test:** add Compose UI tests for the screen's interactions, and run the device tests and the
   minified-build check.
7. **Remove:** delete the screen's XML, adapters and helpers once nothing references them.

## Step 0: Foundations

- **Dependencies:**
  - `androidx.lifecycle:lifecycle-viewmodel-compose` and `lifecycle-runtime-compose`.
  - For tests, `androidx.compose.ui:ui-test-junit4` (androidTest) and `ui-test-manifest`
    (debugImplementation).
  - All versions come from the catalog and the Compose BOM.
- **Screen base:** add a `ComposeScreenActivity` (or update `BaseActivity`) that calls
  `enableEdgeToEdge()` and `setContent`. It drops the content-view padding from
  `applySystemBarInsets()`; screens handle insets through `Scaffold` instead. `BaseServiceActivity`'s
  `onResume` behaviour (`TimeIntervalManager.setAlarmMode(false)`) stays.
- **Theme additions to `RatesTheme`:**
  - the top bar colour: `colorPrimary`, `#607D8B` in light and `#222222` in dark;
  - the rate card background: `#f3f3f3` and `#5f5f5f`, now the `cardBackgroundColor` theme attribute;
  - the icon tint: the `iconTintColor` attribute.

  Keep the XML themes until the cleanup step, since the remaining View screens still use them.
- **Shared composables:** `RatesTopAppBar` (title, back button, actions), and a `RateIcon` for the
  rate-type drawables, reused by the main screen rows, alarm items and charts.
- **Housekeeping:** delete the unused `activity_splash.xml` and `ic_orco_512`.

## Step 1: Alarms screen

Lowest risk: one list, a simple state, and no live rates.

**Composables**
- `AlarmItem(alarm, sourceName, sourceColor, onEnabledChange, onDelete)`. It replaces
  `item_alarm.xml`: a card with the direction arrow (rotated, green for above and red for below),
  the "if above/below" hint, the value, the rate icon, the source name in its colour, a switch and
  a close button.
- `AlarmsScreen(state, onAlarmsEnabledChange, onAdd, onToggle, onDelete, onBack)`. A `Scaffold`
  with a top bar that has a back button, the title and the master switch (replacing
  `toolbar_actionbar_with_switch`). The body is a `LazyColumn` of items, or the "no alarms" text
  when empty, with a FAB that opens the existing add-alarm dialog. When alarms are turned off, the
  list is drawn at 40% opacity as today.
- `Modifier.animateItem()` on list items replaces `SlideInItemAnimator`.

**State:** an `AlarmsViewModel` around `AlarmsRepository`. It keeps the sorted alarms (with
`Alarm.COMPARATOR`), the master switch (`isEnabled` / `updateEnabled`), and actions that call
`updateAlarm` and `deleteAlarm` on the IO scheduler, as the adapter does now. After the add-alarm
dialog saves, the callback inserts the new alarm.

**Tests:** Compose UI tests for the empty state, a toggle updating the alarm, delete removing the
item, and the master switch dimming the list. `AlarmsDeviceTest` covers the database and service
and doesn't change.

**Remove:** `AlarmsAdapter`, `SlideInItemAnimator`, `item_alarm.xml`, `activity_alarms.xml` and
`toolbar_actionbar_with_switch.xml`.

## Step 2: Main screen

The core screen: live data and the most interactions.

**Composables**
- `RateCard(title, value, onClick, onLongClick)`. It replaces `layout_simple_rate_card.xml`. The
  fade when a value changes (`playFadeOutInAnim`) becomes an animated alpha keyed on the value.
- `NoProviderLabel(onSettingsClick)`. It replaces `layout_no_provider.xml` and the span code. The
  gear goes in with `InlineTextContent`, and a click handler opens the source dialog. It keeps the
  "Settings" text for screen readers. The text wraps naturally, so the width-limiting code goes away.
- `RateRow(rateType, icon, cards, onCardClick, onCardLongClick, onSettingsClick)`. A row with the
  icon and either a horizontally scrolling row of cards or the label. It replaces the five
  `HorizontalScrollView` blocks.
- `LandingScreen(state, …)`. A `Scaffold` with the interval hint, the five rows in a scrollable
  column, and a bottom app bar. The bar has the menu button (navigation sheet), the interval and
  source actions (replacing `menu_landing.xml`), and the centred add-alarm FAB.

**State:** a `LandingViewModel` that builds, for each rate type, the cards of enabled sources that
support it (today's `refreshCardItemViews` logic). It fills values from `RatesHolder` on start and
from `ratesEventPublishSubject` after that, and rebuilds when `sourceUpdates` or `intervalUpdates`
fire. Starting the providers, `registerIntervalUpdates` and `startOrStopSources` move from
`LandingActivity` into the ViewModel unchanged. The notification permission request stays in the
activity, since it needs an activity result launcher.

**Tests:** a Compose UI test with fake state: rows, the no-provider label, tapping a card, and
tapping the gear. Keep `ScreensSmokeTest`.

**CI script:** `.github/scripts/emulator-smoke.sh` counts rates by finding `text="… TL"` in a
`uiautomator` dump. Compose `Text` appears in that dump too, but check this on the first CI run. If
it fails, add `testTag`s and match on resource ids instead (`Modifier.semantics { testTagsAsResourceId = true }`).

**Remove:** `activity_landing.xml`, `layout_simple_rate_card.xml`, `layout_no_provider.xml` and
`menu_landing.xml`.

## Step 3: Chart screen

**Composables**
- `ChartScreen(state, …)`. A `Scaffold` with the back button and the interval and source actions
  (replacing `menu_chart.xml`), the chart title, and `PullToRefreshBox` in place of
  `SwipeRefreshLayout`, triggering `ProvidersManager.triggerUpdate()`.
- **The chart:** see decision 3. Recommended first step: keep the `LineChart` View inside
  `AndroidView`, and move today's setup and `addEntry` logic into a `RateChartController` held with
  `remember`. The chart is updated imperatively (adding entries, trimming old ones,
  `moveViewToX`), which maps directly onto `AndroidView`'s `update` and a `DisposableEffect`.
  `view_marker.xml` stays with the View marker.

**State:** a `ChartViewModel` that replays cached events from `RatesHolder` and forwards new ones
from `ratesEventPublishSubject`. The controller adds them to the chart.

**Remove:** `activity_chart.xml`, `toolbar_actionbar.xml`, `menu_chart.xml`, the SwipeRefreshLayout
dependency, the `swipe_refresh_progress_bar_*` dimens and `ViewUtils.calculateActionBarSize`.

## Step 4: Cleanup

Once no screen uses Views:
- **Activities:** `BaseActivity` loses the toolbar and `setContentView` code. If nothing needs
  AppCompat any more, activities extend `ComponentActivity`.
- **Theme:** the window theme becomes a minimal day/night theme without an action bar (no View
  widgets use it any more). Delete the now-unused styles and attributes: `HeaderBar`,
  `ActionBarThemeOverlay`, `ActionBar.TitleText`, `toolbarTheme`, `actionbarHeaderStyle`,
  `actionBarIconColor`, `iconTintColor` and `cardBackgroundColor`.
- **Dependencies:** remove `appcompat`, `material`, `cardview`, `recyclerview` and
  `swiperefreshlayout` if nothing else needs them. The window theme currently comes from
  `material`, so replace it first.
- **Unused resources:** remove what's left, with lint's `UnusedResources` check as a guide.
- **Verification:** confirm the minified build with the emulator job and check the APK size change.

## Decisions needed

1. **How closely to match today's look.** Material 3 components differ in a few visible ways:
   - M3's `BottomAppBar` has no notch around the FAB (today's bar has one). Options: accept an
     M3-style FAB, or draw the notch with a custom shape. *Recommendation:* accept M3.
   - Cards, the top bar and switches use M3 shapes and sizes.

   The colours stay the same through `RatesTheme`.
2. **How the screens read RxJava.** *Recommendation:* a ViewModel per screen that subscribes to
   the Rx streams and exposes Compose state. It keeps logic out of composables, handles lifecycle
   in one place, and makes the screens easy to preview and test. The alternative,
   `androidx.compose.runtime:runtime-rxjava3`'s `subscribeAsState`, is smaller but puts
   subscriptions inside composables.
3. **Chart: `AndroidView` or MPAndroidChart's Compose module.** MPAndroidChart 4.0 ships an
   `MPChartCompose` module with a `LineChart` composable, `rememberChartState()` and composable
   markers. It needs compileSdk 37, which the app already uses. *Recommendation:* `AndroidView`
   first for an exact match with today's live-scrolling behaviour. Moving to `MPChartCompose` can
   follow as a separate change once its API for live, scrolling updates has been checked.
4. **The hidden clear-chart action.** Its menu item is invisible (`android:visible="false"`). Drop
   the action, or show it in the new top bar?
5. **One activity or three.** *Recommendation:* keep three for this migration. Moving to a single
   activity with Navigation can follow later. It would require updating the notification and
   service `PendingIntent`s.

## Risks

- **Visual changes** from Material 3 components (decision 1). The before and after screenshots of
  each step make them explicit.
- **Insets:** moving from padding the content view to `Scaffold` insets can shift content under
  the system bars. Check each screen in light and dark, and in landscape.
- **CI rate count:** the emulator script depends on finding rate text in a `uiautomator` dump (see
  step 2).
- **Live updates:** the main and chart screens update every few seconds. ViewModels must dispose
  their subscriptions, and composables must stay stable (for example, use immutable lists of card
  state) to avoid recomposing everything on every update.

## Rough size

| Step | Size |
|---|---|
| 0. Foundations | S |
| 1. Alarms | M |
| 2. Main | L |
| 3. Chart (AndroidView) | M |
| 4. Cleanup | S–M |
