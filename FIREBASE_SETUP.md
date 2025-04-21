# Firebase Setup Instructions

To complete the setup of this chat application, you need to configure Firebase. Follow these steps:

## 1. Create a Firebase Project

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" and follow the steps to create a new Firebase project
3. Give your project a name and accept the Firebase terms

## 2. Register Your Android App

1. In the Firebase project overview, click the Android icon to add an Android app
2. Enter your app's package name: `com.chandra.walkby`
3. (Optional) Enter a nickname for your app
4. Register the app

## 3. Download and Add Configuration File

1. Download the `google-services.json` file from Firebase
2. Place this file in your app's root directory (inside the `app` folder)

## 4. Enable Authentication

1. In the Firebase console, go to "Authentication" from the left menu
2. Click "Get started"
3. Enable "Email/Password" authentication by clicking on it and toggling the "Enable" switch
4. Save your changes

## 5. Set Up Firestore Database

1. In the Firebase console, go to "Firestore Database" from the left menu
2. Click "Create database"
3. Choose "Start in test mode" for development purposes (you'll want to set up security rules before production)
4. Select a location for your database that's closest to your users
5. Click "Enable"

## 6. Build and Run the App

Now you can build and run your application! The app will connect to Firebase using the configuration from the `google-services.json` file.

## Security Note

The current implementation uses Firestore in test mode, which allows unrestricted access. Before deploying to production, you should set up proper security rules to protect your data. 