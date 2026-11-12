# Plan: fix the Bigpara, Yapı Kredi and Bloomberg HT parsers

Status: Bigpara and Bloomberg HT fixed; Yapı Kredi pending · Researched 2026-11-12

Three of the app's four rate sources return nothing. Altın.in is the only one that works. This
document describes why each one fails today and plans a fix for each. It also covers changes shared
by all three, which should land first.

## Current state

Output of `SourcesProbeTest` on an emulator, against the live sites:

```
Altın.in       OK     USD=50.2549, EUR=55.7552, EUR_USD=1.1095, ONS=3946.6443
Bigpara        ERROR  IndexOutOfBoundsException: Index: 0, Size: 0
Yapı Kredi     EMPTY  no recognised rates
Bloomberg HT   EMPTY  no recognised rates
```

| Source | Fetched from today | Why it fails | Proposed source |
|---|---|---|---|
| Bigpara | `http://www.bigpara.com/doviz/dolar/` (HTML) | The site moved to `bigpara.hurriyet.com.tr` and was redesigned, so `.kurdetail .kurbox` matches nothing and `elements[0]` throws | JSON API `api-bigpara.hurriyet.com.tr/api/trade/current` |
| Yapı Kredi | `…/doviz-bilgileri.aspx/LoadInternetCurrencies` (HTML, jsoup) | The page moved. On the new page the gold row comes first, its value `6.287,45` fails to parse, and the exception discards every row | HTML at `/yatirimci-kosesi/doviz-kurlari/` |
| Bloomberg HT | `https://www.bloomberght.com/` (HTML) | The site was redesigned and the `.data-info` / `.value.LastPrice` markup is gone, so the parser returns an empty list | JSON at `/finance-slider-data` |

All endpoints below answered `200` to OkHttp's default user agent (`okhttp/4.12.0`), so no
browser user agent or cookies are needed today.

## Shared work (do first)

1. **Treat "no recognised rates" as an error.** `BasePoolingProvider` counts an empty list as a
   successful fetch. Broken sources therefore keep polling at full speed without backing off, and
   the UI shows empty cards with no sign of failure. Each converter should throw (for example
   `ParseException("no recognised rates")`) when it finds none, so that `errorRetryDelay` applies
   and the probe reports `ERROR` instead of `EMPTY`.
2. **Parse numbers in one place.** Every site uses Turkish formatting (`3.946,41`), and each model
   handles it differently. Bloomberg strips dots; `BuySellRate.parse` doesn't, which is part of the
   Yapı Kredi bug. Add `RateUtils.parseTurkish(text): Float?` (remove `.` grouping, `,` → `.`) and use
   it everywhere. JSON sources return real numbers and don't need it.
3. **Choose a JSON parser.** The project has none. Recommended: `org.json`, which ships with
   Android, so no new runtime dependency or R8 keep rules. Add `testImplementation("org.json:json")`
   so JVM unit tests get a real implementation. (`unitTests.isReturnDefaultValues = true` would
   otherwise turn every `org.json` call into a silent default.) Use kotlinx.serialization only if
   more JSON sources are planned.
4. **Fixture-based converter tests.** Save trimmed copies of today's responses under
   `app/src/test/resources/fixtures/` (`bigpara-current.json`, `bloomberg-finance-slider.json`,
   `yapikredi-doviz-kurlari.html`). Test each converter against its fixture, following
   `TextConvertersTest`. When a site changes again, the probe flags it and the fixture shows what
   the old format was.
5. **Agree on buy/sell.** "Alış" is the price the bank buys at (the lower one); "Satış" is the price
   it sells at. The current code is inconsistent: Bigpara mapped Alış → `valueBuy`, but Yapı Kredi
   mapped Alış → `valueSell`. **Decision needed:** map Alış → `valueBuy` for every source (my
   recommendation, matching how the sites label it), unless the Yapı Kredi swap was intentional.

## Bigpara — done

> Implemented: `BigparaConverter` reads the JSON API (fixture test in `BigparaConverterTest`), the
> cleartext exception is gone, and `org.json` is a test dependency (shared step 3). Shared step 1 is
> applied to Bigpara only: its converter throws when nothing is recognised. Step 5 (more rates) is
> still open; the provider requests `USDTRY` only. Probe: `Bigpara OK USD=50.2516/50.2639`.

**Findings**

- `http://www.bigpara.com/doviz/dolar/` now redirects to `https://bigpara.hurriyet.com.tr/doviz/dolar/`.
  The redesigned page has no `#content .kurdetail .kurbox`.
- The page reads live prices from a public JSON endpoint, which also works without a browser:

  ```
  GET https://api-bigpara.hurriyet.com.tr/api/trade/current?Symbols=USDTRY,EURTRY,EURUSD,XAUUSD,GLDGR
  {"data":[{"s":"USDTRY","b":50.2508,"a":50.2654,"c":50.258,"ts":1794478478000,"prec":4,…},
           {"s":"EURTRY",…},{"s":"EURUSD",…},{"s":"XAUUSD","b":3946.74,"a":3947.35,…},
           {"s":"GLDGR","b":6370.926,"a":6371.669,…}],
   "errors":[],"success":true}
  ```

  `b` = bid (Alış), `a` = ask (Satış). The response is 684 bytes gzipped, against about 190 KB for
  the HTML page.

**Plan**

1. Point `Api.bigparaService` at `https://api-bigpara.hurriyet.com.tr/` and change `BigparaService`
   to `@GET("api/trade/current") fun rates(@Query("Symbols") symbols: String)`, or hard-code the
   symbol list in the path.
2. Rewrite `BigparaConverter` to read `data[]`. Map `s` to a rate type (`USDTRY`→USD,
   `EURTRY`→EUR, `EURUSD`→EUR_USD, `XAUUSD`→ONS, `GLDGR`→ONS_TRY), with `valueBuyReal = b` and
   `valueSellReal = a`. Throw if `success` is false or nothing is recognised.
3. Change `BigparaRate.toRateType()` to match the symbol code instead of Turkish names.
4. Remove the `bigpara.com` cleartext exception from `network_security_config.xml`. After this, no
   source uses plain HTTP.
5. Optional: the API makes it cheap to support more than USD. Change Bigpara's `supportedRates`
   in `SourcesManager` from `onlyUsdTry` to USD, EUR, EUR_USD and ONS, the same set as Altın.in, so
   Bigpara shows on every row and chart.
6. Add a converter test with the JSON fixture, including a `success: false` case.

**Risks:** this is an undocumented internal API used by Bigpara's own site. It could change or
start requiring a key without notice. The probe in CI will catch that.

## Yapı Kredi

**Findings**

- The old URL redirects inconsistently: to `/yatirimci-kosesi/doviz-kurlari/` with a browser user
  agent, and to the home page otherwise (`301 → https://www.yapikredi.com.tr`).
- The new page still renders the table server-side, with the same `#currencyResultContent` id.
  Columns: code, description, **Alış (TL)**, **Satış (TL)**, change.

  ```
  XAU | Altın (gram)              | 6.287,45 | 6.486,66
  USD | Amerikan Doları           | 49,71    | 50,71
  EUR | Avrupa Para Birimi - Euro | 55,13    | 56,25
  XAG | Gümüş (gram)              | 80,79    | 84,11
  ```
- Why it returns nothing:
  1. The first row is gold, and `BuySellRate.parse("6.287,45")` → `"6.287.45".toFloat()` throws.
  2. The `try` wraps the whole loop, so that one exception discards every row, including USD and EUR.
  3. Gold's code is now `XAU`, but `toRateType()` looks for `"altın"`, so gold would be unrecognised
     even after a successful parse.

**Plan**

1. Fetch `https://www.yapikredi.com.tr/yatirimci-kosesi/doviz-kurlari/` directly instead of going
   through the redirect.
2. Move the fetch from jsoup's own HTTP client into a Retrofit `YapiKrediService` with a
   `YapiKrediConverter`, like the other sources. It then shares the OkHttp client, timeouts and
   minified-build coverage, and the converter can be tested against a fixture.
3. In the converter, select `tbody#currencyResultContent tr` and read `td[0]` (code), `td[2]`
   (Alış) and `td[3]` (Satış). Parse values with `RateUtils.parseTurkish`. **Skip bad rows instead of
   aborting the loop**, then throw only if no row is recognised.
4. In `YapıKrediRate.toRateType()`, match codes: `USD`→USD, `EUR`→EUR, `XAU`→ONS_TRY (TL per gram,
   which is what the old "altın" row was).
5. Apply the buy/sell decision from shared step 5.
6. Add a converter test with the HTML fixture, including a row whose value fails to parse.

**Risks:** the page is about 68 KB gzipped and the bank updates rates only a few times an hour.
Polling it every 3–10 s is wasteful and could get the app rate-limited. Consider a minimum interval
for this source (for example 60 s) in `BasePoolingProvider`. The bank's spread is about 1 TL, so
`valueBuy` and `valueSell` differ visibly from the market sources. That's expected.

## Bloomberg HT — done

> Implemented: `BloombergConverter` reads `/finance-slider-data` (fixture test in
> `BloombergConverterTest`) and throws when nothing is recognised. `RateUtils.parseTurkish` (shared
> step 2) now exists, but only Bloomberg uses it so far. Probe: `Bloomberg HT OK USD=50.2582,
> EUR=55.9087, ONS=3950.86, EUR_USD=1.1099`.
>
> No lighter endpoint exists: the site's scripts only call `/finance-slider-data`, and live updates
> go over a websocket on another host (`nc.domer.tr`). The data is also not real-time. Prices were
> about 4 minutes old, and the response is cached (`cache-control: max-age=30`). At a 10 s interval
> most fetches download the same ~100 KB again, which makes the slower minimum interval (risk
> option 2 below) more worthwhile.

**Findings**

- The home page has been rebuilt with Tailwind. `.data-info` and `.value.LastPrice` are gone, so
  the converter returns an empty list.
- USD, EUR and EUR/USD now sit in table rows like `tr.security-dolar span.lastPrice`. Gold doesn't:
  on the home page it appears only in a chart header styled with utility classes, and on `/altin`
  the row layout differs. An HTML parser would need a fragile special case for gold.
- The site's slider reads a JSON endpoint that has all four rates in one shape:

  ```
  GET https://www.bloomberght.com/finance-slider-data
  {"body":{"financeSlider":{"items":[
     {"slug":"dolar","lastPrice":"50,2582","lastUpdateTime":"12/11/2026 12:55:54","intraDay":"[[…]]",…},
     {"slug":"euro","lastPrice":"55,8755",…},
     {"slug":"eur-usd","lastPrice":"1,1093",…},
     {"slug":"altin-ons","lastPrice":"3.946,41",…}, …]}}}
  ```

  `lastPrice` is a Turkish-formatted string, so it still needs `parseTurkish`.

**Plan**

1. Change `BloombergService` to `@GET("finance-slider-data")`.
2. Rewrite `BloombergConverter` to read `body.financeSlider.items[]`. Map `slug` (`dolar`→USD,
   `euro`→EUR, `eur-usd`→EUR_USD, `altin-ons`→ONS), parse `lastPrice`, and ignore other slugs.
   Throw if none are recognised.
3. Update `BloombergRate.toRateType()` to the slug mapping, and simplify `setRealValues()` to use
   `parseTurkish`. The current dot-stripping logic already handles the format.
4. The `map { toRateType(); setRealValues() }` step in `BloombergRateProvider` can move into the
   converter, as the other sources do.
5. Add a converter test with a JSON fixture trimmed to a few items. Drop the `intraDay` arrays to
   keep it small.

**Risks:** the response is about 100 KB gzipped, mostly `intraDay` chart data the app doesn't need.
At a 10 s interval that's roughly 36 MB an hour. Options, in order of preference:

1. Check whether the slider's `financeSliderApiUrl` (`finance-slider`) or another lighter endpoint
   returns only prices. Not yet found.
2. Give Bloomberg a slower minimum interval, using the same mechanism as Yapı Kredi.
3. Fall back to `/doviz` HTML (about 18 KB) for USD, EUR and EUR/USD, and drop Bloomberg's ONS.

Like Bigpara's, this is an undocumented endpoint behind the site's own UI.

## Order of work and verification

1. Shared steps 1–4 in one change, with no behaviour change except broken sources now backing off.
2. One change per source, each with its fixture test: Bigpara first (smallest), then Bloomberg,
   then Yapı Kredi.
3. For each change:
   - `./gradlew testDebugUnitTest lintDebug assembleProduction`. The production build confirms R8
     doesn't break the new parsing.
   - Run `SourcesProbeTest` on an emulator; the source should report `OK` with plausible values.
   - Check the landing screen and a chart in the debug and minified builds. The CI emulator job
     does the minified check automatically.
4. Keep `SourcesProbeTest` in CI as the early warning. All three sources depend on unofficial
   endpoints or markup that can change at any time.

## Open questions

- Buy/sell orientation for Yapı Kredi (shared step 5): fix the swap, or was it deliberate?
- Should Bigpara support EUR, EUR/USD and ONS (Bigpara step 5), or stay USD-only?
- Is Bloomberg's bandwidth cost acceptable, or should it get a slower interval or lose ONS?
- Should Yapı Kredi and Bloomberg be disabled by default until fixed? Bloomberg is enabled by
  default today and currently shows empty cards on the main screen.
