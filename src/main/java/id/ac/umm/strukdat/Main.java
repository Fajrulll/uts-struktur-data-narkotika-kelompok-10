package id.ac.umm.strukdat;

import id.ac.umm.strukdat.impor.CsvParser;
import id.ac.umm.strukdat.impor.ImporPutusan.HasilImpor;
import id.ac.umm.strukdat.layanan.ArsipPutusan;
import id.ac.umm.strukdat.layanan.ArsipPutusan.Agregat;
import id.ac.umm.strukdat.layanan.ArsipPutusan.HasilCari;
import id.ac.umm.strukdat.layanan.ArsipPutusan.HasilUrut;
import id.ac.umm.strukdat.layanan.KriteriaFilter;
import id.ac.umm.strukdat.model.DataTidakValidException;
import id.ac.umm.strukdat.model.Putusan;
import id.ac.umm.strukdat.struktur.ArrayDinamis;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Aplikasi menu teks untuk mengelola putusan narkotika PN Kepanjen.
 *
 * <p>Menjalankan: {@code java -cp target/classes id.ac.umm.strukdat.Main}
 */
public final class Main {

    private static final String BERKAS_BAWAAN = "data/putusan-50.csv";
    private static final String EKSPOR_BAWAAN = "output/hasil.csv";

    private Main() {
    }

    public static void main(String[] args) {
        jalankan(new Scanner(System.in), System.out, new ArsipPutusan());
    }

    /** Loop menu. Dipisah dari main supaya bisa diuji dengan masukan tersimpan. */
    static void jalankan(Scanner masukan, PrintStream keluar, ArsipPutusan arsip) {
        // hasil filter terakhir, dipakai untuk ekspor
        ArrayDinamis<Putusan> hasilTerakhir = null;
        while (true) {
            tampilMenu(keluar);
            String pilihan = baca(masukan, keluar, "Pilih menu: ");
            if (pilihan == null || pilihan.equals("0")) {
                keluar.println("Selesai.");
                return;
            }
            try {
                switch (pilihan) {
                    case "1" -> impor(masukan, keluar, arsip);
                    case "2" -> tampil(keluar, arsip.semua());
                    case "3" -> tambah(masukan, keluar, arsip);
                    case "4" -> hapus(masukan, keluar, arsip);
                    case "5" -> cari(masukan, keluar, arsip);
                    case "6" -> hasilTerakhir = filter(masukan, keluar, arsip);
                    case "7" -> agregasi(keluar, arsip);
                    case "8" -> urut(masukan, keluar, arsip);
                    case "9" -> riwayat(keluar, arsip);
                    case "10" -> ekspor(masukan, keluar, arsip, hasilTerakhir);
                    default -> keluar.println("Menu tidak dikenal: " + pilihan);
                }
            } catch (IOException e) {
                keluar.println("Kesalahan berkas: " + e.getMessage());
            } catch (DataTidakValidException | IllegalArgumentException e) {
                keluar.println("Data tidak valid: " + e.getMessage());
            }
        }
    }

    // ----------------------------------------------------------- tampilan

    private static void tampilMenu(PrintStream k) {
        k.println();
        k.println("=== Arsip Putusan Narkotika PN Kepanjen ===");
        k.println(" 1. Impor CSV         6. Filter");
        k.println(" 2. Tampil semua      7. Agregasi");
        k.println(" 3. Tambah putusan    8. Urutkan menurut vonis");
        k.println(" 4. Hapus putusan     9. Riwayat audit");
        k.println(" 5. Cari             10. Ekspor");
        k.println(" 0. Keluar");
    }

    private static void tampil(PrintStream k, ArrayDinamis<Putusan> data) {
        if (data.isEmpty()) {
            k.println("(kosong)");
            return;
        }
        for (int i = 0; i < data.size(); i++) {
            k.println((i + 1) + ". " + data.get(i).ringkasan());
        }
        k.println("Jumlah: " + data.size());
    }

    // --------------------------------------------------------------- menu

    private static void impor(Scanner in, PrintStream k, ArsipPutusan arsip) throws IOException {
        String jalur = baca(in, k, "Berkas CSV [" + BERKAS_BAWAAN + "]: ");
        if (jalur == null || jalur.isBlank()) {
            jalur = BERKAS_BAWAAN;
        }
        HasilImpor h = arsip.impor(Path.of(jalur));
        k.println("Impor selesai: " + h.getDiterima() + " diterima, " + h.getDitolak()
                + " ditolak. Total di arsip: " + arsip.jumlah());
        for (int i = 0; i < h.getPesanDitolak().size(); i++) {
            k.println("  " + h.getPesanDitolak().get(i));
        }
    }

    private static void tambah(Scanner in, PrintStream k, ArsipPutusan arsip) {
        k.println("Masukkan satu baris CSV (16 kolom, urutan seperti header):");
        k.println(String.join(",", Putusan.namaKolom()));
        String baris = baca(in, k, "> ");
        if (baris == null || baris.isBlank()) {
            k.println("Dibatalkan.");
            return;
        }
        ArrayDinamis<String[]> hasil = CsvParser.parse(baris);
        if (hasil.isEmpty()) {
            k.println("Dibatalkan.");
            return;
        }
        Putusan p = Putusan.dariKolom(hasil.get(0));
        if (arsip.tambah(p)) {
            k.println("Ditambahkan: " + p.ringkasan() + ". Total: " + arsip.jumlah());
        } else {
            k.println("Ditolak: caseId atau nomor perkara sudah ada.");
        }
    }

    private static void hapus(Scanner in, PrintStream k, ArsipPutusan arsip) {
        String id = baca(in, k, "caseId yang dihapus: ");
        Putusan p = arsip.hapus(id);
        if (p == null) {
            k.println("Tidak ditemukan: " + id);
        } else {
            k.println("Dihapus: " + p.ringkasan() + ". Total: " + arsip.jumlah());
        }
    }

    private static void cari(Scanner in, PrintStream k, ArsipPutusan arsip) {
        String kunci = baca(in, k, "caseId atau nomor perkara: ");
        HasilCari h = arsip.cari(kunci);
        if (h.ditemukan()) {
            k.println("Ditemukan di posisi " + h.getIndeks() + " setelah " + h.getLangkah()
                    + " langkah: " + h.getPutusan().ringkasan());
        } else {
            k.println("Tidak ditemukan setelah " + h.getLangkah() + " langkah.");
        }
    }

    private static ArrayDinamis<Putusan> filter(Scanner in, PrintStream k, ArsipPutusan arsip) {
        KriteriaFilter kr = new KriteriaFilter();
        k.println("Kosongkan (Enter) untuk melewati kriteria.");
        kr.jenis(baca(in, k, "Jenis narkotika (mis. Sabu): "));
        String bMin = baca(in, k, "Berat minimum gram: ");
        String bMaks = baca(in, k, "Berat maksimum gram: ");
        kr.berat(angkaDesimal(bMin, 0), angkaDesimal(bMaks, Double.MAX_VALUE));
        kr.pasal(baca(in, k, "Pasal terbukti mengandung (mis. 114): "));
        String vMin = baca(in, k, "Vonis minimum bulan: ");
        String vMaks = baca(in, k, "Vonis maksimum bulan: ");
        kr.vonis(angkaBulat(vMin, 0), angkaBulat(vMaks, Integer.MAX_VALUE));
        ArrayDinamis<Putusan> hasil = arsip.filter(kr);
        tampil(k, hasil);
        return hasil;
    }

    private static void agregasi(PrintStream k, ArsipPutusan arsip) {
        Agregat a = arsip.agregasi();
        k.println("Jumlah perkara   : " + a.getJumlahPerkara());
        k.println("Rata-rata vonis  : " + String.format("%.2f", a.getRataRataVonisBulan()) + " bulan");
        k.println("Total denda      : Rp" + a.getTotalDenda());
        k.println("Distribusi jenis narkotika:");
        for (int i = 0; i < a.jumlahJenisBerbeda(); i++) {
            k.println("  " + a.getNamaJenis(i) + ": " + a.getJumlahJenis(i));
        }
    }

    private static void urut(Scanner in, PrintStream k, ArsipPutusan arsip) {
        String arah = baca(in, k, "Urutan (1 = menaik, 2 = menurun) [1]: ");
        boolean menaik = arah == null || !arah.trim().equals("2");
        HasilUrut h = arsip.urutkanBerdasarkanVonis(menaik);
        tampil(k, h.getData());
        k.println("Perbandingan: " + h.getPerbandingan() + ", pergeseran: " + h.getPergeseran());
    }

    private static void riwayat(PrintStream k, ArsipPutusan arsip) {
        ArrayDinamis<String> r = arsip.riwayatTerbaruDulu();
        if (r.isEmpty()) {
            k.println("(riwayat kosong)");
            return;
        }
        k.println("Riwayat (terbaru di atas):");
        for (int i = 0; i < r.size(); i++) {
            k.println("  " + r.get(i));
        }
    }

    private static void ekspor(Scanner in, PrintStream k, ArsipPutusan arsip,
            ArrayDinamis<Putusan> hasilTerakhir) throws IOException {
        ArrayDinamis<Putusan> data = hasilTerakhir != null ? hasilTerakhir : arsip.semua();
        String sumber = hasilTerakhir != null ? "hasil filter terakhir" : "semua putusan";
        String jalur = baca(in, k, "Ekspor " + sumber + " ke [" + EKSPOR_BAWAAN + "]: ");
        if (jalur == null || jalur.isBlank()) {
            jalur = EKSPOR_BAWAAN;
        }
        arsip.ekspor(Path.of(jalur), data);
        k.println("Tersimpan: " + jalur + " (" + data.size() + " putusan)");
    }

    // ------------------------------------------------------------- bantu

    private static String baca(Scanner in, PrintStream k, String petunjuk) {
        k.print(petunjuk);
        if (!in.hasNextLine()) {
            return null;
        }
        return in.nextLine();
    }

    private static double angkaDesimal(String teks, double bawaan) {
        if (teks == null || teks.isBlank()) {
            return bawaan;
        }
        try {
            return Double.parseDouble(teks.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bukan angka desimal: '" + teks + "'");
        }
    }

    private static int angkaBulat(String teks, int bawaan) {
        if (teks == null || teks.isBlank()) {
            return bawaan;
        }
        try {
            return Integer.parseInt(teks.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bukan bilangan bulat: '" + teks + "'");
        }
    }
}
