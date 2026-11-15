# ADR 0001: Pemilihan struktur data utama

- Status: diterima
- Tanggal: 2026-11-15
- Pengambil keputusan: M. Fajrul Falah Ar-riziq

## Konteks

Aplikasi menyimpan 50 record putusan dan harus mendukung impor, tampil, tambah/hapus, cari,
filter, agregasi, urutan, audit, dan ekspor. Struktur utama harus generik dan dibuat sendiri,
tanpa koleksi `java.util`. Kebutuhan utamanya: data diisi sekali dari CSV, lalu dibaca dan
diiterasi berulang kali (tampil, filter, agregasi, urutan), sedangkan tambah dan hapus jarang.

## Keputusan

- **Struktur utama: `ArrayDinamis<T>`**, array mentah `Object[]` yang kapasitasnya digandakan
  saat penuh. Seluruh 50 objek `Putusan` disimpan di struktur ini selama aplikasi berjalan.
- **Struktur sekunder: `Stack<T>`** untuk riwayat audit. Operasi terbaru berada di puncak, jadi
  riwayat tampil dari yang terbaru. `Stack` memakai `ArrayDinamis` di dalamnya, bukan koleksi
  `java.util`.

Pemetaan Stack ke kontrak `StrukturData<T>`: `add` = `push`; `get(0)` = puncak; `remove(0)` =
`pop`. `remove` di posisi lain ditolak (`UnsupportedOperationException`) karena stack tidak
boleh menghapus elemen di tengah. Ini penyimpangan yang disengaja.

## Alternatif yang dipertimbangkan

- **Singly linked list**: tambah/hapus di ujung murah dan perubahan pointer mudah ditunjukkan,
  tetapi akses ke posisi tertentu O(n) dan pengurutan lebih rumit. Ditolak karena pekerjaan
  utama aplikasi adalah membaca dan mengurutkan, bukan menyisip.
- **Queue atau Stack sebagai struktur utama**: hapus/sisip di tengah dan pengurutan tidak natural,
  padahal itu fitur wajib. Stack lebih cocok sebagai struktur sekunder untuk riwayat.

## Analisis kompleksitas

| Operasi | Rata-rata | Terburuk | Catatan |
|---|---|---|---|
| `add` | O(1) teramortisasi | O(n) | O(n) hanya saat array penuh dan digandakan |
| `insert(pos)` | O(n) | O(n) | elemen dari `pos` ke belakang bergeser satu langkah |
| `get`, `set` | O(1) | O(1) | akses langsung lewat indeks |
| `remove(pos)` | O(n) | O(n) | elemen setelah `pos` bergeser ke kiri; hapus elemen terakhir O(1) |
| `search`, `contains` | O(n) | O(n) | pencarian linear, jumlah perbandingan dilaporkan |
| `Stack.push` | O(1) teramortisasi | O(n) | `add` pada array dinamis |
| `Stack.pop`, `peek`, `get(0)` | O(1) | O(1) | hanya menyentuh elemen terakhir array |
| `Stack.searchPreserve` | O(n) | O(n) | pop ke penampung lalu `restore`, urutan kembali sama |

## Invariant

`ArrayDinamis.validateInvariant()` memeriksa:
1. `data` tidak null dan kapasitas minimal 1.
2. `0 <= size <= kapasitas`.
3. Slot `0..size-1` tidak null (struktur menolak elemen null).
4. Slot `size..kapasitas-1` selalu null.

`Stack.validateInvariant()` meneruskan pemeriksaan ke penyimpanannya. Test memanggilnya setelah
hampir setiap operasi.

## Konsekuensi

- Menyisip atau menghapus di awal berbiaya O(n), tetapi untuk 50 record biayanya kecil.
- Kapasitas digandakan, sehingga memori bisa sampai dua kali jumlah elemen.
- Karena elemen `null` ditolak, tidak ada ambiguitas antara slot kosong dan elemen kosong.
