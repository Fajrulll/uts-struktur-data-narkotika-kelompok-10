# Trace manual

Trace tangan atas contoh kecil. Hasilnya sama dengan perilaku kode dan test di
`ArrayDinamisTest`, `StackTest`, dan `ArsipPutusanTest`.

## 1. `ArrayDinamis.remove(2)` pada [A, B, C, D, E]

Awal: `size = 5`, `data = [A, B, C, D, E, null, null, null]` (kapasitas 8).

| Langkah | `i` | Aksi | `data` |
|---|---|---|---|
| 1 | 2 | `data[2] = data[3]` | [A, B, **D**, D, E, ...] |
| 2 | 3 | `data[3] = data[4]` | [A, B, D, **E**, E, ...] |
| 3 | - | `data[size-1] = null`, `size = 4` | [A, B, D, E, **null**, ...] |

Elemen yang dikembalikan: C. Jumlah langkah (pergeseran): 2. Invariant tetap terpenuhi: slot
0..3 berisi elemen, slot 4 dan seterusnya null.

## 2. `ArrayDinamis.insert(1, "X")` pada [A, B, C] (size 3, kapasitas 4)

| Langkah | `i` | Aksi | `data` |
|---|---|---|---|
| 1 | 3 | `data[3] = data[2]` | [A, B, C, **C**] |
| 2 | 2 | `data[2] = data[1]` | [A, B, **B**, C] |
| 3 | - | `data[1] = "X"`, `size = 4` | [A, **X**, B, C] |

Jumlah pergeseran: 2. Kapasitas sudah penuh (4 dari 4); sisipan berikutnya akan memicu
`resize(8)`.

## 3. `Stack.searchPreserve("B")` pada stack [A, B, C] (puncak = C)

| Langkah | Aksi | Stack | Penampung | Langkah |
|---|---|---|---|---|
| 1 | `pop` C, `push` ke penampung, C != B | [A, B] | [C] | 1 |
| 2 | `pop` B, `push` ke penampung, B == B, berhenti | [A] | [C, B] | 2 |
| 3 | `restore`: `pop` B dari penampung, `push` ke stack | [A, B] | [C] | |
| 4 | `restore`: `pop` C dari penampung, `push` ke stack | [A, B, C] | [] | |

Hasil: posisi 1 dari puncak (C = 0, B = 1), jumlah langkah 2. Urutan akhir [A, B, C] sama
seperti awal.

## 4. Insertion sort menaik atas vonis [5, 2, 4]

| `i` | `kunci` | Perbandingan | Pergeseran | Array setelahnya |
|---|---|---|---|---|
| 1 | 2 | `5 > 2` ya (1) | 5 ke kanan (1) | [2, 5, 4] |
| 2 | 4 | `5 > 4` ya (2), `2 > 4` tidak (3) | 5 ke kanan (2) | [2, 4, 5] |

Total: 3 perbandingan, 2 pergeseran. Vonis sama tidak digeser (perbandingan memakai `>`
bukan `>=`), sehingga pengurutan stabil.
