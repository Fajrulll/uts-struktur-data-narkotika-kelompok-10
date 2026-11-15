# UTS Struktur Data — Putusan Narkotika PN Kepanjen

Aplikasi Java untuk mengelola 50 putusan perkara narkotika Pengadilan Negeri Kepanjen
dengan struktur data buatan sendiri (tanpa koleksi `java.util` sebagai struktur utama).

> **Status:** kerangka repositori. Dataset dan provenance sudah ada; aplikasi Java, test,
> dan struktur pilihan **belum** diimplementasikan. Bagian bertanda `TODO` diisi saat
> fitur selesai.

## Identitas

| | |
|---|---|
| Mata kuliah | Struktur Data (UTS Semester Ganjil 2026/2027), Universitas Muhammadiyah Malang |
| Dosen | Galih W. Wicaksono |
| Kelompok | Kelompok 10, Kelas D (Pengadilan Negeri Kepanjen) |
| Anggota | M. Fajrul Falah Ar-riziq (NIM 202510370110240) |
| Catatan | Dikerjakan perorangan atas izin dosen. Extractor dan reviewer data sama-sama M. Fajrul Falah Ar-riziq |

## Struktur data pilihan

TODO: isi setelah keputusan dibuat. Rujuk `docs/adr/0001-pemilihan-struktur-data.md`
dan `docs/adr/0002-strategi-pencarian-dan-pengurutan.md`.

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
  Rinciannya di `docs/adr/0003-aturan-berat-barang-bukti.md`.
- Status provenance: `verified` berarti nilai cocok dengan halaman PDF yang dikutip.
  Sebagian besar baris diverifikasi lewat pencocokan otomatis, bukan pembacaan manual
  satu per satu. Lihat `AI_USAGE.md`.

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

## Menjalankan

TODO: isi perintah dan contoh setelah kelas `Main` dibuat.

## Struktur repositori

```
src/main/java/        kode aplikasi
src/test/java/        test otomatis
data/putusan-50.csv   dataset 50 putusan
docs/data-provenance.csv
docs/adr/             catatan keputusan arsitektur
docs/trace/           trace manual struktur data
docs/demo/            bukti demo
AI_USAGE.md           catatan penggunaan AI
CONTRIBUTIONS.md      bukti kontribusi
pom.xml
```
