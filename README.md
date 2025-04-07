## 📖 Dokumentasi Library PlayVideo

### 🌐 Deskripsi Singkat
`PlayVideo` adalah library Android berbasis `FrameLayout` yang menyederhanakan proses pemutaran video dengan fitur tambahan seperti thumbnail, download video, indikator progres download berbentuk lingkaran (Canvas), serta ukuran file dari URL.

---

### ✨ Fitur Utama
- **Pemutaran Video** dengan `VideoView`
- **Thumbnail Otomatis** dari video URL
- **Download Video** dengan validasi file sudah ada
- **Penyimpanan Otomatis** ke folder `/Android/media/id.chaerul.library.video`
- **Indikator Progres Download** dari 0% sampai 100% berbentuk lingkaran (menggunakan `Canvas`)
- **SeekBar Interaktif** untuk mengatur posisi video
- **Ukuran File Otomatis** dengan satuan KB, MB, dan GB
- **Support Callback**: `onSuccess()` & `onError()`
- **Atribut Kustom**: `src` dan `srcError` dari XML

---

### ⚡ Cara Pakai

#### 1. Tambahkan ke Gradle
```groovy
implementation 'id.chaerul.library:playvideo:1.0.0'
```

#### 2. Gunakan di XML
```xml
<id.chaerul.library.playvideo.PlayVideo
    android:id="@+id/play_video"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:src="@raw/sample_video"
    app:srcError="@drawable/ic_error" />
```

#### 3. Konfigurasi di Activity
```java
playVideo.setVideoDownload("https://example.com/video.mp4", "video_local.mp4", new PlayVideo.OnCallback() {
    @Override
    public void onSuccess(String url, String message) {
        Log.d("PlayVideo", message);
    }

    @Override
    public void onError(String message) {
        Log.e("PlayVideo", message);
    }
});
```

---

### 🔍 Lokasi Penyimpanan
Video akan disimpan ke:
```
/sdcard/Android/media/id.chaerul.library.video/
```

---

### 📊 Callback Interface
```java
public interface OnCallback {
    void onSuccess(String url, String message);
    void onError(String message);
}
```

---

### ⚖ Versi dan Minimum SDK
- **minSdk**: 26
- **targetSdk**: 35
- **Java**: Versi 11

---

### 🎓 Pengembang
**Chaerul**
> Mahasiswa yang harus bisa ngoding ✨💻

---

### ✉ Catatan Tambahan
- Library ini cocok untuk aplikasi video streaming, edukasi, atau pemutar konten offline.
- Indikator progres dibuat dari `Canvas` tanpa mengubah bentuk asli `ImageView`
- Ukuran file otomatis dihitung dari URL menggunakan `HttpURLConnection`

---

### 🚀 Siap Dirilis!
Versi 1.0.0 siap digunakan untuk production dan pengembangan. Jangan lupa kasih feedback dan bintang di GitHub ✨

---

