# UTS Struktur Data — Putusan Narkotika PN Kepanjen

Aplikasi Java untuk mengelola 50 putusan perkara narkotika Pengadilan Negeri Kepanjen
dengan struktur data buatan sendiri (tanpa koleksi `java.util` sebagai struktur utama).

## Identitas

| | |
|---|---|
| Mata kuliah | Struktur Data (UTS Semester Ganjil 2026/2027), Universitas Muhammadiyah Malang |
| Dosen | Galih W. Wicaksono |
| Kelompok | Kelompok 10, Kelas D (Pengadilan Negeri Kepanjen) |
| Anggota | M. Fajrul Falah Ar-riziq (NIM 202510370110240) |
| Catatan | Dikerjakan perorangan atas izin dosen. Extractor dan reviewer data sama-sama M. Fajrul Falah Ar-riziq |

## Struktur data pilihan

- **Struktur utama: array dinamis** (`ArrayDinamis<T>`), tempat seluruh 50 objek `Putusan` disimpan.
- **Struktur sekunder: stack** (`Stack<T>`), untuk riwayat audit operasi pengguna.

Alasan, alternatif, kompleksitas, dan invariant ada di
[ADR 0001](docs/adr/0001-pemilihan-struktur-data.md). Strategi pencarian dan pengurutan ada di
[ADR 0002](docs/adr/0002-strategi-pencarian-dan-pengurutan.md). Trace manual operasi struktur
data ada di [docs/trace/trace-manual.md](docs/trace/trace-manual.md).

## Fitur

| Menu | Fitur |
|---|---|
| 1 | Impor CSV: membaca 50 record, menolak record tidak valid atau duplikat tanpa menghentikan program |
| 2 | Tampil ringkasan semua putusan menurut urutan struktur utama |
| 3 | Tambah putusan (satu baris CSV 16 kolom) |
| 4 | Hapus putusan menurut caseId |
| 5 | Cari caseId atau nomor perkara, dengan laporan jumlah langkah |
| 6 | Filter jenis narkotika, rentang berat, pasal terbukti, dan rentang vonis |
| 7 | Agregasi: jumlah perkara, rata-rata vonis, total denda, distribusi jenis narkotika |
| 8 | Urutkan menurut vonis (menaik atau menurun) dengan pencacah perbandingan dan pergeseran |
| 9 | Riwayat audit (terbaru di atas) |
| 10 | Ekspor hasil filter terakhir atau semua putusan ke CSV |

## Prasyarat

- JDK 21 atau lebih baru
- Maven 3.9+

## Build

```bash
mvn -q compile
```

## Test

```bash
mvn test
```

Hasil yang diharapkan: seluruh test lulus (`Failures: 0, Errors: 0`). Test yang memakai data asli
dijalankan dari folder repositori karena membaca `data/putusan-50.csv`.

## Menjalankan

Jalankan dari folder repositori setelah `mvn compile`:

```bash
java -cp target/classes id.ac.umm.strukdat.Main
```

Contoh sesi:

```
Pilih menu: 1
Berkas CSV [data/putusan-50.csv]:
Impor selesai: 50 diterima, 0 ditolak. Total di arsip: 50
Pilih menu: 5
caseId atau nomor perkara: P050
Ditemukan di posisi 49 setelah 50 langkah: P050 | 135/Pid.Sus/2026/PN Kpn | 2026-09-29 | Sabu | vonis 72 bln
```

Skenario lengkap (impor, cari, hapus awal/tengah/akhir, tambah tiga record, record tidak valid,
filter gabungan, agregasi, urutan, riwayat, ekspor) ada di
[docs/demo/skenario-input.txt](docs/demo/skenario-input.txt). Untuk menjalankannya dan menyimpan
keluarannya (Windows):

```
cmd /c "java -cp target\classes id.ac.umm.strukdat.Main < docs\demo\skenario-input.txt > docs\demo\hasil-demo.txt"
```

## Dataset

- `data/putusan-50.csv`: 50 putusan, satu baris per putusan, 16 kolom, UTF-8.
- `docs/data-provenance.csv`: 650 baris (50 putusan × 13 atribut). Kolom:
  `case_id, field_name, value, source_file, source_page, extractor, reviewer, status`.
- `docs/cross-check-report.csv`: salinan provenance dengan kolom `hasil_cek`, hasil
  pencocokan otomatis nilai terhadap teks halaman PDF yang dikutip.
- PDF sumber **tidak** disimpan di repositori. Sumber: putusan3.mahkamahagung.go.id.
- Nama terdakwa disamarkan dengan kode `T001`–`T050`. Nomor perkara dipertahankan
  sebagai identitas sumber.

### Kamus data

| Kolom | Arti |
|---|---|
| `caseId` | P001–P050, diurut menurut tanggal putusan |
| `nomorPerkara` | Nomor perkara, contoh `132/Pid.Sus/2026/PN Kpn` |
| `pengadilan` | `PN Kepanjen` |
| `tanggalPutusan` | Tanggal pengucapan putusan dalam sidang terbuka (`YYYY-MM-DD`). Jika amar menyatakan diucapkan "pada tanggal dan hari itu juga", dipakai tanggal musyawarah |
| `terdakwaKode` | Kode samaran `T001`… |
| `jenisNarkotika` | Jenis narkotika pada barang bukti; lebih dari satu dipisah `\|` |
| `beratBarangBuktiGram` | Lihat aturan berat di bawah |
| `pasalDakwaan` | Pasal dakwaan; lebih dari satu dipisah `\|`. Pasal 609 diberi akhiran `KUHP`, pasal UU Kesehatan diberi akhiran `UU Kesehatan` |
| `pasalTerbukti` | Pasal yang terbukti menurut amar, format sama |
| `tuntutanBulan` | Tuntutan pidana penjara dalam bulan (tahun × 12 + bulan) |
| `vonisBulan` | Vonis pidana penjara dalam bulan |
| `dendaRupiah` | Denda dalam rupiah penuh; 0 jika tidak ada |
| `barangBukti` | Potongan teks amar barang bukti, dipisah `\|` |
| `keadaanMemberatkan` | Keadaan yang memberatkan, dipisah `\|` |
| `keadaanMeringankan` | Keadaan yang meringankan, dipisah `\|` |
| `sourcePages` | Gabungan nomor halaman PDF yang dipakai, dipisah `\|` |

### Aturan ekstraksi

- Nilai yang tidak ditemukan ditulis `0` (angka) atau kosong (teks), tidak diperkirakan.
- Putusan dengan dua terdakwa yang dijatuhi pidana sama digabung menjadi satu baris.
- **Aturan berat** (`beratBarangBuktiGram`): jumlah berat netto/bersih narkotika yang
  tertulis pada amar barang bukti. Jika amar tidak mencantumkan berat narkotika, nilainya `0`.
  Angka berat dari dakwaan, keterangan saksi, atau hasil laboratorium **tidak** dipakai.
  Rinciannya di [ADR 0003](docs/adr/0003-aturan-berat-barang-bukti.md).
- Status provenance: `verified` berarti nilai cocok dengan halaman PDF yang dikutip.
  Sebagian besar baris diverifikasi lewat pencocokan otomatis, bukan pembacaan manual
  satu per satu. Lihat `AI_USAGE.md`.

## Struktur repositori

```
src/main/java/        kode aplikasi (struktur, model, impor, layanan, Main)
src/test/java/        test otomatis
data/putusan-50.csv   dataset 50 putusan
docs/data-provenance.csv
docs/adr/             catatan keputusan arsitektur
docs/trace/           trace manual struktur data
docs/demo/            skenario dan bukti demo
AI_USAGE.md           catatan penggunaan AI
CONTRIBUTIONS.md      bukti kontribusi
pom.xml
```
