Fleet Ledger App 🚛📊
The Fleet Ledger App allows users to manage fleet details, generate PDF reports, and manage them all within the app. Ideal for logistics managers, fleet owners, or anyone needing an efficient way to track and document fleet operations.

Features
 - Add Fleet Details – Input and store information about fleets. 
 - Generate PDF Reports – Create structured PDF reports from the fleet data.
 - View & Share PDFs – Easily access and share generated reports.
 - Delete PDFs – Manage and remove unnecessary reports directly within the app.

File Structure
The project's directory structure is as follows:
FleetApp/
├── app/                          # Main application module
│   ├── src/                      # Source files
│   │   ├── main/                 # Main source folder
│   │   │   ├── java/             # Java/Kotlin code files
│   │   │   │   └── com/          # Package for application
│   │   │   │       └── fleetapp/
│   │   │   │           ├── components/  # UI components
│   │   │   │           ├── dataclasses/ # Data models
│   │   │   │           ├── datastore/   # Datastore
│   │   │   │           ├── routes/      # Navigation routes
│   │   │   │           ├── screens/     # App composable screens
│   │   │   │           ├── ui/theme/    # App themes
│   │   │   │           ├── utilities/   # Utility functions
│   │   │   │           └── viewmodels/  # ViewModels and ViewModelFactory
│   │   │   │   └── MainActivity.kt     # Main Activity
│   │   │   ├── res/                # Resources (layouts, strings, images)
│   │   │   ├── AndroidManifest.xml  # App configuration
├── build.gradle                   # Project-level build file
├── settings.gradle                # Project-level settings
├── README.md                      # Project documentation (this file)

Installation
Clone this repository:
  - git clone https://github.com/amaankhan420/fleet-ledger.git
Open the project in Android Studio.
Build the project.

Technologies Used
  - Android Studio
  - Kotlin
  - Jetpack Compose
  - PDF generation libraries

License
This project is licensed under the MIT License – see the LICENSE file for details.
