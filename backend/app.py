from flask import Flask, request, jsonify
from flask_cors import CORS
import joblib
import os
from train_model import MultiSourceSpamDetector

app = Flask(__name__)
CORS(app)

# Load model at startup
detector = MultiSourceSpamDetector()
model_folder = 'models'

try:
    if os.path.exists(os.path.join(model_folder, 'spam_model.pkl')):
        detector.load_model(model_folder)
        print("✓ Model loaded successfully")
    else:
        print("⚠ Model not found. Please run train_model.py first")
except Exception as e:
    print(f"Error loading model: {e}")

@app.route('/', methods=['GET'])
def home():
    """API information endpoint"""
    return jsonify({
        'name': 'AI-Driven Notification Spam Detector API',
        'version': '1.0',
        'status': 'running',
        'endpoints': {
            '/': 'API information',
            '/analyze': 'POST - Analyze notification message',
            '/health': 'GET - Health check'
        }
    })

@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'model_loaded': os.path.exists(os.path.join(model_folder, 'spam_model.pkl'))
    })

@app.route('/analyze', methods=['POST'])
def analyze():
    """Analyze notification message"""
    try:
        # Get request data
        data = request.get_json()
        
        print("\n" + "="*60)
        print("📨 NEW NOTIFICATION RECEIVED")
        print("="*60)
        
        if not data or 'message' not in data:
            print("❌ ERROR: Missing 'message' field")
            return jsonify({
                'success': False,
                'error': 'Missing required field: message'
            }), 400
        
        message = data.get('message', '')
        app_name = data.get('app_name', 'Unknown')
        title = data.get('title', '')
        package_name = data.get('package_name', '')
        
        print(f"📱 App: {app_name}")
        print(f"📦 Package: {package_name}")
        print(f"📝 Title: {title}")
        print(f"💬 Message: {message}")
        
        # Combine title and message for analysis
        full_text = f"{title} {message}".strip()
        
        if not full_text:
            print("❌ ERROR: Empty message")
            return jsonify({
                'success': False,
                'error': 'Message cannot be empty'
            }), 400
        
        print(f"\n🔍 Analyzing: {full_text[:100]}...")
        
        # Predict
        result = detector.predict(full_text)
        
        print(f"\n📊 ANALYSIS RESULT:")
        print(f"   Prediction: {result['prediction']}")
        print(f"   Risk Level: {result['risk_level']}")
        print(f"   Risk Score: {result['risk_score']:.2f}")
        print(f"   Confidence: {result['confidence']:.2f}")
        print(f"   Issues: {', '.join(result['detected_issues']) if result['detected_issues'] else 'None'}")
        print("="*60 + "\n")
        
        # Prepare response
        response = {
            'success': True,
            'prediction': result['prediction'],
            'confidence': result['confidence'],
            'risk_score': result['risk_score'],
            'risk_level': result['risk_level'],
            'detected_issues': result['detected_issues'],
            'explanation': result['explanation'],
            'app_name': app_name,
            'package_name': package_name
        }
        
        return jsonify(response)
    
    except Exception as e:
        print(f"❌ ERROR: {str(e)}")
        print("="*60 + "\n")
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

@app.route('/batch_analyze', methods=['POST'])
def batch_analyze():
    """Analyze multiple messages at once"""
    try:
        data = request.get_json()
        
        if not data or 'messages' not in data:
            return jsonify({
                'success': False,
                'error': 'Missing required field: messages'
            }), 400
        
        messages = data.get('messages', [])
        results = []
        
        for msg in messages:
            result = detector.predict(msg)
            results.append(result)
        
        return jsonify({
            'success': True,
            'count': len(results),
            'results': results
        })
    
    except Exception as e:
        return jsonify({
            'success': False,
            'error': str(e)
        }), 500

if __name__ == '__main__':
    print("\n" + "="*60)
    print("AI-Driven Notification Spam Detector API")
    print("="*60)
    print("Server starting on http://localhost:5001")
    print("="*60 + "\n")
    
    app.run(host='0.0.0.0', port=5001, debug=True)
