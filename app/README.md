# PokémonBox

## 📝 Project Description
Android application developed in Kotlin for exploring the Pokémon database using PokéAPI, with intuitive navigation and search capabilities.

## ✨ Key Features
- Paginated Pokémon list display
- Dynamic page loading
- Pokémon search by name
- Efficient MVVM architecture

## 🛠 Technologies Used
- **Language**: Kotlin
- **Architecture**: MVVM
- **Networking**: Retrofit
- **Asynchronous**: Kotlin Coroutines
- **API**: PokéAPI (https://pokeapi.co)

## 📋 Requirements
- Android 6.0+
- Internet Connection

## 🔧 Project Dependencies
```gradle
dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    
    // Lifecycle & LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.5.0")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    
    // Coroutines & Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:3.9.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.0")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.1.0")
}
```

## 🏗 MVVM Architecture
- **Model**: Data and API management
- **View**: User interfaces
- **ViewModel**: Presentation logic

## 🔍 Implementation Details

### Pagination
- Loading 20 Pokémon per page
- Automatic paging at list bottom

### Search
- Pokémon name filtering
- Real-time search functionality

## 🧪 Unit Testing
Tests implemented for:
- Pagination logic
- Search functionality
- API calls
- Data processing

### Running Tests
```bash
./gradlew test
```

## 🚀 Installation
1. Clone the repository
2. Open with Android Studio
3. Sync Gradle dependencies
4. Run on emulator/device

## 📈 Future Improvements
- Offline mode
- Dark theme support

## 📄 License

MIT License

Copyright (c) 2024 Simone Paolo Petta

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
