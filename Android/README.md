
# Fitrus Device SDK Android Development Guide

## 1. Overview
The Fitrus Device SDK allows Android apps to connect with Fitrus series devices via BLE (Bluetooth Low Energy) to provide body composition analysis, heart rate measurement, stress measurement, and temperature measurement functionalities.

This document provides the necessary environment setup, initialization, main functionalities, and sample code for developing Android apps using the Fitrus Device SDK.

## 2. Development Environment

| Item | Recommended Environment |
|---|---|
| Language | Kotlin, Java |
| Android Version | Android 6.0 (API 23) or higher |
| Gradle Version | 7.0 or higher |
| Android Gradle Plugin | 7.0 or higher |

## 3. Gradle Configuration

### 1) Add Repository
Add the following repository settings to `settings.gradle.kts` or your project-level `build.gradle.kts`.

```kotlin
pluginManagement {
    repositories {
        maven { url = uri("https://gitlab.com/api/v4/projects/102/packages/maven") }
        google()
        mavenCentral()
    }
}
```

### 2) Add Dependency in app/build.gradle.kts

```kotlin
dependencies {
    implementation("com.onesoftdigm.fitrus:fitrus-device-sdk:0.2")
```

## 4. Initialization & FitrusBleDelegate Interface

### 1) SDK Initialization

```kotlin
val fitrusDevice = FitrusDevice(context, delegate, "your_api_key")
```
- `context`: Application or Activity Context
- `delegate`: Callback object to receive measurement results and status events
- `your_api_key`: API key provided by Fitrus


### 2) FitrusBleDelegate Interface

The FitrusBleDelegate interface handles device connection state, measurement results, and error notifications.
The following callbacks must be implemented in your application.

#### 📌 Required Methods

| Method | Description |
|--------|-------------|
| `handleFitrusConnected()` | Called when the device is successfully connected |
| `handleFitrusDisconnected()` | Called when the device is disconnected |
| `fitrusDispatchError(error: String)` | Called when an error occurs during communication or measurement |

#### 📊 Measurement Callback Methods

| Method | Description |
|--------|-------------|
| `handleFitrusCompMeasured(result: Map<String, String>)` | Called when body composition measurement is completed |
| `handleFitrusPpgMeasured(result: Map<String, Any>)` | Called when PPG-related measurements (heart rate, stress, blood pressure) are completed |
| `handleFitrusTempMeasured(result: Map<String, String>)` | Called when body or object temperature is measured |
| `handleFitrusBatteryInfo(result: Map<String, Any>)` | Called when battery status is received |
| `handleFitrusDeviceInfo(result: Map<String, String>)` | Called when device information is received |

#### ✅ New Features (as of March 2025)

The following measurement types have been added to the SDK and are supported via the FitrusBleDelegate interface:

- **Heart Rate Measurement**  
  - Start: `manager.startFitrusHeartRateMeasure()`  
  - Callback: `handleFitrusPpgMeasured(result)`

- **Stress Measurement**  
  - Start: `manager.startFitrusStressMeasure(birth: String)`  
    - `birth`: String representing the user's birth date in `yyyy/MM/dd` format (e.g., `"1990/03/05"`)  
  - Callback: `handleFitrusPpgMeasured(result)`

- **Body Temperature & Object Temperature Measurement**  
  - Start: `manager.startFitrusTempBodyMeasure()` or `manager.startFitrusTempObjectMeasure()`  
  - Callback: `handleFitrusTempMeasured(result)`

- **Blood Pressure Measurement**  
  - Start: `manager.StartFitrusBloodPressure(systolic: Float, diastolic: Float)`  
    - `systolic`: Base systolic pressure (in mmHg)  
    - `diastolic`: Base diastolic pressure (in mmHg)  
  - Callback: `handleFitrusPpgMeasured(result)`

> 💡 All callbacks are invoked on the main thread, so it's safe to update UI components directly.

### Detailed Explanation

#### handleFitrusConnected()
- Called when the device is successfully connected.

#### handleFitrusDisconnected()
- Called when the device is disconnected.

#### handleFitrusDeviceInfo(result)
- Called with device information such as firmware version, battery level, brightness, and other parameters.

#### handleFitrusBatteryInfo(result)
- Called with battery level information.
```kotlin
{
    "Battery" to 95  // Battery level in percentage
}
```

#### handleFitrusCompMeasured(result)
- Called with body composition measurement results.
```kotlin
Comp(
    protein = 7.5,
    bmr = 1500.0,
    mineral = 3.2,
    bodyAge = 30,
    smm = 30.5,
    icw = 25.0,
    ecw = 15.0,
    bfp = 25.0,
    bfm = 18.0
)
```

#### fitrusDispatchError(error)
- Called when an error occurs during communication or measurement.
```kotlin
fitrusDispatchError("[227] Low Battery")
```

## 5. Device Scanning & Connection

### Start Scanning
```kotlin
fitrusDevice.startFitrusScan()
```

### Stop Scanning
```kotlin
fitrusDevice.stopFitrusScan()
```

### Check Connection Status
```kotlin
if (fitrusDevice.fitrusConnectionState) {
    // Device is connected
}
```

### Disconnect Device
```kotlin
fitrusDevice.disconnectFitrus()
```

## 6. Measurement

### 6.1 Body Composition Measurement

### Start Measurement
```kotlin
fitrusDevice.startFitrusCompMeasure(
    Gender.MALE,
    175.3f,  // Height (cm)
    72.5f,   // Weight (kg)
    "1990/01/01",  // Birth date
    0.0f     // Correction value (0.0 = no correction)
)
```

### Correction Value Explanation
The correction value adjusts measurement results (like body fat percentage) when there is a known discrepancy between the actual value and the device result.

| Item | Description |
|---|---|
| Default | 0.0 (No correction) |
| Range | -30.0 ~ +30.0 |
| Positive | Increases measured value |
| Negative | Decreases measured value |

#### Example Correction Values
| Value | Meaning |
|---|---|
| 0.0 | No correction |
| -2.0 | Measurement is higher than actual, so decrease result |
| +2.0 | Measurement is lower than actual, so increase result |

### 6.2 Heart Rate Measurement

This function measures the user’s heart rate (BPM) and oxygen saturation (SpO2) using photoplethysmography (PPG) signals.

#### ✔️ Supported Devices
- Fitrus Plus
- Fitrus Neo
- Fitrus Light (firmware ≥ 3.0)

#### ⏱️ Measurement Duration
- 30 seconds

#### 🚫 Not Supported On
- Fitrus A

An error is returned via `fitrusDispatchError("Not Support Device!!")` if the device is unsupported.

#### ▶️ How to Start
```kotlin
manager.startFitrusHeartRateMeasure()
```

#### 📥 Callback
```kotlin
override fun handleFitrusPpgMeasured(result: Map<String, Any>)
```

#### 📊 Result Format
| Key      | Description                     |
|----------|---------------------------------|
| `bpm`    | Heart rate (beats per minute)   |
| `oxygen` | Blood oxygen saturation (%)     |
| `Date`   | Measurement timestamp           |

---

### 6.3 Stress Measurement

This function measures the user’s stress level using PPG signals. Requires user birth date input.

#### ✔️ Supported Devices
- Fitrus Plus
- Fitrus Neo
- Fitrus Light (firmware ≥ 3.0)

#### 🚫 Not Supported On
- Fitrus A

#### ▶️ How to Start
```kotlin
manager.startFitrusStressMeasure(birth: String)
```
- `birth`: User’s birth date in `"yyyy/MM/dd"` format (e.g., `"1991/03/01"`)

#### 📥 Callback
```kotlin
override fun handleFitrusPpgMeasured(result: Map<String, Any>)
```

#### 📊 Result Format
| Key           | Description                  |
|----------------|------------------------------|
| `stressValue`  | Numerical stress index        |
| `stressLevel`  | Stress level (LOW/MID/HIGH)   |
| `bpm`          | Heart rate (bpm)              |
| `oxygen`       | Oxygen saturation (%)         |
| `device`       | Device name                   |
| `Date`         | Measurement timestamp         |

---

### 6.4 Blood Pressure Measurement

Estimates systolic and diastolic blood pressure using PPG signals and baseline values.

#### ✔️ Supported Devices
- Fitrus Plus (firmware ≥ 1.2)

#### 🚫 Not Supported On
- Fitrus A
- Fitrus Neo
- Fitrus Light

#### ▶️ How to Start
```kotlin
manager.StartFitrusBloodPressure(baseSystolic: Float, baseDiastolic: Float)
```
- `baseSystolic`: Baseline systolic pressure in mmHg
- `baseDiastolic`: Baseline diastolic pressure in mmHg

#### 📥 Callback
```kotlin
override fun handleFitrusPpgMeasured(result: Map<String, Any>)
```

#### 📊 Result Format
| Key        | Description                          |
|------------|--------------------------------------|
| `SBP`      | Measured systolic blood pressure     |
| `DBP`      | Measured diastolic blood pressure    |
| `BaseSBP`  | Input systolic baseline              |
| `BaseDBP`  | Input diastolic baseline             |

---

### 6.5 Temperature Measurement (Body/Object)

Measures body or object surface temperature depending on the function used.

#### ✔️ Supported Devices
- Fitrus Plus
- Fitrus Neo

#### 🚫 Not Supported On
- Fitrus A
- Fitrus Light

#### ▶️ How to Start
```kotlin
manager.startFitrusTempBodyMeasure()      // For body temperature
manager.startFitrusTempObjectMeasure()    // For object temperature
```

#### 📥 Callback
```kotlin
override fun handleFitrusTempMeasured(result: Map<String, String>)
```

#### 📊 Result Format
| Key    | Description                       |
|--------|-----------------------------------|
| `temp` | Measured temperature in Celsius   |


## 7. Battery Information Retrieval

### Request Battery Info
```kotlin
fitrusDevice.getBatteryInfo()
```

### Battery Info Callback
```kotlin
override fun handleFitrusBatteryInfo(result: Map<String, Any>) {
    val battery = result["Battery"]
    Log.d("BatteryInfo", "Battery Level: $battery%")
}
```

## 8. Device Information Retrieval

### Request Device Info
```kotlin
fitrusDevice.getDeviceInfoAll()
```

### Device Info Callback
```kotlin
override fun handleFitrusDeviceInfo(result: Map<String, String>) {
    Log.d("DeviceInfo", result.toString())
}
```


## 9. Stop Measurement
```kotlin
fitrusDevice.stopFitrusPpgMeasure()
```

## 10. Error Handling

```kotlin
override fun fitrusDispatchError(error: String) {
    Log.e("FitrusError", error)
    Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
}
```
# 11. Contact Information

- Technical Support: jybaik@onesoftdigm.com

## 12. Version History

### 0.2 (2025-03-27)
- Added detailed documentation for new measurements via `FitrusBleDelegate`:
  - Heart rate
  - Stress (with birthdate parameter)
  - Blood pressure (with systolic and diastolic parameters)
  - Body and object temperature
- Updated section 2) FitrusBleDelegate Interface to reflect the above changes.

### 0.1 (2025-02-27)
- Initial Release