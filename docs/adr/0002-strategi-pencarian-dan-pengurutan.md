# ADR 0002: Strategi pencarian dan pengurutan

- Status: diterima
- Tanggal: 2026-11-15
- Pengambil keputusan: M. Fajrul Falah Ar-riziq

## Konteks

Dataset 50 record dengan atribut teks, tanggal, dan angka. Pengguna perlu mencari berdasarkan
caseId atau nomor perkara, memfilter (jenis, berat, pasal, rentang vonis), dan mengurutkan
menurut vonis tanpa merusak invariant struktur utama.

## Keputusan

**Pencarian: pencarian linear** dari indeks 0, mencocokkan caseId atau nomor perkara (persis,
tidak peka huruf besar-kecil). Jumlah perbandingan dilaporkan sebagai "langkah". Filter juga
satu kali lintasan linear dan mempertahankan urutan struktur utama.

**Pengurutan: insertion sort yang stabil**, dijalankan pada **salinan** array sehingga struktur
utama tidak berubah. Elemen hanya digeser jika kuncinya benar-benar lebih besar (menaik) atau
lebih kecil (menurun), jadi putusan dengan vonis sama tetap berurutan seperti di struktur utama.

## Alternatif yang dipertimbangkan

- **Pencarian biner**: O(log n), tetapi mensyaratkan data terurut menurut kunci pencarian. Setelah
  tambah/hapus, urutan caseId tidak terjamin, sehingga harus mengurutkan ulang dulu. Untuk 50
  record, selisih langkahnya tidak berarti.
- **Merge sort**: O(n log n) dan stabil, tetapi butuh array bantu dan kode lebih panjang.
- **Quick sort**: rata-rata cepat, tetapi tidak stabil dan terburuk O(n^2).

## Analisis kompleksitas dan stabilitas

| Algoritma | Waktu terbaik | Rata-rata | Terburuk | Ruang | Stabil |
|---|---|---|---|---|---|
| Pencarian linear | O(1) | O(n) | O(n) | O(1) | tidak relevan |
| Insertion sort | O(n) | O(n^2) | O(n^2) | O(1) tambahan (selain salinan) | ya |

Pada data yang sudah terurut, insertion sort hanya butuh n-1 perbandingan dan 0 pergeseran
(diuji di `ArsipPutusanTest`). Dengan n = 50, terburuknya 1.225 perbandingan.

## Pencacah operasi

`HasilUrut` mengembalikan dua pencacah: `perbandingan` (jumlah perbandingan vonis) dan
`pergeseran` (jumlah elemen yang digeser). Menu pengurutan menampilkannya, dan operasi tersebut
juga dicatat di riwayat audit. Contoh dari skenario `docs/demo/skenario-input.txt`: urut menaik
atas 50 putusan menghasilkan 800 perbandingan dan 753 pergeseran. Pencarian menampilkan
jumlah langkah (misalnya P001 = 1 langkah, P050 = 50 langkah, tidak ditemukan = 50 langkah).

## Konsekuensi

- Pencarian dan pengurutan mudah dijelaskan langkah demi langkah.
- Jika data membesar, pencarian biner atas data terurut dan merge sort layak dipertimbangkan.
