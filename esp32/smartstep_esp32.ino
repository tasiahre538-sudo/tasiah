/*
 * SMARTSTEP - Firmware ESP32 Monitoring Langkah Kaki (IoT)
 * Proyek Mahasiswa Teknik Elektro:
 * 1. Bunga Gloria Paembonan
 * 2. Tasiah
 *
 * Alur Sistem:
 * SENSOR (MPU6050 / Pedometer) -> ESP32 -> Wi-Fi -> FIREBASE / API -> APLIKASI SMARTSTEP
 *
 * Struktur Data Payload (TIDAK MENGGUNAKAN KALORI):
 * {
 *   "steps": 1420,
 *   "targetSteps": 6000,
 *   "distanceMeters": 1065.0,
 *   "durationSeconds": 920,
 *   "deviceStatus": "CONNECTED",
 *   "updatedAt": "2026-10-21 14:30:00"
 * }
 */

#include <WiFi.h>
#include <HTTPClient.h>
#include <Wire.h>
#include <ArduinoJson.h>

// ================= KONFIGURASI WI-FI =================
const char* WIFI_SSID     = "NAMA_WIFI_ANDA";
const char* WIFI_PASSWORD = "PASSWORD_WIFI_ANDA";

// ================= KONFIGURASI ENDPOINT =================
// Masukkan URL Firebase Realtime Database atau REST API Server Anda
// Contoh Firebase: https://PROJECT_ID-default-rtdb.firebaseio.com/smartstep/live.json
// Contoh Server Lokal: http://192.168.1.100:8000/api/esp32_steps
const char* API_ENDPOINT  = "https://smartstep-iot-default-rtdb.firebaseio.com/smartstep/live.json";

// ================= KONFIGURASI SENSOR (MPU6050 I2C) =================
const int MPU_ADDR = 0x68; // Alamat I2C MPU6050
int16_t AcX, AcY, AcZ;

// Variabel Algoritma Deteksi Langkah
float prevMagnitude = 0;
float thresholdHigh = 14.5; // Ambang batas percepatan puncak langkah (m/s^2)
float thresholdLow  = 10.0;
bool isStepPeak = false;
unsigned long lastStepTime = 0;
const unsigned long stepDebounceMs = 300; // Minimal interval 300ms antar langkah

// Variabel Monitoring Langkah
int stepCount = 0;
int dailyTarget = 6000;
float distanceMeters = 0.0;
unsigned long sessionStartMillis = 0;
unsigned long lastSendMillis = 0;
const unsigned long sendIntervalMs = 3000; // Kirim data setiap 3 detik

void initMPU6050() {
  Wire.begin(21, 22); // SDA = GPIO 21, SCL = GPIO 22 pada ESP32 DevKit V1
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(0x6B); // Power Management 1 register
  Wire.write(0);    // Bangunkan MPU6050 (sleep mode = 0)
  Wire.endTransmission(true);
  Serial.println("[MPU6050] Sensor berhasil diinisialisasi.");
}

void readSensorAndDetectStep() {
  Wire.beginTransmission(MPU_ADDR);
  Wire.write(0x3B); // Register data akselerometer
  Wire.endTransmission(false);
  Wire.requestFrom(MPU_ADDR, 6, true);

  if (Wire.available() >= 6) {
    AcX = Wire.read() << 8 | Wire.read();
    AcY = Wire.read() << 8 | Wire.read();
    AcZ = Wire.read() << 8 | Wire.read();

    // Konversi nilai raw ke m/s^2 (skala default +/- 2g: 1g = 16384 LSB, 1g = 9.8 m/s^2)
    float ax = (AcX / 16384.0) * 9.8;
    float ay = (AcY / 16384.0) * 9.8;
    float az = (AcZ / 16384.0) * 9.8;

    // Magnitudo vektor percepatan total
    float magnitude = sqrt(ax * ax + ay * ay + az * az);
    unsigned long now = millis();

    // Deteksi Puncak Langkah (Peak Detection)
    if (magnitude > thresholdHigh && !isStepPeak && (now - lastStepTime > stepDebounceMs)) {
      isStepPeak = true;
    }
    else if (magnitude < thresholdLow && isStepPeak) {
      isStepPeak = false;
      stepCount++;
      lastStepTime = now;

      // Rumus: 1 langkah rata-rata = 0.75 meter
      distanceMeters = stepCount * 0.75;

      Serial.print("[STEP DETECTED] Total Langkah: ");
      Serial.print(stepCount);
      Serial.print(" | Jarak: ");
      Serial.print(distanceMeters);
      Serial.println(" m");
    }

    prevMagnitude = magnitude;
  }
}

void sendDataToCloud() {
  if (WiFi.status() != WL_CONNECTED) {
    Serial.println("[Wi-Fi] Terputus. Menghubungkan ulang...");
    WiFi.reconnect();
    return;
  }

  HTTPClient http;
  http.begin(API_ENDPOINT);
  http.addHeader("Content-Type", "application/json");

  unsigned long durationSec = (millis() - sessionStartMillis) / 1000;

  // Format Dokumen JSON
  StaticJsonDocument<256> doc;
  doc["steps"] = stepCount;
  doc["targetSteps"] = dailyTarget;
  doc["distanceMeters"] = distanceMeters;
  doc["durationSeconds"] = durationSec;
  doc["deviceStatus"] = "CONNECTED";
  doc["updatedAt"] = millis() / 1000;

  String jsonString;
  serializeJson(doc, jsonString);

  // Kirim data via HTTP PUT / POST
  int httpResponseCode = http.PUT(jsonString);

  if (httpResponseCode > 0) {
    Serial.print("[HTTP SUCCESS] Respon: ");
    Serial.println(httpResponseCode);
  } else {
    Serial.print("[HTTP ERROR] Kode: ");
    Serial.println(httpResponseCode);
  }

  http.end();
}

void setup() {
  Serial.begin(115200);
  delay(1000);

  Serial.println("\n==============================================");
  Serial.println("SMARTSTEP - ESP32 Footstep IoT Monitor");
  Serial.println("Pengembang: Bunga Gloria Paembonan & Tasiah");
  Serial.println("Teknik Elektro");
  Serial.println("==============================================");

  // Inisialisasi Sensor
  initMPU6050();

  // Koneksi Wi-Fi
  Serial.print("[Wi-Fi] Menghubungkan ke: ");
  Serial.println(WIFI_SSID);
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);

  int retry = 0;
  while (WiFi.status() != WL_CONNECTED && retry < 20) {
    delay(500);
    Serial.print(".");
    retry++;
  }

  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n[Wi-Fi] Terhubung! Alamat IP: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\n[Wi-Fi] Belum terhubung. Berjalan dalam mode lokal.");
  }

  sessionStartMillis = millis();
}

void loop() {
  // 1. Baca data sensor akselerometer & hitung langkah kaki
  readSensorAndDetectStep();

  // 2. Kirim data ke Cloud / Firebase setiap periode waktu tertentu
  if (millis() - lastSendMillis > sendIntervalMs) {
    lastSendMillis = millis();
    sendDataToCloud();
  }

  delay(20); // Sampling rate ~50Hz
}
