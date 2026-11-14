# MediTrack: Setup and Run Guide (OpenJDK 27)

## 0. What you need
| Tool | Version | Notes |
|---|---|---|
| OpenJDK | 27 | Runs Gradle (the build tool). Temurin or any OpenJDK 27 build works. |
| Android Studio | Panda 4 (2025.3.4) or newer, Quail recommended | Must support AGP 9.2. |
| Gradle | 9.8.2 | Downloaded automatically from `gradle-wrapper.properties`. First Gradle with Java 27 support is 9.8. |
| Android SDK | Platform 36, Build-Tools, Platform-Tools, Emulator | Installed from SDK Manager. |
| Phone | Android 10 (API 29) or newer | For the real-device run. |

Why JDK 27 *and* Java 17 in the code: JDK 27 is only the JDK that runs Gradle. The app itself is compiled to
Java 17 bytecode (`compileOptions` in `app/build.gradle.kts`) because Android does not accept Java 27 output.

## 1. Install OpenJDK 27
1. Download OpenJDK 27 (for example Eclipse Temurin 27 from adoptium.net, or jdk.java.net/27).
2. Install it. Note the install folder:
   - Windows: `C:\Program Files\Eclipse Adoptium\jdk-27...`
   - macOS: `/Library/Java/JavaVirtualMachines/temurin-27.jdk/Contents/Home`
   - Linux: `/usr/lib/jvm/...-27...`
3. Verify in a new terminal: `java -version` must say 27.
4. Optional: set `JAVA_HOME` to that folder.

## 2. Install Android Studio and SDK
1. Install Android Studio and open it once; accept the default SDK setup.
2. Tools > SDK Manager > SDK Platforms: tick **Android 16 (API 36)**. Apply.
3. SDK Tools tab: tick **Android SDK Build-Tools**, **Platform-Tools**, **Android Emulator**. Apply.

## 3. Open the project and use JDK 27
1. Unzip `MediTrack-JDK27.zip`. File > Open > select the `MediTrack` folder > Trust Project.
2. Set the Gradle JDK: Settings (Ctrl+Alt+S, or Android Studio > Settings on Mac) >
   Build, Execution, Deployment > Build Tools > Gradle > **Gradle JDK** > Add JDK from disk /
   Download JDK > pick your JDK 27 folder. Apply.
3. File > Sync Project with Gradle Files. The first sync downloads Gradle 9.8.2 and libraries, so you need internet once.
4. Build > Make Project. Wait for "BUILD SUCCESSFUL".

If sync fails because of JDK 27 (errors about jlink, JdkImageTransform, "class file version" or an
unsupported Java version): in the same Gradle JDK dropdown choose the **embedded / jbr** JDK that comes
with Android Studio. The app code and the rest of the build do not change.

## 4A. Run on the emulator
1. Tools > Device Manager > **Create Virtual Device**.
2. Pick a phone (for example Pixel 8) > Next. Pick a system image of API 29 or higher (API 36 "Google APIs"
   is fine). Click Download if needed. On an Apple-silicon Mac choose an arm64 image. Finish.
3. On Windows or Linux, make sure hardware acceleration is on (BIOS virtualization, Windows Hypervisor Platform / KVM).
4. Press the play button on the AVD, wait for the home screen.
5. Select the emulator in the device dropdown (top toolbar), then press **Run** (green triangle, Shift+F10).
6. In the app: allow **Notifications**, then tap **Open settings** and enable **Alarms & reminders**
   (or Settings > Apps > MediTrack > Alarms & reminders).
7. Test reminders:
   - Medicines tab > **+** > fill the form > pick a time 2 minutes ahead > Save.
   - Press Home or lock the emulator. The notification should appear on time.
   - Tap TAKEN, SNOOZE 10 MIN or SKIP, then open the History tab.
8. Test "Missed": temporarily set `MISSED_AFTER_MS = 60_000L` in `util/Config.kt`, run again, and ignore a reminder.
9. Test offline: turn on Airplane mode in the emulator. Everything except SMS should still work.
10. Test SMS on emulators: start a second emulator (its port number is shown in the title bar, e.g. 5556).
    Save that number as an emergency contact, press "Send emergency SMS now", and check the second emulator's
    Messages app. The emulator's Extended controls (...) > Phone also lets you send a test SMS to the device.
11. Watch logs: Logcat panel, filter by `com.meditrack.app`.

## 4B. Run on a real phone (USB)
1. On the phone: Settings > About phone > tap **Build number** 7 times > enter your PIN.
2. Settings > System > Developer options > turn on **USB debugging**.
3. Connect the phone with a data-capable USB cable. Choose "File transfer / Android Auto" mode if asked.
4. On the phone, tap **Allow** on "Allow USB debugging?" (tick "Always allow").
5. In Android Studio, the phone's name appears in the device dropdown. If not: try another cable or port,
   install the phone maker's USB driver (Windows), or run `adb devices` (it must say `device`, not `unauthorized`).
6. Select the phone and press **Run**. The app installs and opens.
7. Grant Notifications, and enable **Alarms & reminders** when asked.
8. Keep reminders reliable on real phones:
   - Settings > Apps > MediTrack > Battery > **Unrestricted** (or "Don't optimize").
   - Samsung: Battery > Background usage limits > remove MediTrack from "Sleeping apps".
   - Xiaomi, Oppo, Vivo, Realme: enable **Autostart** and allow background activity for MediTrack.
9. SMS test: you need a SIM with signal and load. Add a contact (use your own second number first),
   then tap "Send emergency SMS now" and allow the SMS permission.

## 4C. Run on a real phone (Wi-Fi, Android 11+)
1. Phone and computer must be on the same Wi-Fi.
2. Developer options > **Wireless debugging** > turn on.
3. Android Studio: Device Manager > **Pair Devices Over Wi-Fi** > scan the QR code from
   Wireless debugging > "Pair device with QR code" (or use the pairing code option).
4. The phone appears in the device dropdown. Press Run.

## 4D. Install without Android Studio (APK)
1. Build > Build Bundle(s) / APK(s) > **Build APK(s)**.
2. The file is at `app/build/outputs/apk/debug/app-debug.apk`.
3. Install with `adb install -r app-debug.apk`, or copy the file to the phone and open it
   (allow "Install unknown apps" for your file manager).

## 5. Troubleshooting
| Problem | Fix |
|---|---|
| Sync says "Unsupported class file major version" or jlink error | Use the embedded JBR as Gradle JDK (see section 3). |
| "Could not resolve ..." | Check internet, then File > Sync again. Try File > Invalidate Caches. |
| `kotlin-android` plugin error | Do not add it. AGP 9 has Kotlin built in. |
| Version mismatch with your Android Studio | Update Android Studio (Help > Check for Updates). |
| Notifications do not show | Allow notifications and Alarms & reminders; set battery to Unrestricted. |
| Reminder arrives late on an emulator | Emulators sleep; keep it awake or test on a real phone. |
| Phone not listed | Cable and port, USB mode, revoke USB debugging authorizations, reconnect. |
| SMS not sent | Needs SIM, signal and SMS permission. Emulators only reach other emulators. |
