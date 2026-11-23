# Coroutine Demo & Task Manager (Modul M17)

Aplikasi Android berbasis **Jetpack Compose** dan arsitektur **MVVM** yang mengimplementasikan pemrograman asinkron menggunakan **Kotlin Coroutines**, mencakup seluruh tugas pada **Bagian 9 (Latihan)** serta pengembangan fitur tambahan[cite: 1].

---

## Fitur Utama & Pengembangan

* **Concurrent Data Fetching (`async` & `coroutineScope`):** Memuat daftar tugas (`2000 ms`), pengumuman (`1000 ms`), dan jumlah mata kuliah (`1500 ms`) secara bersamaan di `Dispatchers.IO`[cite: 1].
* **Realtime Execution Timer:** Mengukur total durasi pemuatan konkuren menggunakan `measureTimeMillis` (~2000 ms)[cite: 1].
* **Error Handling & Retry:** Menangani kegagalan jaringan (`IOException`) dan menyediakan tombol **Retry** saat terjadi error[cite: 1].
* **Safe Job Cancellation:** Membatalkan proses pemuatan yang sedang berjalan menggunakan `Job.cancel()` tanpa tertangkap sebagai error[cite: 1].
* **Background Sorting (`Dispatchers.Default`):** Mengurutkan daftar tugas secara alfabetis (A–Z / Z–A) di latar belakang menggunakan `withContext(Dispatchers.Default)`[cite: 1].
* **Interactive UI & Search:** Kolom pencarian tugas/mata kuliah serta fitur centang (*checkbox*) untuk menandai tugas selesai.
* **UI Responsiveness Test:** Tombol **Tap me** untuk menguji kelancaran *main thread* selama proses asinkron dan tombol **Block UI** (`Thread.sleep(3000L)`) sebagai pembanding[cite: 1].

---

## Jawaban & Pembahasan Latihan (Bagian 9)

### 1. Menambah Permintaan ke-3 (`getCourseCount()`)
* **Implementasi:** Menambahkan `suspend fun getCourseCount(): Int` dengan `delay(1500L)` di `TaskRepository` dan memuatnya melalui `async` ketiga di `TasksViewModel.loadData()`[cite: 1].
* **Apakah total waktu pemuatan masih sekitar 2 detik?**
  **Ya**, total waktu pemuatan tetap sekitar **2 detik (2000 ms)**[cite: 1].
* **Alasan:** Karena ketiga fungsi (`getTasks()` 2000 ms, `getAnnouncement()` 1000 ms, dan `getCourseCount()` 1500 ms) dijalankan secara **konkuren (bersamaan)** menggunakan `async` di dalam `coroutineScope`[cite: 1]. Waktu tunggu total ditentukan oleh pekerjaan terlama (`2000 ms`), bukan penjumlahan waktu ketiganya[cite: 1].

### 2. Tombol Retry
* **Implementasi:** Menampilkan tombol **Retry** tepat di sebelah pesan error pada `TasksScreen` ketika `state.errorMessage != null`[cite: 1]. Menekan tombol ini akan memanggil kembali `viewModel.loadData()`[cite: 1].

### 3. Menangkap Terlalu Banyak (`catch (e: Exception)`)
* **Pesan yang muncul:** Saat menekan **Load** lalu segera menekan **Cancel**, pesan yang muncul berubah menjadi **`Failed to load: StandaloneCoroutine was cancelled`** (atau *Job was cancelled*), bukan `"Loading cancelled"`[cite: 1].
* **Alasan:** Pembatalan coroutine melemparkan `CancellationException` pada titik suspensi (`delay()`)[cite: 1]. Karena `CancellationException` merupakan turunan dari `Exception`, blok `catch (e: Exception)` ikut menangkapnya dan menganggap pembatalan tersebut sebagai kegagalan[cite: 1]. Oleh karena itu, tangkap exception spesifik (`IOException`) atau lempar ulang `CancellationException` (`throw e`)[cite: 1].

### 4. Di Mana Macetnya? (`Thread.sleep(2000L)`)
* **Di dalam `withContext(Dispatchers.IO)`:** Layar **tidak macet**[cite: 1]. `Thread.sleep(2000L)` hanya memblokir *worker thread* milik `Dispatchers.IO`, sementara *main thread* tetap bebas merespons input pengguna[cite: 1].
* **Setelah `withContext` dihapus:** Layar **menjadi macet (freeze)** selama 2 detik[cite: 1]. Tanpa `withContext`, `getTasks()` berjalan di konteks `viewModelScope` (`Dispatchers.Main`), sehingga `Thread.sleep(2000L)` memblokir *main thread* dan menghentikan penggambaran UI[cite: 1].

### 5. Pertanyaan Konsep
* **(a) Perbedaan `launch` dan `async`:** `launch` menjalankan coroutine tanpa mengembalikan nilai (*fire-and-forget*) dan menghasilkan `Job` untuk mengontrol siklus hidupnya[cite: 1]. Sebaliknya, `async` mengembalikan nilai hasil di masa depan berupa `Deferred<T>` yang diambil menggunakan `.await()`[cite: 1].
* **(b) Kenapa `GlobalScope` sebaiknya dihindari:** `GlobalScope` hidup selama aplikasi berjalan dan tidak terikat pada siklus hidup layar atau `ViewModel`[cite: 1]. Jika layar ditutup sebelum pekerjaan selesai, coroutine tetap berjalan di latar belakang sehingga berisiko menyebabkan kebocoran memori (*memory leak*) dan pemborosan sumber daya[cite: 1].
* **(c) Apa yang terjadi pada `getAnnouncement()` jika `getTasks()` gagal:** Berdasarkan prinsip *structured concurrency* di dalam `coroutineScope`, kegagalan pada satu *child* (`getTasks()`) akan membatalkan *parent scope* beserta seluruh *sibling* coroutine yang berjalan, sehingga `getAnnouncement()` otomatis dibatalkan sebelum selesai[cite: 1].

### 6. Tantangan (Tombol Sort dengan `Dispatchers.Default`)
* **Implementasi:** Menambahkan tombol **Sort** yang memanggil `sortTasksByTitle()` di `TasksViewModel`[cite: 1]. Proses pengurutan daftar tugas dijalankan menggunakan `withContext(Dispatchers.Default)` agar komputasi pengurutan tidak membebani *main thread*, kemudian hasilnya diperbarui ke `StateFlow`[cite: 1].

---

## Struktur Proyek

```text
com.example.coroutinedemo
├── data
│   ├── Task.kt
│   └── TaskRepository.kt
├── ui
│   ├── main
│   │   ├── TasksUiState.kt
│   │   ├── TasksViewModel.kt
│   │   └── TasksScreen.kt
│   └── theme
└── MainActivity.kt
```[cite: 1]
