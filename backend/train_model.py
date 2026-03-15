import pandas as pd
import numpy as np
import re
import os
import joblib
import nltk
from nltk.corpus import stopwords
from nltk.stem import PorterStemmer
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier, VotingClassifier, GradientBoostingClassifier
from sklearn.naive_bayes import MultinomialNB
from sklearn.svm import LinearSVC
from sklearn.calibration import CalibratedClassifierCV
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix
from sklearn.pipeline import Pipeline
from sklearn.utils import resample

# Download NLTK data
for resource in ['stopwords', 'punkt']:
    try:
        nltk.data.find(f'corpora/{resource}' if resource == 'stopwords' else f'tokenizers/{resource}')
    except LookupError:
        nltk.download(resource, quiet=True)

STOP_WORDS = set(stopwords.words('english'))
STEMMER = PorterStemmer()

# ─── Spam patterns for rule-based boosting ───────────────────────────────────
FINANCIAL_PATTERNS = [
    r'(bank|account).{0,20}(block|suspend|verif|updat)',
    r'(kyc|otp|pin|cvv).{0,20}(updat|verif|expir|shar)',
    r'(won|win|winner|lottery|jackpot).{0,30}(claim|collect|prize)',
    r'(cashback|refund|reward).{0,20}(claim|collect|credit)',
    r'[₹$£€]\s*\d{3,}',
    r'\d{3,}\s*[₹$£€]',
    r'(free|click).{0,20}(now|here|link|http)',
    r'(urgent|immediately|expire).{0,30}(click|verify|update|call)',
    r'(atm|debit|credit).{0,20}(block|suspend|expir)',
    r'(password|login|credential).{0,20}(reset|verif|click)',
]

PHISHING_URL_PATTERNS = [
    r'bit\.ly', r'tinyurl', r'goo\.gl', r'ow\.ly', r't\.co',
    r'http://', r'\.tk/', r'\.ml/', r'\.ga/', r'\.cf/',
    r'click\s*here', r'verify\s*now', r'login\s*now',
]

COMPILED_FINANCIAL = [re.compile(p, re.IGNORECASE) for p in FINANCIAL_PATTERNS]
COMPILED_PHISHING  = [re.compile(p, re.IGNORECASE) for p in PHISHING_URL_PATTERNS]


def preprocess(text):
    if not isinstance(text, str) or not text.strip():
        return ""
    text = text.lower()
    has_url = bool(re.search(r'https?://|www\.', text))
    has_money = bool(re.search(r'[₹$£€]|\d{4,}', text))
    text = re.sub(r'[^a-z\s]', ' ', text)
    text = re.sub(r'\s+', ' ', text).strip()
    words = [STEMMER.stem(w) for w in text.split() if w not in STOP_WORDS and len(w) > 1]
    if has_url:   words.append('has_url')
    if has_money: words.append('has_money')
    return ' '.join(words)


def rule_score(text):
    """Returns extra risk score from rule-based patterns (0.0 – 1.0)"""
    score = 0.0
    t = text if isinstance(text, str) else ""
    for pat in COMPILED_FINANCIAL:
        if pat.search(t):
            score += 0.15
    for pat in COMPILED_PHISHING:
        if pat.search(t):
            score += 0.10
    if t.count('!') >= 3:
        score += 0.05
    caps_words = [w for w in t.split() if w.isupper() and len(w) > 2]
    if len(caps_words) >= 2:
        score += 0.05
    return min(score, 1.0)


def detect_issues(text):
    issues = []
    t = text if isinstance(text, str) else ""
    tl = t.lower()
    fin_kw = ['bank','account','blocked','suspended','verify','kyc','otp','atm',
              'credit card','debit card','transaction','fraud','unauthorized']
    found = [k for k in fin_kw if k in tl]
    if found: issues.append(f"Financial scam indicators: {', '.join(found[:4])}")
    urg_kw = ['urgent','immediately','act now','expire','limited time','today only','asap']
    found = [k for k in urg_kw if k in tl]
    if found: issues.append(f"Urgency tactics: {', '.join(found[:3])}")
    prize_kw = ['won','winner','lottery','jackpot','prize','congratulations','claim']
    found = [k for k in prize_kw if k in tl]
    if found: issues.append(f"Prize/lottery scam: {', '.join(found[:3])}")
    phish_kw = ['click here','verify now','login now','reset password','confirm now']
    found = [k for k in phish_kw if k in tl]
    if found: issues.append(f"Phishing patterns: {', '.join(found[:3])}")
    if re.search(r'https?://|bit\.ly|tinyurl|goo\.gl', t, re.I):
        issues.append("Suspicious URL detected")
    if t.count('!') >= 3:
        issues.append("Excessive punctuation")
    caps = [w for w in t.split() if w.isupper() and len(w) > 2]
    if len(caps) >= 2:
        issues.append("Excessive capitalization")
    if re.search(r'[₹$£€]\s*\d{3,}|\d{3,}\s*[₹$£€]', t):
        issues.append("Contains large monetary amount")
    return issues


# ─── Dataset loaders ─────────────────────────────────────────────────────────

def load_email(path):
    df = pd.read_csv(path)
    # columns: Category, Message
    df = df.rename(columns={'Category': 'label', 'Message': 'text'})
    df['label'] = df['label'].str.strip().str.lower()
    df = df[df['label'].isin(['ham', 'spam'])][['label', 'text']].dropna()
    print(f"  email.csv          → {len(df):>6} rows  (spam={len(df[df.label=='spam'])})")
    return df


def load_spam_csv(path):
    df = pd.read_csv(path, usecols=[0, 1], header=0, encoding='latin-1')
    df.columns = ['label', 'text']
    df['label'] = df['label'].str.strip().str.lower()
    df = df[df['label'].isin(['ham', 'spam'])][['label', 'text']].dropna()
    print(f"  spam.csv           → {len(df):>6} rows  (spam={len(df[df.label=='spam'])})")
    return df


def load_spam_dataset(path):
    df = pd.read_csv(path)
    # columns: message_content, is_spam
    df = df.rename(columns={'message_content': 'text', 'is_spam': 'label'})
    df['label'] = df['label'].apply(lambda x: 'spam' if str(x).strip() in ['1', 'spam', 'True', 'true'] else 'ham')
    df = df[['label', 'text']].dropna()
    print(f"  spam_dataset.csv   → {len(df):>6} rows  (spam={len(df[df.label=='spam'])})")
    return df


def load_youtube(path):
    df = pd.read_csv(path)
    # columns: COMMENT_ID, AUTHOR, DATE, CONTENT, VIDEO_NAME, CLASS
    df = df.rename(columns={'CONTENT': 'text', 'CLASS': 'label'})
    df['label'] = df['label'].apply(lambda x: 'spam' if str(x).strip() == '1' else 'ham')
    df = df[['label', 'text']].dropna()
    print(f"  Youtube-Spam.csv   → {len(df):>6} rows  (spam={len(df[df.label=='spam'])})")
    return df


def load_phishing(path):
    df = pd.read_csv(path)
    # columns: url, type  (benign / phishing / defacement / malware)
    df = df.rename(columns={'url': 'text', 'type': 'label'})
    df['label'] = df['label'].apply(lambda x: 'spam' if str(x).strip() != 'benign' else 'ham')
    # Sample to avoid dominating the dataset (651k rows)
    spam_df = df[df.label == 'spam'].sample(n=min(15000, len(df[df.label=='spam'])), random_state=42)
    ham_df  = df[df.label == 'ham'].sample(n=min(15000, len(df[df.label=='ham'])),  random_state=42)
    df = pd.concat([spam_df, ham_df])
    print(f"  malicious_phish    → {len(df):>6} rows  (spam={len(df[df.label=='spam'])})")
    return df


# ─── Main training ────────────────────────────────────────────────────────────

class SpamDetector:
    def __init__(self):
        self.vectorizer = TfidfVectorizer(
            max_features=30000,
            ngram_range=(1, 3),
            sublinear_tf=True,
            min_df=2,
            analyzer='word'
        )
        # Ensemble: LR + calibrated LinearSVC + GradientBoosting
        lr  = LogisticRegression(C=5.0, max_iter=1000, class_weight='balanced', solver='lbfgs')
        svc = CalibratedClassifierCV(LinearSVC(C=1.0, max_iter=2000, class_weight='balanced'))
        gb  = GradientBoostingClassifier(n_estimators=100, max_depth=4, random_state=42)
        self.model = VotingClassifier(
            estimators=[('lr', lr), ('svc', svc), ('gb', gb)],
            voting='soft',
            weights=[3, 3, 2]
        )

    def fit(self, X_raw, y):
        X = self.vectorizer.fit_transform([preprocess(t) for t in X_raw])
        self.model.fit(X, y)

    def predict_proba_single(self, text):
        X = self.vectorizer.transform([preprocess(text)])
        proba = self.model.predict_proba(X)[0]
        return proba  # [ham_prob, spam_prob]

    def predict(self, text):
        proba = self.predict_proba_single(text)
        ml_spam_prob = float(proba[1])
        rs = rule_score(text)
        # Blend ML probability with rule score
        final_score = min(ml_spam_prob * 0.65 + rs * 0.35 + rs * ml_spam_prob * 0.35, 1.0)

        if final_score >= 0.60:
            risk_level = "HIGH_RISK"
            prediction = "spam"
        elif final_score >= 0.30:
            risk_level = "SUSPICIOUS"
            prediction = "spam"
        else:
            risk_level = "SAFE"
            prediction = "ham"

        issues = detect_issues(text)
        if risk_level == "SAFE" and len(issues) >= 2:
            risk_level = "SUSPICIOUS"
            prediction = "spam"
            final_score = max(final_score, 0.35)

        if risk_level in ("SUSPICIOUS", "SAFE") and len(issues) >= 4:
            risk_level = "HIGH_RISK"
            prediction = "spam"
            final_score = max(final_score, 0.65)

        explanation = {
            "HIGH_RISK": "High confidence threat detected. Multiple spam/phishing indicators found.",
            "SUSPICIOUS": "Suspicious patterns detected. Exercise caution.",
            "SAFE": "No significant spam indicators detected."
        }[risk_level]

        return {
            'prediction': prediction,
            'risk_level': risk_level,
            'risk_score': round(final_score, 4),
            'confidence': round(float(max(proba)), 4),
            'detected_issues': issues,
            'explanation': explanation
        }

    def save(self, folder='models'):
        os.makedirs(folder, exist_ok=True)
        joblib.dump(self.model,      os.path.join(folder, 'spam_model.pkl'))
        joblib.dump(self.vectorizer, os.path.join(folder, 'vectorizer.pkl'))
        print(f"\n✅ Model saved → {folder}/")

    def load(self, folder='models'):
        self.model      = joblib.load(os.path.join(folder, 'spam_model.pkl'))
        self.vectorizer = joblib.load(os.path.join(folder, 'vectorizer.pkl'))


def main():
    BASE = os.path.dirname(os.path.abspath(__file__))
    DS   = os.path.join(BASE, '..', 'datasets')

    print("=" * 60)
    print("  AI Spam Detector — Full Dataset Training")
    print("=" * 60)
    print("\n📂 Loading datasets...")

    frames = []
    loaders = [
        (load_email,        os.path.join(DS, 'email.csv')),
        (load_spam_csv,     os.path.join(DS, 'spam.csv')),
        (load_spam_dataset, os.path.join(DS, 'spam_dataset.csv')),
        (load_youtube,      os.path.join(DS, 'Youtube-Spam-Dataset.csv')),
        (load_phishing,     os.path.join(DS, 'malicious_phish (2).csv')),
    ]
    for fn, path in loaders:
        try:
            frames.append(fn(path))
        except Exception as e:
            print(f"  ⚠ Skipped {os.path.basename(path)}: {e}")

    df = pd.concat(frames, ignore_index=True).dropna(subset=['text'])
    df['text'] = df['text'].astype(str)

    print(f"\n📊 Combined dataset: {len(df):,} rows")
    print(f"   Spam : {len(df[df.label=='spam']):,}")
    print(f"   Ham  : {len(df[df.label=='ham']):,}")

    # Balance classes
    spam_df = df[df.label == 'spam']
    ham_df  = df[df.label == 'ham']
    n = min(len(spam_df), len(ham_df), 25000)
    spam_df = resample(spam_df, n_samples=n, random_state=42)
    ham_df  = resample(ham_df,  n_samples=n, random_state=42)
    df = pd.concat([spam_df, ham_df]).sample(frac=1, random_state=42).reset_index(drop=True)

    print(f"\n⚖️  Balanced to {len(df):,} rows ({n:,} spam + {n:,} ham)")

    X = df['text'].values
    y = (df['label'] == 'spam').astype(int).values

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.2, random_state=42, stratify=y
    )

    print(f"\n🔧 Training on {len(X_train):,} samples...")
    detector = SpamDetector()
    detector.fit(X_train, y_train)

    # Evaluate
    X_test_vec = detector.vectorizer.transform([preprocess(t) for t in X_test])
    y_pred = detector.model.predict(X_test_vec)
    acc = accuracy_score(y_test, y_pred)

    print(f"\n{'='*60}")
    print(f"  ✅ Accuracy : {acc*100:.2f}%")
    print(f"{'='*60}")
    print(classification_report(y_test, y_pred, target_names=['ham', 'spam']))

    cm = confusion_matrix(y_test, y_pred)
    print(f"Confusion Matrix:\n  TN={cm[0,0]}  FP={cm[0,1]}\n  FN={cm[1,0]}  TP={cm[1,1]}")

    detector.save(os.path.join(BASE, 'models'))

    # Live test
    print(f"\n{'='*60}")
    print("  🧪 Live Prediction Tests")
    print(f"{'='*60}")
    tests = [
        ("SAFE",      "Meeting rescheduled to 3pm tomorrow, see you there"),
        ("SAFE",      "Your Amazon order has been shipped and will arrive Friday"),
        ("HIGH_RISK", "URGENT: Your bank account is SUSPENDED! Verify now: bit.ly/fix123"),
        ("HIGH_RISK", "Congratulations! You WON ₹50,000 lottery. Claim now: http://prize.tk"),
        ("HIGH_RISK", "Your ATM card will be blocked. Update KYC immediately: www.bank-kyc.ml"),
        ("SUSPICIOUS","Limited time offer! 50% discount on all products today only"),
        ("HIGH_RISK", "FREE iPhone! Click here now to claim your prize!!!"),
        ("SUSPICIOUS","You have a pending refund. Click to verify your account details"),
        ("SAFE",      "Hi, can we reschedule our call to next Monday?"),
        ("HIGH_RISK", "Your OTP is 847291. Never share this. (This is a test phishing msg)"),
    ]
    for expected, msg in tests:
        r = detector.predict(msg)
        status = "✅" if r['risk_level'] == expected else "⚠️ "
        print(f"\n{status} [{expected}→{r['risk_level']}] {msg[:60]}")
        print(f"   Score={r['risk_score']:.2f}  Issues={len(r['detected_issues'])}")

    print(f"\n{'='*60}")
    print("  Training complete!")
    print(f"{'='*60}\n")


if __name__ == '__main__':
    main()
