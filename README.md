# FirebaseUI Quickstarts for Android
A simple a quickstarts project for getting you started with Firebase for Android app development using [FirebaseUI](https://github.com/firebase/FirebaseUI-Android).
There is also a demo Activity that demonstrate users authentication, photo upload to cloud storage and writing data to the realtime database in a single flow.

## The project is setup using:
- [FirebaseUI](https://github.com/firebase/FirebaseUI-Android)
- [FirebaseUI-database](https://github.com/firebase/FirebaseUI-Android/tree/master/database)
- [FirebaseUI-auth (with Fabric repository)](https://github.com/firebase/FirebaseUI-Android/tree/master/auth)
- [FirebaseUI-storage](https://github.com/firebase/FirebaseUI-Android/tree/master/storage)
- [Butterknife](https://github.com/JakeWharton/butterknife)
- [Glide](https://github.com/bumptech/glide)
- [Espresso](http://google.github.io/android-testing-support-library/docs/espresso)

## Getting Started
- Log in to the [Firebase Console](https://console.firebase.google.com)
- Add a new project
- Register your app to the created project by clicking on "Add Firebase to your Android app" in the Firebase Console.
  Make sure your package name matches the package name of your app.

  Add your [debug certificate](https://developers.google.com/android/guides/client-auth) SHA-1 if you are going to enable Google Sign-In


  **NOTE: The Google services plugin for Gradle has already been added to this project, so remember to skip the Add Firebase SDK instruction step**
  
  ##### Getting your debug certificate on Mac/Linux
  ```
  keytool -exportcert -list -v  -alias androiddebugkey -keystore ~/.android/debug.keystore
  ```
 
  ##### Getting your debug certificate on Windows
  ```
  keytool -exportcert -list -v -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore
  ```
  
- Go to Authentication tab in the Firebase Console and enable your Sign-in methods
- Run the app on Android device or emulator with Google Play Services
- Well done, you are almost there! 
- Tap on the FloatingActionButton in the app to verify you setup
- Clap for yourself, because you made it to the end! Happy coding

## Requirements
- Android SDK.
- Android 7.0 (API 25) .
- Android SDK Tools
- Android SDK Build tools 25.0.2
- Android Support Repository
- Android Support library
- Google Play Services
