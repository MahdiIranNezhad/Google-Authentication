# Native Google Auth (Unity Android)

A lightweight **Unity Package Manager (UPM)** library that provides **native Google Sign-In for Android** without Firebase or Google Play Games Services (GPGS).

This package is designed for **Unity Android projects** that need a simple, direct, and Iran-friendly Google authentication flow based on **ID Token (OAuth Client Type 3)**.

---

## Features

- Native Google Sign-In using `play-services-auth`
- **ID Token only** (OAuth 2.0 Client ID – type 3)
- No Firebase dependency
- No Google Play Games Services dependency
- Works with custom UnityPlayer activities
- Automatic AndroidManifest configuration at build time
- Clean UPM-based integration

---

## Requirements

### Unity
- Any modern Unity version with Android support
- No Unity-version-specific APIs are used

### Android
- **Minimum SDK**: API 23 (Android 6.0)
- **Target SDK**: API 34+

These values are enforced by the Android library Gradle configuration and are compatible with current Google Play requirements.

---


## Installation

### 1. External Dependency Manager for Unity (EDM4U)

This package **requires EDM4U** to resolve Google Play Services dependencies.

EDM4U is already declared as a UPM dependency in `package.json`:

```json
"com.google.external-dependency-manager": "1.2.185"
```

No manual installation is required.

---

### 2. Install Native Google Auth Package

Add via Git URL:

```text
https://github.com/MahdiIranNezhad/Google-Authentication.git
```

or local path in `manifest.json`:

```json
"dependencies": {
  "com.unity.extension.googleauth": "https://github.com/MahdiIranNezhad/Google-Authentication.git"
}
```

---

## Google Console Setup (Required)

This package **only requires a Web Client ID (OAuth client type 3)**.

You can obtain this ID from either **Google Cloud Console** or **Firebase**.

### Option A – Google Cloud Console

1. Open **Google Cloud Console**
2. Select or create a project
3. Configure **OAuth consent screen**
4. Create **OAuth Client ID**:
   - Application type: **Web application**
5. Copy the generated **Client ID**

### Option B – Firebase (google-services.json)

If your project already uses Firebase:

1. Open `google-services.json`
2. Locate a client entry with:
   ```json
   "client_type": 3
   ```
3. Copy the corresponding `client_id`

This value is the **Web Client ID** required by the package.

Reference:
- https://developers.google.com/identity/sign-in/android/start

---


## Unity Configuration

### Project Settings

After importing the package:

1. Open **Project Settings → Native Google Auth**
2. Paste your **Web Client ID**
3. Save

The value is stored in a ScriptableObject:

```
Assets/Resources/GoogleAuthSettings.asset
```

---

## AndroidManifest Handling (Important)

This package **automatically injects** the Web Client ID into your `AndroidManifest.xml` at build time.

### Injected Meta-Data

```xml
<meta-data
    android:name="com.unity.extension.GoogleAuth.WEB_CLIENT_ID"
    android:value="YOUR_WEB_CLIENT_ID" />
```

### Why This Is Required

- Native Google Sign-In reads the Web Client ID **only from Android manifest meta-data**
- Hardcoding values in Java is discouraged
- This approach keeps credentials managed from Unity Editor

### How It Works

- During Android builds:
  - The settings asset is loaded
  - The Web Client ID is validated
  - `Assets/Plugins/Android/AndroidManifest.xml` is created or updated
- At runtime, `SignInHelperActivity` reads the value directly from manifest meta-data

You can also force an update manually:

```
Tools → Google Auth → Force Update AndroidManifest
```

---


## Native Android Components

### SignInHelperActivity

A transparent helper activity that:

- Reads the Web Client ID from manifest meta-data
- Launches the Google Sign-In intent
- Extracts the ID Token
- Reports success or failure back to Unity

Declared automatically:

```xml
<activity
    android:name="com.unity.extension.GoogleAuth.SignInHelperActivity"
    android:theme="@android:style/Theme.Translucent.NoTitleBar.Fullscreen"
    android:exported="false" />
```

### GoogleSignInPlugin

A singleton bridge that:

- Stores Unity-side callbacks
- Dispatches results on the Unity main thread

---

## Usage

### Subscribe to Events

```csharp
GoogleAuthentication.OnStart += OnAuthorizeStart;
GoogleAuthentication.OnResultHandler += OnAuthorizeEnd;
```

### Start Authorization

```csharp
GoogleAuthentication.Authorize();
```

### Handle Result

```csharp
public void OnAuthorizeStart()
{
    Debug.Log("Google Sign-In started");
}

public void OnAuthorizeEnd(GoogleAuthentication.AuthResult result)
{
    Debug.Log($"Result: {result.result}");
    Debug.Log($"ID Token: {result.token_id}");
    Debug.Log($"Error: {result.error_message}");
}
```

---

## Error Handling

Failures are reported via `OnResultHandler`:

- Invalid or missing Web Client ID
- User cancellation
- Google Play Services errors

The helper activity always finishes after reporting the result.

---

## What This Package Does NOT Do

- No Firebase Authentication
- No Google Play Games Services
- No silent sign-in
- No server-side verification
- No access tokens or auth codes

---

## Use Cases

- Iran-restricted projects where GPGS is unavailable
- Lightweight authentication without Firebase
- Client-only Google identity usage

---

## License

**MIT License**

This license is compatible with:
- Unity UPM distribution
- Google Play Services SDK usage
- Open-source and commercial projects

---

## Author

**Mahdi Iran Nezhad**  
Email: yp2014ir@gmail.com

---

## Notes

- EDM4U must resolve dependencies before building
- Ensure `maven.google.com` is reachable
- ID Token validity depends on Google configuration

