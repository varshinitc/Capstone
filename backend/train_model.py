import pandas as pd
import numpy as np
import re
import os
import joblib
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.naive_bayes import MultinomialNB
from sklearn.metrics import accuracy_score, classification_report
import nltk
from nltk.corpus import stopwords
from nltk.stem import PorterStemmer

# Download required NLTK data
try:
    nltk.data.find('corpora/stopwords')
except LookupError:
    nltk.download('stopwords')

class MultiSourceSpamDetector:
    def __init__(self):
        self.vectorizer = TfidfVectorizer(max_features=5000, ngram_range=(1, 2))
        self.model = MultinomialNB()
        self.stemmer = PorterStemmer()
        self.stop_words = set(stopwords.words('english'))
        
    def load_multiple_datasets(self, data_folder='data'):
        """Load and combine all CSV datasets"""
        datasets = []
        dataset_files = [
            'sms_spam.csv',
            'email_spam.csv',
            'youtube_comments.csv',
            'notification_spam.csv'
        ]
        
        for file in dataset_files:
            file_path = os.path.join(data_folder, file)
            if os.path.exists(file_path):
                df = pd.read_csv(file_path)
                datasets.append(df)
                print(f"Loaded {file}: {len(df)} records")
        
        # Combine all datasets
        combined_df = pd.concat(datasets, ignore_index=True)
        print(f"\nTotal records: {len(combined_df)}")
        print(f"Spam: {len(combined_df[combined_df['label'] == 'spam'])}")
        print(f"Ham: {len(combined_df[combined_df['label'] == 'ham'])}")
        
        return combined_df
    
    def preprocess_text(self, text):
        """Complete preprocessing pipeline"""
        if not isinstance(text, str):
            return ""
        
        # Convert to lowercase
        text = text.lower()
        
        # Flag URLs
        has_url = bool(re.search(r'http[s]?://|www\.', text))
        
        # Remove special characters and punctuation
        text = re.sub(r'[^a-zA-Z\s]', ' ', text)
        
        # Remove extra spaces
        text = re.sub(r'\s+', ' ', text).strip()
        
        # Tokenize and remove stop words
        words = text.split()
        words = [word for word in words if word not in self.stop_words]
        
        # Apply stemming
        words = [self.stemmer.stem(word) for word in words]
        
        # Add URL flag as feature
        if has_url:
            words.append('url_present')
        
        return ' '.join(words)
    
    def extract_features(self, messages):
        """Apply preprocessing to all messages"""
        return [self.preprocess_text(msg) for msg in messages]
    
    def train(self, data_folder='data'):
        """Train the model"""
        # Load datasets
        df = self.load_multiple_datasets(data_folder)
        
        # Preprocess messages
        print("\nPreprocessing messages...")
        df['processed_message'] = self.extract_features(df['message'])
        
        # Prepare data
        X = df['processed_message']
        y = df['label'].map({'spam': 1, 'ham': 0})
        
        # Split data
        X_train, X_test, y_train, y_test = train_test_split(
            X, y, test_size=0.2, random_state=42, stratify=y
        )
        
        # Vectorize
        print("\nVectorizing text...")
        X_train_vec = self.vectorizer.fit_transform(X_train)
        X_test_vec = self.vectorizer.transform(X_test)
        
        # Train model
        print("Training model...")
        self.model.fit(X_train_vec, y_train)
        
        # Evaluate
        y_pred = self.model.predict(X_test_vec)
        accuracy = accuracy_score(y_test, y_pred)
        
        print(f"\n{'='*50}")
        print(f"Model Accuracy: {accuracy*100:.2f}%")
        print(f"{'='*50}")
        print("\nClassification Report:")
        print(classification_report(y_test, y_pred, target_names=['ham', 'spam']))
        
        return accuracy
    
    def save_model(self, model_folder='models'):
        """Save model and vectorizer"""
        os.makedirs(model_folder, exist_ok=True)
        joblib.dump(self.model, os.path.join(model_folder, 'spam_model.pkl'))
        joblib.dump(self.vectorizer, os.path.join(model_folder, 'vectorizer.pkl'))
        print(f"\nModel saved to {model_folder}/")
    
    def load_model(self, model_folder='models'):
        """Load saved model and vectorizer"""
        self.model = joblib.load(os.path.join(model_folder, 'spam_model.pkl'))
        self.vectorizer = joblib.load(os.path.join(model_folder, 'vectorizer.pkl'))
        print("Model loaded successfully")
    
    def detect_all_issues(self, text):
        """Identify spam indicators in text"""
        issues = []
        text_lower = text.lower()
        
        # Check for financial scam keywords (HIGH PRIORITY)
        financial_scam_keywords = ['cashback', 'rupees', 'won', 'winner', 'prize', 'reward',
                                   'atm', 'card blocked', 'kyc', 'bank', 'account blocked',
                                   'verify', 'update details', 'claim now', 'expire', 'midnight',
                                   'blocked', 'update', 'claim']
        found_financial = [kw for kw in financial_scam_keywords if kw in text_lower]
        if found_financial:
            issues.append(f"Financial scam indicators: {', '.join(found_financial)}")
        
        # Check for spam keywords
        spam_keywords = ['free', 'click', 'urgent', 'limited', 'offer', 'discount', 'congratulations']
        found_keywords = [kw for kw in spam_keywords if kw in text_lower]
        if found_keywords:
            issues.append(f"Spam keywords: {', '.join(found_keywords)}")
        
        # Check for URLs
        if re.search(r'http[s]?://|www\.', text):
            issues.append("Contains suspicious URL")
        
        # Check for excessive punctuation
        if text.count('!') > 2:
            issues.append("Excessive exclamation marks")
        
        # Check for all caps words
        words = text.split()
        caps_words = [w for w in words if w.isupper() and len(w) > 2]
        if len(caps_words) > 2:
            issues.append("Multiple words in ALL CAPS")
        
        # Check for money symbols and amounts
        if re.search(r'[₹$£€]\s*\d+|\d+\s*[₹$£€]|\d{3,}\s*(rupees|dollars|pounds)', text_lower):
            issues.append("Contains monetary amounts")
        
        return issues
    
    def generate_explanation(self, prediction, confidence, issues):
        """Generate human-readable explanation"""
        if prediction == 'spam':
            if confidence > 0.8:
                explanation = "High confidence spam detection. "
            else:
                explanation = "Moderate confidence spam detection. "
            
            if issues:
                explanation += "Issues found: " + "; ".join(issues)
            else:
                explanation += "Message pattern matches known spam."
        else:
            explanation = "Message appears legitimate. No significant spam indicators detected."
        
        return explanation
    
    def predict(self, message):
        """Predict single message with enhanced rule-based detection"""
        # Preprocess
        processed = self.preprocess_text(message)
        
        # Vectorize
        vectorized = self.vectorizer.transform([processed])
        
        # Predict
        prediction = self.model.predict(vectorized)[0]
        probability = self.model.predict_proba(vectorized)[0]
        
        # Get confidence
        confidence = probability[prediction]
        
        # Detect issues
        issues = self.detect_all_issues(message)
        
        # Enhanced risk determination with rule-based override
        text_lower = message.lower()
        
        # HIGH PRIORITY: Financial scam detection
        financial_scam_patterns = [
            ('cashback' in text_lower and 'claim' in text_lower),
            ('won' in text_lower and 'rupees' in text_lower),
            ('card' in text_lower and 'blocked' in text_lower),
            ('kyc' in text_lower and 'update' in text_lower),
            ('account' in text_lower and 'blocked' in text_lower),
            (re.search(r'[₹$]\s*\d{4,}', message)),  # Large amounts
        ]
        
        has_financial_scam = any(financial_scam_patterns)
        has_url = bool(re.search(r'http[s]?://|www\.', message))
        
        # Override prediction for obvious scams
        if has_financial_scam and has_url:
            risk_level = "HIGH_RISK"
            risk_score = 0.95
            pred_label = 'spam'
        elif has_financial_scam or (len(issues) >= 3):
            risk_level = "HIGH_RISK"
            risk_score = 0.85
            pred_label = 'spam'
        elif prediction == 1:  # spam from ML model
            if confidence > 0.8 or len(issues) >= 2:
                risk_level = "HIGH_RISK"
                risk_score = confidence
            else:
                risk_level = "SUSPICIOUS"
                risk_score = confidence * 0.7
            pred_label = 'spam'
        else:  # ham
            if len(issues) > 0:
                risk_level = "SUSPICIOUS"
                risk_score = 0.5
                pred_label = 'spam'
            else:
                risk_level = "SAFE"
                risk_score = 1 - confidence
                pred_label = 'ham'
        
        # Generate explanation
        explanation = self.generate_explanation(pred_label, confidence, issues)
        
        return {
            'prediction': pred_label,
            'confidence': float(confidence),
            'risk_score': float(risk_score),
            'risk_level': risk_level,
            'detected_issues': issues,
            'explanation': explanation
        }

def main():
    print("="*60)
    print("Multi-Source Spam Detection Model Training")
    print("="*60)
    
    # Initialize detector
    detector = MultiSourceSpamDetector()
    
    # Train model
    accuracy = detector.train()
    
    # Save model
    detector.save_model()
    
    # Test predictions
    print("\n" + "="*60)
    print("Testing Model Predictions")
    print("="*60)
    
    test_messages = [
        "Your order has been delivered successfully",
        "CONGRATULATIONS! You won $10000! Click here now!",
        "Meeting scheduled for tomorrow at 3pm",
        "FREE GIFT! Limited time offer! Click now!",
        "You have received ₹50,000 cashback. Claim now: http://free-money.com",
        "Your ATM card will be blocked. Update KYC: http://bank-update.com"
    ]
    
    for msg in test_messages:
        result = detector.predict(msg)
        print(f"\nMessage: {msg}")
        print(f"Prediction: {result['prediction'].upper()}")
        print(f"Risk Level: {result['risk_level']}")
        print(f"Confidence: {result['confidence']:.2%}")
        print(f"Issues: {', '.join(result['detected_issues']) if result['detected_issues'] else 'None'}")
    
    print("\n" + "="*60)
    print("Training Complete!")
    print("="*60)

if __name__ == "__main__":
    main()
