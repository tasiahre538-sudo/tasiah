"""
SMARTSTEP - Modul Computer Vision & Backend API
Proyek Mahasiswa Teknik Elektro:
1. Bunga Gloria Paembonan
2. Tasiah

Deskripsi:
Program Python untuk mendeteksi pose dan gerakan kaki menggunakan OpenCV & MediaPipe Pose.
Dilengkapi server FastAPI untuk menyediakan endpoint data langkah ke aplikasi SMARTSTEP.

Endpoint API:
- GET  /api/status : Memeriksa status kesiapan kamera dan model pose
- GET  /api/steps  : Mengambil data jumlah langkah terbaru dan jarak tempuh
- POST /api/reset  : Mengatur ulang (reset) hitungan langkah menjadi 0
"""

import sys
import time
import math
import threading
from typing import Optional

try:
    import cv2
    import mediapipe as mp
except ImportError:
    print("Peringatan: opencv-python dan mediapipe belum terinstal.")
    print("Jalankan: pip install -r requirements.txt")

try:
    from fastapi import FastAPI
    from fastapi.middleware.cors import CORSMiddleware
    import uvicorn
except ImportError:
    print("Peringatan: fastapi dan uvicorn belum terinstal.")
    print("Jalankan: pip install -r requirements.txt")

# State Bersama (Thread-Safe)
step_state = {
    "steps": 0,
    "distanceMeters": 0.0,
    "camera_active": False,
    "status": "Inisialisasi...",
    "last_knee_y_left": 0.0,
    "last_knee_y_right": 0.0,
    "leg_phase": "DOWN", # "UP" or "DOWN"
    "updatedAt": time.strftime("%Y-%m-%d %H:%M:%S")
}
state_lock = threading.Lock()

# Inisialisasi FastAPI
app = FastAPI(
    title="SMARTSTEP Computer Vision API",
    description="Backend API penghubung Computer Vision langkah kaki ke aplikasi SMARTSTEP",
    version="1.0.0"
)

# Aktifkan CORS agar frontend web / Android dapat mengakses
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/api/status")
def get_status():
    """Endpoint untuk memeriksa kesiapan modul Computer Vision."""
    with state_lock:
        return {
            "status": "Online" if step_state["camera_active"] else "Kamera Offline / Menunggu",
            "camera_active": step_state["camera_active"],
            "detail": step_state["status"],
            "authors": ["Bunga Gloria Paembonan", "Tasiah"],
            "updatedAt": step_state["updatedAt"]
        }

@app.get("/api/steps")
def get_steps():
    """
    Endpoint utama yang dibaca aplikasi SMARTSTEP.
    Mengirimkan jumlah langkah, jarak tempuh, durasi, dan status perangkat (TIDAK ADA KALORI).
    """
    with state_lock:
        return {
            "steps": step_state["steps"],
            "distanceMeters": round(step_state["steps"] * 0.75, 2), # 1 langkah ~ 0.75 m
            "camera_active": step_state["camera_active"],
            "status": step_state["status"],
            "updatedAt": step_state["updatedAt"]
        }

@app.post("/api/reset")
def reset_steps():
    """Mengatur ulang jumlah langkah kembali ke 0."""
    with state_lock:
        step_state["steps"] = 0
        step_state["distanceMeters"] = 0.0
        step_state["updatedAt"] = time.strftime("%Y-%m-%d %H:%M:%S")
        return {"message": "Hitungan langkah berhasil di-reset", "steps": 0}


def calculate_angle(a, b, c):
    """Menghitung sudut 3 titik sendi (misal: Pinggul - Lutut - Pergelangan Kaki)"""
    radians = math.atan2(c[1] - b[1], c[0] - b[0]) - math.atan2(a[1] - b[1], a[0] - b[0])
    angle = abs(radians * 180.0 / math.pi)
    if angle > 180.0:
        angle = 360 - angle
    return angle


def run_computer_vision():
    """
    Thread utama Computer Vision:
    Membuka webcam, mendeteksi pose kaki via MediaPipe,
    dan menghitung langkah berdasarkan perubahan sudut & elevasi lutut.
    """
    print("=" * 60)
    print("SMARTSTEP - Memulai Modul Computer Vision...")
    print("Pengembang: Bunga Gloria Paembonan & Tasiah (Teknik Elektro)")
    print("=" * 60)

    # Inisialisasi MediaPipe Pose
    mp_pose = mp.solutions.pose
    mp_drawing = mp.solutions.drawing_utils
    pose = mp_pose.Pose(
        min_detection_confidence=0.6,
        min_tracking_confidence=0.6
    )

    # Inisialisasi Kamera (Webcam default index 0)
    cap = cv2.VideoCapture(0)

    if not cap.isOpened():
        print("[ERROR] Kamera webcam tidak dapat diakses atau tidak ditemukan!")
        with state_lock:
            step_state["camera_active"] = False
            step_state["status"] = "Kamera tidak tersedia"
        return

    with state_lock:
        step_state["camera_active"] = True
        step_state["status"] = "Kamera Aktif & Mendeteksi Gerakan"

    # State mesin penghitung langkah
    leg_state = "DOWN"
    knee_threshold_angle = 150.0  # Sudut lutut saat terangkat
    min_step_interval = 0.35      # Debounce minimal 350ms antar langkah
    last_step_time = 0

    try:
        while cap.isOpened():
            ret, frame = cap.read()
            if not ret:
                print("[WARNING] Tidak dapat membaca frame dari kamera.")
                with state_lock:
                    step_state["camera_active"] = False
                    step_state["status"] = "Gagal membaca frame kamera"
                break

            # Konversi BGR ke RGB untuk MediaPipe
            image_rgb = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            image_rgb.flags.writeable = False
            results = pose.process(image_rgb)
            image_rgb.flags.writeable = True

            h, w, _ = frame.shape

            if results.pose_landmarks:
                landmarks = results.pose_landmarks.landmark

                # Koordinat sendi kaki kiri
                left_hip = [landmarks[mp_pose.PoseLandmark.LEFT_HIP.value].x * w,
                            landmarks[mp_pose.PoseLandmark.LEFT_HIP.value].y * h]
                left_knee = [landmarks[mp_pose.PoseLandmark.LEFT_KNEE.value].x * w,
                             landmarks[mp_pose.PoseLandmark.LEFT_KNEE.value].y * h]
                left_ankle = [landmarks[mp_pose.PoseLandmark.LEFT_ANKLE.value].x * w,
                              landmarks[mp_pose.PoseLandmark.LEFT_ANKLE.value].y * h]

                # Koordinat sendi kaki kanan
                right_hip = [landmarks[mp_pose.PoseLandmark.RIGHT_HIP.value].x * w,
                             landmarks[mp_pose.PoseLandmark.RIGHT_HIP.value].y * h]
                right_knee = [landmarks[mp_pose.PoseLandmark.RIGHT_KNEE.value].x * w,
                              landmarks[mp_pose.PoseLandmark.RIGHT_KNEE.value].y * h]
                right_ankle = [landmarks[mp_pose.PoseLandmark.RIGHT_ANKLE.value].x * w,
                               landmarks[mp_pose.PoseLandmark.RIGHT_ANKLE.value].y * h]

                # Hitung sudut lutut kiri & kanan
                left_angle = calculate_angle(left_hip, left_knee, left_ankle)
                right_angle = calculate_angle(right_hip, right_knee, right_ankle)

                now = time.time()

                # Deteksi fase langkah (Kaki diangkat -> ditekuk -> menapak kembali)
                is_leg_lifted = (left_angle < knee_threshold_angle) or (right_angle < knee_threshold_angle)

                if is_leg_lifted and leg_state == "DOWN":
                    leg_state = "UP"
                elif not is_leg_lifted and leg_state == "UP":
                    # Kaki kembali menapak -> catat sebagai 1 langkah
                    if (now - last_step_time) > min_step_interval:
                        last_step_time = now
                        with state_lock:
                            step_state["steps"] += 1
                            step_state["distanceMeters"] = round(step_state["steps"] * 0.75, 2)
                            step_state["updatedAt"] = time.strftime("%Y-%m-%d %H:%M:%S")
                            step_state["status"] = f"Langkah Terdeteksi: {step_state['steps']}"
                        print(f"[SMARTSTEP CV] Langkah terdeteksi! Total: {step_state['steps']}")
                    leg_state = "DOWN"

                # Gambar skeleton pose pada tampilan OpenCV
                mp_drawing.draw_landmarks(
                    frame,
                    results.pose_landmarks,
                    mp_pose.POSE_CONNECTIONS,
                    mp_drawing.DrawingSpec(color=(245, 117, 66), thickness=2, circle_radius=2),
                    mp_drawing.DrawingSpec(color=(245, 66, 230), thickness=2, circle_radius=2)
                )

            # Tampilkan overlay teks info
            with state_lock:
                current_steps = step_state["steps"]
                cam_status = step_state["status"]

            cv2.rectangle(frame, (10, 10), (380, 100), (255, 255, 255), -1)
            cv2.putText(frame, "SMARTSTEP - CV Step Tracker", (20, 35),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.65, (233, 30, 99), 2)
            cv2.putText(frame, f"Langkah: {current_steps} (Jarak: {current_steps * 0.75:.1f} m)", (20, 65),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.6, (26, 28, 30), 2)
            cv2.putText(frame, f"Status: {cam_status}", (20, 90),
                        cv2.FONT_HERSHEY_SIMPLEX, 0.45, (100, 100, 100), 1)

            cv2.imshow("SMARTSTEP - Computer Vision (Tekan 'q' untuk keluar)", frame)

            if cv2.waitKey(10) & 0xFF == ord('q'):
                print("Menghentikan modul Computer Vision...")
                break

    except Exception as e:
        print(f"[ERROR] Terjadi kesalahan pada loop CV: {e}")
        with state_lock:
            step_state["status"] = f"Error: {e}"
    finally:
        cap.release()
        cv2.destroyAllWindows()
        with state_lock:
            step_state["camera_active"] = False
            step_state["status"] = "Kamera Dinonaktifkan"


def start_server():
    """Menjalankan server FastAPI pada port 8000 di background."""
    uvicorn.run(app, host="0.0.0.0", port=8000, log_level="warning")


if __name__ == "__main__":
    # Jalankan server FastAPI pada thread terpisah
    server_thread = threading.Thread(target=start_server, daemon=True)
    server_thread.start()
    print("[INFO] Server FastAPI berjalan di http://0.0.0.0:8000")
    print("[INFO] Endpoint: http://<IP_LAPTOP>:8000/api/steps")

    # Jalankan loop Computer Vision di thread utama
    run_computer_vision()
