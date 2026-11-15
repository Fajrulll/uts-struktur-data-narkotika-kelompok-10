# ADR 0003: Aturan berat barang bukti

- Status: diterima
- Tanggal: 2026-11-15
- Pengambil keputusan: M. Fajrul Falah Ar-riziq

## Konteks

PDF putusan mencantumkan berat narkotika di beberapa tempat yang angkanya tidak selalu
sama: dakwaan, keterangan saksi, hasil laboratorium, dan amar barang bukti. Contohnya
putusan 144/Pid.Sus/2026/PN Kpn menulis 0,11 g dari saksi, sedangkan hasil lab netto 0,017 g.
Modul meminta nilai yang tidak ditemukan ditulis 0 atau null dan tidak diperkirakan.

## Keputusan

`beratBarangBuktiGram` adalah jumlah berat netto/bersih narkotika yang tertulis pada
**amar barang bukti** (bagian "Menetapkan barang bukti"). Jika amar tidak mencantumkan
berat narkotika, nilainya `0`. Jika amar memberi total dan rincian, dipakai total tertulis
agar tidak terhitung dua kali.

## Alternatif yang dipertimbangkan

- Memakai berat dari dakwaan atau lab: ditolak karena angkanya berbeda antar bagian dan
  sulit ditelusuri ke satu halaman.
- Memakai berat dari saksi: ditolak karena bukan hasil penimbangan resmi.

## Konsekuensi

- Tujuh putusan bernilai 0 karena amar tidak mencantumkan berat narkotika:
  - 463/2025, 454/2025, 527/2025, 528/2025: amar hanya memuat HP dan/atau alat hisap.
  - 526/2025 dan 144/2026: amar menyebut sabu tanpa berat (angka berat hanya ada di dakwaan
    atau keterangan saksi).
  - 520/2025: amar menyebut 9 batang ganja tanpa berat.
- Putusan 445/2025: amar menulis ganja 58 g sebagai "berat" tanpa kata bersih atau netto,
  sedangkan biji ganja 3,01 g "berat bersih". Karena aturan hanya memakai netto/bersih,
  nilai yang dipakai 3,01. Alternatif 61,01 (58 + 3,01) tidak dipakai.
- Putusan 513/2025 menjumlahkan semua berat netto narkotika di amar (sabu dan ekstasi): 16,579.
- Nilai berat tidak dapat dipakai sebagai ukuran barang bukti sebenarnya, hanya sebagai
  yang tertulis di amar.
