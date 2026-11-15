package id.ac.umm.strukdat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import id.ac.umm.strukdat.layanan.ArsipPutusan;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

class MainTest {

    private static final Path DATA = Path.of("data", "putusan-50.csv");

    /** Menjalankan menu dengan masukan tersimpan (satu baris per elemen), mengembalikan keluaran. */
    private String jalankan(ArsipPutusan arsip, String... baris) {
        ByteArrayOutputStream tampung = new ByteArrayOutputStream();
        PrintStream keluar = new PrintStream(tampung, true, StandardCharsets.UTF_8);
        Scanner masukan = new Scanner(String.join("\n", baris) + "\n");
        Main.jalankan(masukan, keluar, arsip);
        return tampung.toString(StandardCharsets.UTF_8);
    }

    @Test
    void imporLaluTampilMenunjukkanLimaPuluh() {
        assumeTrue(Files.exists(DATA));
        ArsipPutusan arsip = new ArsipPutusan();
        String keluar = jalankan(arsip, "1", "", "2", "0");
        assertTrue(keluar.contains("Impor selesai: 50 diterima, 0 ditolak"), keluar);
        assertTrue(keluar.contains("Jumlah: 50"), keluar);
        assertEquals(50, arsip.jumlah());
    }

    @Test
    void cariYangAdaDanTidakAdaMelaporkanLangkah() {
        assumeTrue(Files.exists(DATA));
        String keluar = jalankan(new ArsipPutusan(), "1", "", "5", "P001", "5", "P999", "0");
        assertTrue(keluar.contains("Ditemukan di posisi 0 setelah 1 langkah"), keluar);
        assertTrue(keluar.contains("Tidak ditemukan setelah 50 langkah"), keluar);
    }

    @Test
    void recordTidakValidDitolakDanProgramTetapBerjalan() {
        ArsipPutusan arsip = new ArsipPutusan();
        String keluar = jalankan(arsip, "3", "P001,rusak,tidak,lengkap", "2", "0");
        assertTrue(keluar.contains("Data tidak valid"), keluar);
        assertTrue(keluar.contains("(kosong)"), keluar);
        assertTrue(keluar.contains("Selesai."), keluar);
        assertEquals(0, arsip.jumlah());
    }

    @Test
    void menuTidakDikenalDanMasukanHabisTidakMenjatuhkanProgram() {
        String keluar = jalankan(new ArsipPutusan(), "99");
        assertTrue(keluar.contains("Menu tidak dikenal: 99"), keluar);
        assertTrue(keluar.contains("Selesai."), keluar);
    }

    @Test
    void kriteriaFilterBukanAngkaDilaporkanTanpaMenghentikanProgram() {
        assumeTrue(Files.exists(DATA));
        String keluar = jalankan(new ArsipPutusan(), "1", "", "6", "", "abc", "", "", "", "", "0");
        assertTrue(keluar.contains("Data tidak valid: bukan angka desimal"), keluar);
        assertTrue(keluar.contains("Selesai."), keluar);
    }

    @Test
    void hapusLaluRiwayatMencatatOperasi() {
        assumeTrue(Files.exists(DATA));
        ArsipPutusan arsip = new ArsipPutusan();
        String keluar = jalankan(arsip, "1", "", "4", "P001", "9", "0");
        assertTrue(keluar.contains("Dihapus: P001"), keluar);
        assertTrue(keluar.contains("#2 HAPUS P001"), keluar);
        assertTrue(keluar.contains("#1 IMPOR"), keluar);
        assertEquals(49, arsip.jumlah());
    }

    @Test
    void agregasiDanUrutDitampilkan() {
        assumeTrue(Files.exists(DATA));
        String keluar = jalankan(new ArsipPutusan(), "1", "", "7", "8", "2", "0");
        assertTrue(keluar.contains("Jumlah perkara   : 50"), keluar);
        assertTrue(keluar.contains("Distribusi jenis narkotika"), keluar);
        assertTrue(keluar.contains("Perbandingan:"), keluar);
    }

    @Test
    void eksporHasilFilterMenulisBerkas() throws IOException {
        assumeTrue(Files.exists(DATA));
        Path sementara = Files.createTempFile("menu-ekspor", ".csv");
        try {
            String keluar = jalankan(new ArsipPutusan(),
                    "1", "", "6", "Ganja", "", "", "", "", "", "10", sementara.toString(), "0");
            assertTrue(keluar.contains("Tersimpan: "), keluar);
            assertTrue(Files.readString(sementara).startsWith("caseId,"));
        } finally {
            Files.deleteIfExists(sementara);
        }
    }
}
