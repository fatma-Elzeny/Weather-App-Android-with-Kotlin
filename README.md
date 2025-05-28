# 🌤️ AirCast - Weather Forecast App

**AirCast** is a powerful Android weather application that delivers 5-day forecasts, live hourly updates, alerts, and smart location-based tracking with full support for **offline mode**, **multi-language UI**, and **user-defined preferences**.

---

## Features

-  **5-Day Forecast** (Daily & Hourly)
-  **Location Modes**: GPS-based or Map Picker
- **Custom Alerts** (Notifications or Alarms)
-  **Multi-language Support** (English & Arabic)
-  **Offline Weather Caching**
- **Dynamic UI** with hourly chart & daily icons
-  **Settings Panel**:
  - Temperature Unit (°C / °F)
  - Wind Speed Unit (m/s, km/h, mph)
  - Language Toggle
  - Enable/Disable Notifications
- **Favorites**: Save multiple locations
-  Sunrise & Sunset time display
-  Feels Like temperature & humidity
-  Modern & accessible design

---

##  Architecture

AirCast follows the **MVVM (Model-View-ViewModel)** design pattern and clean architecture principles:

```
📦 data
   ┣ 📂 local         # Room DB & DAO
   ┣ 📂 remote        # Retrofit API services
   ┗ 📂 repository    # Unified interface for data access

📦 domain
   ┗ 📂 model         # Core models (WeatherResponse, SettingsData, etc.)

📦 ui
   ┣ 📂 main          # MainActivity (weather view)
   ┣ 📂 alerts        # Weather alerts screen
   ┣ 📂 favorites     # Saved locations
   ┣ 📂 settings      # App preferences
   ┗ 📂 splash        # Initial onboarding & permissions

📦 utils              # Language manager, icon mapper, etc.
```

---

##  Technologies Used

| Tech | Purpose |
|------|---------|
| **Kotlin** | Primary programming language |
| **MVVM** | App architecture |
| **Room** | Local database caching |
| **Retrofit** | API communication |
| **LiveData + ViewModel** | State management |
| **WorkManager** | (Optional) background tasks |
| **Lottie** | Animations for splash/setup |
| **OSMDroid** | Map picker integration |
| **Coroutines** | Async networking & database |
| **SharedPreferences** | Settings & language persistence |

---

## 🌍 Language Support

- ✅ English
- ✅ Arabic (RTL layout + localized strings)
- Auto-adapts via `SettingsActivity` or device locale.

---

##  Data Source

Uses **OpenWeatherMap 5-Day Forecast API**:

- Endpoint: `https://api.openweathermap.org/data/2.5/forecast`
- Supports:
  - Hourly temperature
  - Feels like
  - Weather icon codes
  - Wind/humidity/pressure
  - Sunrise/sunset
  - Description translation via `lang=ar`

---

##  How Offline Mode Works

1. Each successful API call saves:
   - Raw JSON response
   - Timestamp
   - Location name
2. If user is offline:
   - Loads the most recent cached forecast
   - Shows “Last Updated: [time]”
   - Disables data refresh

---


