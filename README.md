# Fruit Disease Prediction and Classification

A machine learning project that takes a photo of a fruit and detects whether it has a disease. It supports three fruits  **Apple**, **Mango**, and **Orange** and includes both the model training code and an Android app to use on your phone.


## What's Inside


├── CNN_model.ipynb        # General CNN training notebook
├── applecnn.ipynb         # Apple disease model training
├── mangocnn.ipynb         # Mango disease model training
├── orangecnn.ipynb        # Orange disease model training
├── apple_model.tflite     # Trained Apple model (mobile-ready)
├── mango_model.tflite     # Trained Mango model (mobile-ready)
├── orange_model.tflite    # Trained Orange model (mobile-ready)
└── app/                   # Android app source code (Java)

> The `master` branch contains everything — model training notebooks, trained `.tflite` models, and the Android app.


## Machine Learning (Notebooks)

Models are trained using CNN (Convolutional Neural Networks) in Python with TensorFlow/Keras. Each notebook loads fruit disease images, trains the model, and exports a `.tflite` file for mobile use.

**Tools:** Python, TensorFlow, Keras, Jupyter Notebook


## Android App

Built in **kotlin** using Android Studio. The user picks a fruit, takes or selects a photo, and the app runs the matching `.tflite` model on-device to show the result, no internet needed.

**Tools:** kotlin, Android Studio, TensorFlow Lite, Gradle (Kotlin DSL)

**Minimum:** Android 5.0+


## How to Run

**Notebooks:**
'''bash
git clone https://github.com/abhi6744/Fruit-Disease-prediction-classification.git
'''
# Open any .ipynb in Jupyter or Google Colab and run all cells

**Android App:**
1. Open the project in Android Studio
2. Place `.tflite` files in `app/src/main/assets/`
3. Connect a device or emulator and click **Run**


## Tech Stack

| Part | Technology |
|------|-----------|
| Model Training | Python, TensorFlow, Keras |
| Model Format | TensorFlow Lite (.tflite) |
| Mobile App | Java, Android Studio |
| Build System | Gradle (Kotlin DSL) |


