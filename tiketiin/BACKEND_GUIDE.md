# Tiketiin — Backend Integration Guide

Panduan internal pengembangan kelompok. Status: fondasi backend dan integrasi beranda; fitur bisnis belum seluruhnya terhubung ke Supabase.

## 1. Teknologi dan arsitektur

- Kotlin, Jetpack Compose, ViewModel, Coroutines/StateFlow.
- Retrofit + OkHttp + Gson untuk REST API Supabase.
- Supabase PostgreSQL, Auth, Storage, RLS dan RPC.
- Room (SQLite) untuk cache katalog event, schema version 3.

Alur: Screen → ViewModel → Repository → Local/Remote Data Source → Room/Supabase. Jangan memanggil API langsung dari composable produksi. DummyDataSource dan DefaultTiketiinRepository masih merupakan simulasi, bukan fallback transaksi.

## 2. Setup anggota

1. Clone repository GitHub melalui Android Studio, checkout branch fitur masing-masing setelah branch tersedia dari develop.
2. Buka folder Gradle `tiketiin` (folder yang berisi settings.gradle.kts).
3. Pastikan JDK/SDK dan Gradle sesuai konfigurasi project. Sync Gradle.
4. Tambahkan konfigurasi berikut pada `local.properties` tanpa menghapus `sdk.dir`:

```properties
SUPABASE_URL=https://nyfmshdwgpickakxttjp.supabase.co
SUPABASE_PUBLISHABLE_KEY=<publishable key dari project Supabase>
```

Gunakan publishable key saja. Jangan memasukkan service_role/secret key, password, token, atau local.properties ke Git. Akses dashboard Supabase diberikan secara individual oleh pengelola project. Jangan membagikan password pribadi. Jalankan aplikasi di emulator.

## 3. Struktur backend

| Lokasi (di bawah com.example.tiketiin) | Tanggung jawab |
|---|---|
| data.remote.SupabaseConfig | Konfigurasi URL dan publishable key |
| data.remote.RetrofitClient | Client bersama, header API dan access token |
| data.remote.api.SupabaseApiService | Endpoint REST data |
| data.remote.api.AuthApiService | Endpoint Auth |
| data.remote.dto | Model JSON API |
| data.remote.EventRemoteDataSource | Katalog event dari server |
| data.local | DatabaseProvider, TiketiinDatabase, migrations, EventLocalDataSource |
| data.local.dao / entity | DAO dan entity Room |
| data.mapper.EventMapper | Konversi DTO → entity → model UI |
| data.repository.EventCatalogRepository | Cache dan refresh katalog |
| data.repository.RepositoryProvider | Penyedia repository katalog |
| data.auth | AuthRepository, SessionManager, SecureSessionStorage, AuthenticatedRequest |
| data.model.Models | Model frontend bersama |
| data.source.DummyDataSource | Simulasi frontend sementara |
| ui.debug.AuthTestScreen | Pengujian developer, jangan tampilkan pada produksi |

## 4. Autentikasi dan UUID

AuthRepository menyediakan login email/password, getCurrentUser, refreshSession, ensureValidSession dan logout. SessionManager diinisialisasi di MainActivity; menyimpan sesi terenkripsi dengan Android Keystore. Jangan mencetak isi token/password. Refresh bersifat on-demand pada helper, bukan background atau retry global.

```kotlin
val authRepository = AuthRepository()
val user = authRepository.login(email, password)
```

Gunakan identitas sesi sebenarnya untuk request privat:

```kotlin
val profile = AuthenticatedRequest.execute {
    val userId = requireNotNull(SessionManager.userId)
    RetrofitClient.api.getProfile("eq.$userId").single()
}
```

Contoh tersebut digunakan di repository/data source atau pengujian. Tangani loading, kegagalan jaringan, sesi kedaluwarsa/dicabut, dan CancellationException di layer yang tepat. Jangan menganggap hasSession berarti sesi telah divalidasi server. Jangan mengganti akun ketika request privat masih berjalan; batalkan request akun lama dan bersihkan state privat. Server/RLS harus memvalidasi auth.uid(), bukan mempercayai ID kiriman client.

User.id, TicketListing.sellerId, Order.buyerId dan Order.sellerId bertipe String. Event.createdBy bertipe String? (mapping ownership server masih perlu dikonfirmasi). ID event/listing/order/hotel tetap Int sesuai model saat ini. UUID dummy tidak boleh dikirim ke server; sellerId kosong pada draft hanya placeholder.

**Penting:** halaman login/register/OTP asli masih dummy. Sebelum mengaktifkan fitur privat produksi, hubungkan navigasi dan logout asli dengan AuthRepository, bukan DummyDataSource. Jangan gunakan sesi debug yang tersisa untuk menjalankan fitur dummy. Logout akun test lewat AuthTestScreen sebelum mengembalikan tampilan utama.

## 5. Katalog event dan Room

```kotlin
val repository = RepositoryProvider.getEventRepository(context)
val cached = repository.getCachedEvents()
repository.refreshEvents()
val updated = repository.getCachedEvents()
```

Tersedia juga observeEvents() untuk mengamati perubahan Room. Beranda membaca cache terlebih dahulu, lalu refresh Supabase. Katalog memuat events, venues dan listing AKTIF dengan quantity > 0. Harga minimum dan stok total dihitung dari listing yang terlihat sesuai RLS. Cache bukan sumber kebenaran transaksi. Event lewat dan tanpa stok disaring oleh HomeViewModel. Wishlist belum diintegrasikan pada katalog.

Room menggunakan database `tiketiin_database`, tabel `cached_events`, migration 1→2 untuk koordinat dan 2→3 untuk harga/stok. Simpan schema JSON dalam Git. Untuk perubahan tabel berikutnya: naikkan versi, buat migration, daftarkan pada DatabaseProvider (dan test builder), uji upgrade tanpa uninstall. Jangan gunakan destructive migration untuk data yang harus dipertahankan. Satu database bersama; koordinasikan perubahan schema. Jangan menyimpan token/KTP di cache event.

Gunakan Room hanya jika ada kebutuhan nyata, misalnya katalog offline atau draft. Data privat perlu pemisahan per akun dan pembersihan saat logout. Jangan mendukung order/payment offline dengan cache harga atau stok.

## 6. Database Supabase

Ringkasan rancangan yang telah dikerjakan; cek schema, SQL migration dan policy aktual sebelum memakai endpoint baru.

| Tabel | Kegunaan |
|---|---|
| profiles | Profil akun dan status verifikasi |
| user_preferences | Preferensi pengguna |
| venues | Lokasi event |
| events | Katalog event |
| ticket_listings | Listing, harga, stok |
| verifications | Pengajuan verifikasi |
| wishlists | Event tersimpan |
| event_views | Interaksi pengguna |
| orders | Pesanan |
| payments | Pembayaran simulasi |
| hotels | Katalog hotel |
| hotel_availability | Ketersediaan hotel |
| notifications | Notifikasi |

RPC yang pernah disiapkan: get_event_interaction_stats, submit_kyc, review_kyc, create_order, pay_order, confirm_ticket, expire_orders, cancel_order, get_price_history, get_event_demand_stats, get_user_recommendation_context, get_seller_sales_summary, get_available_hotels. Periksa signature, hasil return, status enum, otorisasi dan perilaku aktual pada SQL sebelum membuat DTO/endpoint. Jangan menebak parameter RPC. Perhitungan biaya transaksi dan status harus diselaraskan dengan SQL serta frontend. Pembayaran masih simulasi; jangan diperlakukan seperti payment gateway nyata.

Storage: avatars, event-posters, hotel-images (public sesuai rancangan); ticket-proofs dan kyc-documents (private). Akses file private harus mengikuti policy/otorisasi. Verifikasi policy dan validasi path/file KYC sebelum mengaktifkan upload nyata. Jangan gunakan dokumen identitas asli untuk pengujian.

## 7. Pembagian pekerjaan

| Anggota | Branch usulan | Scope |
|---|---|---|
| Nada | feature/auth-for-you | Login/register/OTP, profil, wishlist, For You, koordinasi katalog/search/detail |
| Raisya | feature/fair-price | Fair Price, listing, e-KYC |
| Naila | feature/hot-opportunity | Hot Opportunity, seller dashboard, kelola event, laporan penjualan |
| Saskia | feature/order-hotel | Checkout, payment/escrow simulasi, pesanan, rekomendasi hotel |

Gunakan fondasi bersama; pertahankan desain dan algoritma frontend yang sudah dibuat kecuali ada alasan perubahan. Koordinasikan kontrak DTO, endpoint, repository, model dan migration yang dipakai lebih dari satu anggota. Fitur privat bergantung pada integrasi login asli; jangan menggabungkan transaksi dummy dengan katalog remote. Setiap anggota bertanggung jawab atas testing fitur masing-masing dan alasan penggunaan local/remote.

## 8. Git workflow

`main` untuk versi stabil; `develop` untuk integrasi; `feature/...` untuk pekerjaan anggota. Saat ini fondasi berada pada `feature/backend-setup` sampai PR di-merge. Branch anggota dibuat dari develop yang sudah berisi fondasi. Commit kecil dan jelas, push ke branch sendiri, PR ke develop, review konflik dan test. Jangan push langsung ke main atau mengubah SQL shared tanpa koordinasi. Simpan SQL perubahan di folder migration project dan catat perubahan yang dijalankan di dashboard; workflow Supabase CLI belum disiapkan.

## 9. Testing dan batasan

Berdasarkan laporan testing pengembang: koneksi REST, cache Room, katalog beranda online/offline, login demo, current user, profile, RLS baca profiles, manual refresh, logout, session persistence, pengujian awal validasi/concurrency, dan regresi UUID telah diuji. Ini bukan audit independen atau jaminan seluruh policy aman.

Masih perlu: pengujian refresh on-demand dengan token benar-benar mendekati expiry; handling revoked session dan account switch; login/register/OTP/Google asli; integrasi search/detail/wishlist dan fitur bisnis; pengujian RLS lintas akun untuk semua tabel dan storage; verifikasi KYC dan RPC transaksi; fresh install offline, error handling dan release build. Navigasi Search/Wishlist/Detail dari beranda sementara dibatasi agar tidak mencampur data dummy dan remote. Jangan merilis aplikasi sebagai sistem transaksi sungguhan pada tahap ini.

Akun demo (password diberikan secara aman oleh pengelola): buyer@tiketiin.example, buyer2@tiketiin.example, seller@tiketiin.example. Jangan commit kredensial. Gunakan akun demo untuk test RLS, bukan akun pengguna nyata.
