from datetime import datetime
from app import db

class TrafficData(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    ip_address = db.Column(db.String(45), nullable=False)
    user_agent = db.Column(db.String(255), nullable=False)
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)
    session_id = db.Column(db.String(100), nullable=False)
    
    # 地理位置信息
    geo_location = db.Column(db.String(100))
    is_proxy = db.Column(db.Boolean, default=False)
    
    # 行为数据
    click_timestamp = db.Column(db.DateTime)
    mouse_movements = db.Column(db.Integer, default=0)
    page_scrolls = db.Column(db.Integer, default=0)
    time_on_page = db.Column(db.Integer, default=0)  # 秒
    interaction_depth = db.Column(db.Integer, default=0)
    conversion = db.Column(db.Boolean, default=False)
    
    # 设备信息
    device_fingerprint = db.Column(db.String(100))
    browser_fingerprint = db.Column(db.String(100))
    
    # 评分结果
    total_score = db.Column(db.Float, default=0)
    is_suspicious = db.Column(db.Boolean, default=False)
    is_fake = db.Column(db.Boolean, default=False)
    
    def __repr__(self):
        return f'<TrafficData {self.ip_address}>'

class MaliciousIP(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    ip_address = db.Column(db.String(45), unique=True, nullable=False)
    source = db.Column(db.String(50))  # IP来源（例如：Google恶意IP列表）
    added_date = db.Column(db.DateTime, default=datetime.utcnow)
    
    def __repr__(self):
        return f'<MaliciousIP {self.ip_address}>' 