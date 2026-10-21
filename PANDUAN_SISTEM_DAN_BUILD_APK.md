# PANDUAN LENGKAP SISTEM SMARTSTEP & PEMBUATAN APK ANDROID

Aplikasi: **SMARTSTEP**  
Proyek Mahasiswa: **Teknik Elektro**  
Pengembang:
1. **Bunga Gloria Paembonan**
2. **Tasiah**

---

## 1. DESKRIPSI SISTEM & ARSITEKTUR

SMARTSTEP adalah sistem pemantauan (monitoring) aktivitas langkah kaki berbasis IoT (Internet of Things) dan Computer Vision yang menampilkan:
- Jumlah langkah harian (informasi utama).
- Target langkah harian dan sisa langkah yang harus ditempuh.
- Jarak tempuh (dihitung berdasarkan rata-rata panjang langkah 0.75 meter).
- Durasi aktivitas (jam, menit, detik).
- Riwayat aktivitas langkah harian dengan filter tanggal.
- Status koneksi perangkat ESP32, Firebase, dan modul Computer Vision.

> **CATATAN PENTING ARSITEKTUR:**  
> Sesuai ketentuan proyek, sistem SMARTSTEP **TIDAK MEMILIKI FITUR KALORI** pada bagian mana pun (tidak ada nilai kalori, kartu kalori, maupun grafik kalori).

### Alur Kerja Sistem (Data Pipeline):
```
[Sensor MPU6050 / Kaki] 
         │ (I2C)
         ▼
[Mikrokontroler ESP32] ──(Wi-Fi JSON)──> [Firebase / Cloud API] ──> [Aplikasi SMARTSTEP (Android / Web)]
                                                   ▲
[Webcam Laptop] ──> [Python MediaPipe Pose] ───────┘ (FastAPI HTTP Endpoint)
```

---

## 2. STRUKTUR DIREKTORI PROYEK

```
├── app/
│   ├── build.gradle.kts           # Konfigurasi dependensi Android & build APK
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml # Izin internet, network, dan cleartext traffic
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt # Entry point, 4 Tab Menu & Navigasi
│       │   │   ├── data/
│       │   │   │   ├── model/StepModels.kt       # Model data langkah tanpa kalori
│       │   │   │   ├── local/StepDao.kt          # Room Database DAO
│       │   │   │   ├── local/AppDatabase.kt      # Database Room SQLite
│       │   │   │   └── repository/StepRepository.kt # Repository sinkronisasi IoT & CV
│       │   │   └── ui/
│       │   │       ├── StepViewModel.kt          # State management UI & Filter
│       │   │       ├── theme/Color.kt & Theme.kt # Desain pink pastel lembut & modern
│       │   │       ├── components/CommonComponents.kt # Header, chart, progress bar
│       │   │       └── screens/
│       │   │           ├── HomeScreen.kt         # Halaman Beranda
│       │   │           ├── MonitoringScreen.kt   # Halaman Monitoring & CV
│       │   │           ├── HistoryScreen.kt      # Halaman Riwayat & Filter
│       │   │           └── ProfileScreen.kt      # Halaman Profil & Penulis
│       │   └── res/                              # Ikon adaptif custom SMARTSTEP
├── computer_vision/
│   ├── cv_step_tracker.py         # Skrip deteksi pose MediaPipe & server FastAPI
│   └── requirements.txt           # Library Python (OpenCV, MediaPipe, FastAPI)
├── esp32/
│   └── smartstep_esp32.ino        # Kode program Arduino ESP32 & pembacaan sensor
├── metadata.json                  # Metadata platform Google AI Studio
└── PANDUAN_SISTEM_DAN_BUILD_APK.md # Panduan instalasi dan pengujian
```

---

## 3. CARA MENJALANKAN APLIKASI PADA BROWSER / EMULATOR

1. Di Google AI Studio, aplikasi dijalankan langsung pada panel **Streaming Emulator** di samping kanan browser.
2. Emulator akan menampilkan tampilan mobile Android secara langsung.
3. Aplikasi mendukung dua mode operasi:
   - **Mode DEMO (Default):** Mengaktifkan simulasi pengujian dengan tombol `+10`, `+50`, dan `Auto Sim` di Beranda sehingga dosen penguji atau pengguna dapat langsung mencoba antarmuka tanpa perlu perangkat keras terpasang.
   - **Mode LIVE:** Mematikan data simulasi dan hanya menampilkan data asli dari sensor ESP32 / Firebase. Jika sensor belum terhubung, status ditampilkan secara jujur sebagai `Belum Terhubung`.

---

## 4. CARA MENJALANKAN MODUL COMPUTER VISION (PYTHON)

Modul Computer Vision berfungsi menghitung langkah kaki secara visual menggunakan kamera/webcam laptop:

1. **Instalasi Python:**
   Pastikan laptop memiliki Python versi 3.9 s/d 3.11.
2. **Instal Dependensi:**
   Buka terminal di dalam folder `computer_vision/` lalu jalankan:
   ```bash
   pip install -r requirements.txt
   ```
3. **Jalankan Program:**
   ```bash
   python cv_step_tracker.py
   ```
4. **Cara Kerja:**
   - Program akan membuka kamera webcam dan menampilkan jendela deteksi.
   - MediaPipe Pose mendeteksi sendi pinggul, lutut, dan pergelangan kaki.
   - Saat kaki ditekuk/diangkat lalu menapak kembali, hitungan langkah bertambah secara otomatis.
   - Server FastAPI berjalan otomatis di latar belakang pada port `8000`.
5. **Koneksi ke Aplikasi SMARTSTEP:**
   - Cek alamat IP lokal laptop Anda (misal `192.168.1.100` via `ipconfig` di Windows atau `ifconfig` di Linux/Mac).
   - Buka menu **Monitoring** pada aplikasi SMARTSTEP.
   - Masukkan alamat: `http://<IP_LAPTOP_ANDA>:8000` (contoh: `http://192.168.1.100:8000`).
   - Tekan tombol **Uji Koneksi CV**.
   - Aplikasi akan tersambung dan menampilkan jumlah langkah hasil deteksi kamera.

---

## 5. CARA MENYIAPKAN DAN MENGUNGGAH KODE ESP32

1. Buka file `esp32/smartstep_esp32.ino` pada **Arduino IDE**.
2. Pasang library yang diperlukan melalui *Tools > Manage Libraries*:
   - `ArduinoJson` (oleh Benoit Blanchon)
3. Sambungkan pin sensor MPU6050 ke ESP32:
   - `VCC` -> `3.3V` ESP32
   - `GND` -> `GND` ESP32
   - `SDA` -> `GPIO 21` ESP32
   - `SCL` -> `GPIO 22` ESP32
4. Sesuaikan nama Wi-Fi dan password pada baris:
   ```cpp
   const char* WIFI_SSID     = "NAMA_WIFI";
   const char* WIFI_PASSWORD = "PASSWORD_WIFI";
   ```
5. Pilih board: **ESP32 Dev Module**, pilih Port COM yang sesuai, lalu klik **Upload**.
6. Buka Serial Monitor pada baud rate `115200` untuk memverifikasi langkah kaki yang terdeteksi saat sensor digerakkan.

---

## 6. CARA MEMBANGUN APK ANDROID MENGGUNAKAN ANDROID STUDIO

Untuk mengubah proyek ini menjadi file instalasi APK mandiri di HP Android fisik:

### Langkah A: Export / Download Proyek
1. Pada Google AI Studio, klik ikon **Settings** atau menu opsi di kanan atas.
2. Pilih **Download Project as ZIP** (atau clone repository Git jika sudah dihubungkan ke GitHub).
3. Ekstrak file ZIP ke komputer Anda.

### Langkah B: Buka di Android Studio
1. Buka aplikasi **Android Studio** (disarankan versi Ladybug / Koala atau lebih baru).
2. Pilih **Open**, arahkan ke folder hasil ekstrak proyek.
3. Tunggu hingga proses **Gradle Sync** selesai mengunduh semua library (memerlukan koneksi internet).

### Langkah C: Build APK
1. Di menu atas Android Studio, pilih:
   `Build` > `Build App Bundle(s) / APK(s)` > `Build APK(s)`.
2. Atau melalui Terminal Android Studio jalankan:
   ```bash
   gradle assembleDebug
   ```
3. Setelah proses build selesai, klik notifikasi **locate** pada sudut kanan bawah.
4. File APK dapat ditemukan di:
   `app/build/outputs/apk/debug/app-debug.apk`
5. Kirim file `app-debug.apk` tersebut ke smartphone Android Anda (via WhatsApp, kabel data USB, atau Google Drive) dan pasang (install) di HP.

---

## 7. PENGUJIAN FITUR (CHECKLIST HASIL)

| Fitur | Status | Keterangan |
| :--- | :--- | :--- |
| **Menu Beranda** | Berfungsi Penuh | Menampilkan langkah, target bar, jarak tempuh, durasi, status badge, dan grafik mingguan. |
| **Menu Monitoring** | Berfungsi Penuh | Menampilkan metrik real-time, sisa langkah, form target harian, status koneksi, dan konfigurasi CV. |
| **Menu Riwayat** | Berfungsi Penuh | Menampilkan daftar aktivitas dengan filter tanggal dan empty state informatif jika kosong. |
| **Menu Profil** | Berfungsi Penuh | Menampilkan identitas Bunga Gloria Paembonan & Tasiah, spesifikasi teknis, dan petunjuk sistem. |
| **Ketiadaan Kalori** | Terpenuhi 100% | Bebas dari seluruh variabel, rumus, kartu, teks, maupun kalkulasi kalori. |
| **Mode DEMO / LIVE** | Berfungsi Penuh | Data simulasi diberi label eksplisit; status tidak mengklaim terhubung jika belum diuji. |
| **Firmware ESP32** | Tersedia | Kode lengkap di `esp32/smartstep_esp32.ino`. |
| **Modul Computer Vision** | Tersedia | Kode lengkap di `computer_vision/cv_step_tracker.py`. |
| **Ikon Aplikasi Custom** | Dikonfigurasi | Ikon adaptif modern pink pastel `ic_smartstep_logo`. |
