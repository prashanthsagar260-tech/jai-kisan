# jai kisan (జై కిసాన్) - Farmer Agri-Tech Platform

A farmer-first mobile agri-tech application in Telugu and English designed with high-contrast earthy aesthetics, large iconography, offline caching, and voice assistance for farmers with varying literacy levels.

## User Review & Critical Decisions

> [!IMPORTANT]
> The clarifying questions were completed with recommended defaults to deliver a production-ready MVP:
> - **Language & Accessibility**: Full bilingual support with Telugu (`తెలుగు`) as the default language and an instant one-tap English toggle, accompanied by audio readouts (TTS) and voice commands.
> - **Regional Focus**: Pre-configured with real major agricultural mandis across Telangana and Andhra Pradesh (Warangal, Nizamabad, Khammam, Guntur, Kurnool, Hyderabad, Anantapur) with regional price tracking.
> - **AI Voice Assistant**: Dual-mode farmer support — a one-tap voice mic assistant plus a realistic full-screen simulated Kisan Call Center phone dialog (IVR + Gemini AI) speaking fluent Telugu.
> - **Offline & Data Resilience**: Room-backed local caching ensures prices, weather forecasts, and logistics reports are instantly accessible even with patchy 2G/3G connectivity.

---

## 1. Overview & Core Concept

**jai kisan** empowers farmers with actionable, timely market intelligence and agricultural decision support:
- **Daily Mandi Prices & Trend Visuals**: Track daily commodity arrivals, modal prices (Min, Max, Modal per Quintal), and interactive 7-day, 30-day, and seasonal graphs.
- **Transportation & Logistics Report**: Calculate distance, vehicle options (Local Truck, Cooperative Pickup, Shared Auto/Mini-truck), estimated fares, and travel duration from farm to market.
- **Nearest Market Locator & Navigation**: Find nearby agricultural markets with distance sorting, operating hours, direct phone dialing, and map route directions.
- **Live Crop Photo Upload**: Camera and gallery upload with on-device bitmap compression for low bandwidth, quality tagging, and buyer/community showcase.
- **AI Kisan Call Center & Voice Assistant**: Interactive Telugu voice assistant powered by Gemini for spoken price inquiries, pest diagnosis, and weather advice.
- **Crop Catalog & Database**: Comprehensive guide covering Kharif, Rabi, and Zaid crops (Paddy, Cotton, Chilli, Red Gram, Maize, Turmeric, Tomato, Groundnut) with soil needs, cost of cultivation, and common disease treatments.
- **Cold Storage Locator**: Perishable crop filter showing cold storage facilities, refrigeration temperatures, available quintal capacity, and rental rates.
- **Price Forecasting with Confidence Ratings**: Historical trend-driven short-term projections (weekly/monthly) with explicit confidence intervals to aid selling decisions.
- **Weather & Climate Advisory**: Localized IMD-style temperature, rainfall forecasts, humidity, wind speed, and actionable agricultural advisories (sowing, spraying, harvesting).

---

## 2. User Experience & Visual Design

### Visual Identity & Theme
- **Aesthetic Direction**: Warm organic, utilitarian, and high-contrast. Designed specifically for outdoor sunlight readability in farm fields.
- **Color Palette**:
  - Primary: Deep Agri Green (`#1B5E20`) — represents healthy crops and prosperity.
  - Secondary / Accent: Harvest Gold / Warm Amber (`#F57F17` / `#FFB300`) — stands out for prices, alerts, and call actions.
  - Soil Accent: Warm Earth Brown (`#5D4037`) — anchors cards and secondary details.
  - Background: Crisp Warm Cream (`#FBF9F1`) in Light mode; Deep Charcoal Foliage (`#121A12`) in Dark mode.
  - Surface Cards: Pure White (`#FFFFFF`) with bold borders (`1.5.dp`) and subtle tonal elevation for clear visual separation.
- **Typography & Ergonomics**:
  - Telugu script rendering with high legibility and increased line height (`sp` scaling).
  - Prominent touch targets (minimum `56.dp` for core action buttons).
  - Icon-first cards paired with large Telugu labels so farmers can navigate visually without reading dense paragraphs.

### Key User Flows
1. **Home / Price Hub**:
   - Header with Language Toggle (`తెలుగు / EN`), Data Saver badge, and Voice Call floating button.
   - Quick Weather Alert ticker (e.g., "వర్ష సూచన: రాబోయే 24 గంటల్లో తేలికపాటి వర్షం").
   - Crop price grid showing crop icon, modal price (₹/Quintal), change indicator (`▲ +₹120` / `▼ -₹50`), and quick audio readout button.
2. **Detailed Price & Trend Analytics**:
   - Tap any crop to view 7-Day, 30-Day, and Seasonal charts drawn with high-contrast Canvas graphs.
   - Price Prediction card displaying next week's estimated range with confidence rating (`మధ్యస్థ విశ్వసనీయత - 82%`).
3. **Transport & Logistics Planner**:
   - Select origin and destination mandi.
   - View transport vehicles (Shared Auto ₹400, Pickup Truck ₹1,200, 10-Tonne Lorry ₹3,800), load capacities, and direct transporter contact.
4. **AI Kisan Call Center**:
   - One tap on the green phone icon launches the "1800-Kisan" simulated call screen with live voice waveform, speakerphone toggle, and spoken Telugu queries answered by Gemini.

---

## 3. Key Product Decisions & Trade-Offs

| Decision | Chosen Approach | Rationale |
| :--- | :--- | :--- |
| **Bilingual Localization** | In-app dynamic Language switcher (`AppLanguage.TELUGU` / `AppLanguage.ENGLISH`) with centralized string maps and localized formats | Enables instant switching without restarting the app or requiring system language changes. |
| **Offline Data Persistence** | Room Database with pre-seeded realistic mandi data, cached weather, and photo records | Ensures zero latency on low-end devices and full functionality when in remote rural fields without cellular data. |
| **Audio & Voice Integration** | Android `TextToSpeech` (Telugu `te-IN` / English fallback) + Gemini AI Logic assistant | Empowers illiterate and semi-literate farmers to listen to prices, weather warnings, and crop advice. |
| **Image Compression** | On-device Canvas/Bitmap subsampling before storing in Room / local files | Prevents high memory consumption and supports low-bandwidth uploads. |
| **Modular Jetpack Compose** | Single Activity architecture with clean screen composables and centralized state | Maintains clean architecture under 500 lines per file for long-term maintainability. |

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                          MainActivity                                   │
│            (Edge-to-Edge, Language & TTS Controller)                   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
┌───────────────────────────────────▼────────────────────────────────────┐
│                         KisanMainScreen                                │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ TopAppBar: Logo, Language Switcher (తెలుగు/EN), Offline Status │   │
│   └────────────────────────────────────────────────────────────────┘   │
│   ┌────────────────────────────────────────────────────────────────┐   │
│   │ NavigationBar (M3):                                            │   │
│   │ [1. ధరలు (Prices)] [2. మార్కెట్ & రవాణా (Logistics)]           │   │
│   │ [3. పంటలు (Crops)] [4. శీతల గిడ్డంగి (Cold Storage)]          │   │
│   │ [5. కిసాన్ కాల్ (AI Call)]                                     │   │
│   └────────────────────────────────────────────────────────────────┘   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
          ┌─────────────────────────┼─────────────────────────┐
          ▼                         ▼                         ▼
┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
│ MarketViewModel  │      │ LogisticsViewModel│     │ AiCallViewModel  │
│ - Prices & Trends│      │ - Mandi Locator  │      │ - Gemini AI      │
│ - Predictions    │      │ - Fare Calculator│      │ - Voice Dialogue │
│ - Weather Cache  │      │ - Cold Storage   │      │ - Text-to-Speech │
└─────────┬────────┘      └─────────┬────────┘      └─────────┬────────┘
          │                         │                         │
          └─────────────────────────┼─────────────────────────┘
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        KisanRepository & Room DB                       │
│  - MandiPriceDao: Daily prices, historical trends, price predictions   │
│  - CropDao: Crop catalog, seasonal advisory, disease guide             │
│  - LogisticsDao: Mandi coordinates, distance matrix, transport rates   │
│  - ColdStorageDao: Cold storage facilities, capacities & tariffs       │
│  - CropPhotoDao: Saved quality photos, compression & tags              │
└────────────────────────────────────────────────────────────────────────┘
```

### Core Entities & Models
- `MandiPrice`: `cropId`, `cropNameTe`, `cropNameEn`, `marketName`, `minPrice`, `maxPrice`, `modalPrice`, `trendDelta`, `historyPoints`, `predictionRange`, `predictionConfidence`.
- `TransportOption`: `vehicleType`, `capacityQuintals`, `baseRate`, `perKmRate`, `contactPhone`.
- `ColdStorage`: `facilityName`, `mandiVicinity`, `totalCapacityMt`, `availableCapacityMt`, `monthlyRatePerQuintal`, `temperatureRange`.
- `CropAdvisory`: `cropName`, `season`, `sowingTime`, `harvestTime`, `fertilizerAdvice`, `diseaseAdvice`.
- `WeatherForecast`: `location`, `temperature`, `condition`, `rainfallProbability`, `farmingAdvisory`.

---

## 5. Verification Plan

### Automated & Unit Tests
- **Database & DAOs**: Validate Room entities, queries, and pre-seeded market price records via JVM unit tests.
- **Logistics Math**: Unit test transport fare calculation (base rate + distance * rate per km).
- **Compilation Check**: Run `compile_applet` to ensure error-free builds with Kotlin Compose and Room KSP.

### Manual Verification
- Test language switching between Telugu and English across all tabs.
- Verify price trend graph drawing and interactive tooltip inspection.
- Simulate kisan call center dialogue and verify audio text-to-speech readout.
- Test crop photo capture/picker with bitmap compression.
- Confirm edge-to-edge system bar insets and accessibility touch targets.
