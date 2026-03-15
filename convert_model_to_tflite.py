#!/usr/bin/env python3
"""
Convert the scikit-learn spam model to TensorFlow Lite for Android
"""
import pickle
import numpy as np
import tensorflow as tf
from tensorflow import keras

# Load the trained model and vectorizer
with open('backend/models/spam_model.pkl', 'rb') as f:
    model = pickle.load(f)

with open('backend/models/vectorizer.pkl', 'rb') as f:
    vectorizer = pickle.load(f)

print("Loaded model and vectorizer")
print(f"Model type: {type(model)}")
print(f"Vectorizer type: {type(vectorizer)}")

# Get model parameters
try:
    # For logistic regression or similar models
    weights = model.coef_
    bias = model.intercept_
    
    print(f"Weights shape: {weights.shape}")
    print(f"Bias shape: {bias.shape}")
    
    # Create a simple TensorFlow model that mimics the sklearn model
    input_dim = weights.shape[1]
    
    tf_model = keras.Sequential([
        keras.layers.Input(shape=(input_dim,)),
        keras.layers.Dense(1, activation='sigmoid', use_bias=True)
    ])
    
    # Set the weights from sklearn model
    tf_model.layers[0].set_weights([weights.T, bias])
    
    # Convert to TensorFlow Lite
    converter = tf.lite.TFLiteConverter.from_keras_model(tf_model)
    converter.optimizations = [tf.lite.Optimize.DEFAULT]
    tflite_model = converter.convert()
    
    # Save the model
    with open('app/src/main/assets/spam_model.tflite', 'wb') as f:
        f.write(tflite_model)
    
    print("✅ TensorFlow Lite model saved to app/src/main/assets/spam_model.tflite")
    
    # Save vectorizer vocabulary for Android
    vocabulary = vectorizer.vocabulary_
    with open('app/src/main/assets/vocabulary.txt', 'w') as f:
        # Sort by index
        sorted_vocab = sorted(vocabulary.items(), key=lambda x: x[1])
        for word, idx in sorted_vocab:
            f.write(f"{word}\n")
    
    print("✅ Vocabulary saved to app/src/main/assets/vocabulary.txt")
    print(f"Vocabulary size: {len(vocabulary)}")
    
except Exception as e:
    print(f"Error: {e}")
    print("\nNote: This script works with Logistic Regression or similar linear models.")
    print("For other model types, a different conversion approach is needed.")
