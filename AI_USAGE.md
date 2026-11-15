# Catatan Penggunaan AI

Dokumen ini mengikuti Bagian 6 panduan UTS. AI generatif **digunakan** pada proyek ini,
dan semua penggunaan dicatat di bawah. Anggota tetap bertanggung jawab atas kebenaran data,
kode, pengujian, dan atribusi.

- Anggota: M. Fajrul Falah Ar-riziq (NIM 202510370110240)
- Alat: Claude (Anthropic), lewat aplikasi Claude Code di desktop

## Log penggunaan

| Tanggal | Anggota | Tujuan | Ringkasan prompt | Keluaran yang dipakai | Perubahan oleh anggota | Cara verifikasi |
|---|---|---|---|---|---|---|
| 2026-11-14 | M. Fajrul | Mengubah 50 PDF putusan menjadi dataset 16 atribut | Memberi ZIP 50 PDF, skema 16 atribut, dan aturan (nilai tidak ada = 0/null, nama disamarkan, caseId P001…, pemisah `\|`, PN Kepanjen) | Skrip Python (PyMuPDF dan regex) yang mengekstrak nomor perkara, tanggal, jenis narkotika, pasal, tuntutan, vonis, denda, barang bukti, keadaan memberatkan/meringankan | TODO: tulis perubahan yang Anda lakukan sendiri | Pencocokan nilai dengan teks halaman PDF; baris tidak cocok dibuka manual dari PDF |
| 2026-11-14 | M. Fajrul | Membuat `data/putusan-50.csv` dan `docs/data-provenance.csv` | Meminta satu baris per putusan dan satu baris per atribut per putusan | Kedua CSV (50 baris dan 650 baris) | Menetapkan extractor dan reviewer atas nama sendiri | Pemeriksaan jumlah baris, caseId unik, dan konsistensi nilai antar kedua file |
| 2026-11-14/15 | M. Fajrul | Pemeriksaan silang dan penentuan status | Meminta pengecekan status dan mencocokkan nilai dengan PDF | `docs/cross-check-report.csv`; temuan nilai tidak cocok dan dikoreksi dari PDF (denda putusan 504/2025, 454/455/2025, tahun putusan 518/2025, pasal 185/2026, dan lainnya) | Memutuskan aturan berat (ADR 0003) | Membuka PDF terkait untuk setiap koreksi |
| 2026-11-15 | M. Fajrul | Menerapkan aturan berat dari amar barang bukti | Memutuskan memakai aturan amar | Nilai berat baru untuk 5 putusan | Keputusan aturan oleh anggota | Membaca ulang amar barang bukti di teks PDF; jumlah berat 513/2025 dihitung ulang |
| 2026-11-15 | M. Fajrul | Menyusun kerangka repositori | Meminta kerangka sesuai Bagian 5.1 modul | README, ADR 0003, templat ADR 0001 dan 0002, templat AI_USAGE dan CONTRIBUTIONS, `pom.xml`, `.gitignore`, templat Issue dan PR | mengisi nomor kelompok, menjalankan build, dan mengatur repo di GitHub sendiri | Membandingkan struktur dengan Bagian 5.1 modul | 
| 2026-11-15 | M. Fajrul | Struktur data utama | Meminta antarmuka StrukturData dan array dinamis generik beserta test | StrukturData.java, ArrayDinamis.java, ArrayDinamisTest.java (22 test) | mempelajari alur kode dan menjalankan mvn test sendiri; belum mengubah logika| Dijelaskan baris per baris; mvn test dijalankan sendiri, 22 test lulus |
| 2026-11-15 | M. Fajrul | Struktur sekunder untuk audit | Meminta Stack generik berbasis ArrayDinamis | Stack.java, StackTest.java (15 test) | menjalankan mvn test sendiri | mvn test dijalankan sendiri, 37 test lulus |
| 2026-11-15 | M. Fajrul | Model data dan impor CSV | Meminta kelas Putusan, pembaca CSV, dan impor dengan validasi | Putusan.java, CsvParser.java, ImporPutusan.java dan test | Belum mengubah logika; memasukkan kode ke repo, membuat Issue, branch, commit, dan PR sendiri | mvn test dijalankan sendiri, 104 test lulus |
| 2026-11-15 | M. Fajrul | Fitur aplikasi dan menu | Meminta cari, filter, agregasi, urutan, ekspor, audit, dan Main | ArsipPutusan.java, KriteriaFilter.java, EksporCsv.java, Main.java dan test | Belum mengubah logika; memasukkan kode ke repo, membuat Issue, branch, commit, dan PR sendiri | mvn test dijalankan sendiri, 104 test lulus; skenario demo dijalankan sendiri, hasil di docs/demo/hasil-demo.txt |
| 2026-11-15 | M. Fajrul | Dokumentasi | Meminta README, ADR 0001-0002, dan trace manual | README.md, ADR 0001 dan 0002, trace-manual.md | Belum mengubah isi; memasukkan ke repo lewat Issue dan PR sendiri | Isi dokumen dicocokkan dengan kode dan hasil mvn test |

## Batas yang perlu dipahami

- Status `verified` pada `docs/data-provenance.csv` untuk sebagian besar baris berarti nilai
  **cocok secara otomatis** dengan teks halaman PDF yang dikutip. Itu belum berarti setiap
  baris dibaca manual oleh anggota. Sekitar 16 nilai dikonfirmasi langsung dari PDF.
- Kolom `barangBukti` berisi potongan teks amar hasil ekstraksi otomatis, belum diringkas.
- Skrip ekstraksi dijalankan secara lokal dan tidak disimpan di repositori ini.

## Sampel verifikasi manual oleh anggota

TODO: isi setelah Anda memeriksa sendiri. Disarankan minimal 10 putusan dengan semua atribut
dibandingkan langsung ke PDF.

| case_id | Atribut yang diperiksa | Hasil | Tanggal |
|---|---|---|---|
| TODO | TODO | TODO | TODO |

## Penggunaan AI untuk kode aplikasi

TODO: catat setiap saran AI pada kode Java (jika ada), apa yang diubah, dan bagaimana diuji.
Jika tidak dipakai AI untuk kode, tulis pernyataan itu di sini.
