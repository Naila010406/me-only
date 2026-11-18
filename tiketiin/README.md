# Tiketiin (frontend)

Frontend aplikasi Tiketiin dengan Jetpack Compose. Semua data masih dummy (di memori),
jadi aplikasi bisa dijalankan tanpa internet dan tanpa Supabase.

## Cara membuka
1. Ekstrak zip, lalu di Android Studio pilih File > Open dan pilih folder `tiketiin`.
2. Tunggu Gradle Sync selesai, lalu Run di emulator (minimal Android 8 / API 26).
3. Masuk dengan email apa saja dan kata sandi minimal 8 karakter.

## Struktur (pola sama dengan tugas navigation)
- `Routes.kt`, `TiketiinApp.kt`: daftar alamat layar dan NavHost.
- `data/model`: bentuk data (Event, TicketListing, Order, Hotel, User).
- `data/source/DummyDataSource.kt`: data dummy. Nanti diganti Supabase.
- `data/repository/TiketiinRepository.kt`: satu-satunya pintu data untuk ViewModel.
- `domain/`: logika murni tanpa UI.
  - `DemandCalculator.kt`: Fungsi 2, Hot Opportunity.
  - `OtherCalculators.kt`: versi dasar Fungsi 1 (harga wajar), 3 (For You), 4 (hotel terdekat).
- `ui/<fitur>/`: Screen + ViewModel + UiState per fitur.
- `ui/components/`: komponen yang dipakai ulang (tombol, kartu event, bottom nav, ikon).
- `ui/theme/`: warna, font Poppins, tema.

## Mengganti poster
Poster masih berupa gradasi warna. Taruh gambar di `app/src/main/res/drawable/`
(nama huruf kecil, misalnya `poster_lalala.jpg`), lalu isi `posterRes = R.drawable.poster_lalala`
pada event terkait di `DummyDataSource.kt`.

## Unit test
`app/src/test/.../DemandCalculatorTest.kt` berisi 8 test untuk perhitungan Hot Opportunity.
