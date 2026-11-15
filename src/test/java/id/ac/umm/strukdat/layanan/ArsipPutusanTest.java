package id.ac.umm.strukdat.layanan;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import id.ac.umm.strukdat.impor.EksporCsv;
import id.ac.umm.strukdat.impor.ImporPutusan.HasilImpor;
import id.ac.umm.strukdat.layanan.ArsipPutusan.Agregat;
import id.ac.umm.strukdat.layanan.ArsipPutusan.HasilCari;
import id.ac.umm.strukdat.layanan.ArsipPutusan.HasilUrut;
import id.ac.umm.strukdat.model.Putusan;
import id.ac.umm.strukdat.struktur.ArrayDinamis;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ArsipPutusanTest {

    private static final Path DATA = Path.of("data", "putusan-50.csv");

    /** Arsip berisi 50 putusan asli. Test dilewati jika berkas tidak ada. */
    private ArsipPutusan muat() throws IOException {
        assumeTrue(Files.exists(DATA), "data/putusan-50.csv tidak ditemukan dari folder kerja");
        ArsipPutusan a = new ArsipPutusan();
        a.impor(DATA);
        return a;
    }

    private Putusan baru(int nomor, int vonis) {
        String id = String.format("P%03d", nomor);
        return Putusan.dariKolom(new String[] {
            id, nomor + "/Pid.Sus/2030/PN Kpn", "PN Kepanjen", "2030-01-01",
            String.format("T%03d", nomor), "Sabu", "1.5", "Pasal 114 ayat (1)",
            "Pasal 114 ayat (1)", "60", String.valueOf(vonis), "1000", "bukti", "m", "r", "1"
        });
    }

    // ------------------------------------------------------------- impor

    @Test
    void muatLimaPuluhRecord() throws IOException {
        ArsipPutusan a = muat();
        assertEquals(50, a.jumlah());
        a.periksaInvariant();
    }

    @Test
    void recordTidakValidTidakMerusakArsip() {
        ArsipPutusan a = new ArsipPutusan();
        String header = String.join(",", Putusan.namaKolom());
        HasilImpor h = a.imporTeks(header + "\nP001,rusak\n");
        assertEquals(0, h.getDiterima());
        assertEquals(1, h.getDitolak());
        assertEquals(0, a.jumlah());
        a.periksaInvariant();
    }

    // -------------------------------------------------------------- cari

    @Test
    void cariYangAdaDiAwalTengahDanAkhirMelaporkanLangkah() throws IOException {
        ArsipPutusan a = muat();
        HasilCari awal = a.cari("P001");
        assertTrue(awal.ditemukan());
        assertEquals(0, awal.getIndeks());
        assertEquals(1, awal.getLangkah());
        HasilCari tengah = a.cari("P025");
        assertEquals(24, tengah.getIndeks());
        assertEquals(25, tengah.getLangkah());
        HasilCari akhir = a.cari("P050");
        assertEquals(49, akhir.getIndeks());
        assertEquals(50, akhir.getLangkah());
    }

    @Test
    void cariYangTidakAdaMemeriksaSemuaElemen() throws IOException {
        ArsipPutusan a = muat();
        HasilCari h = a.cari("P999");
        assertFalse(h.ditemukan());
        assertNull(h.getPutusan());
        assertEquals(-1, h.getIndeks());
        assertEquals(50, h.getLangkah());
    }

    @Test
    void cariBerdasarkanNomorPerkaraDanTidakPekaHurufBesar() throws IOException {
        ArsipPutusan a = muat();
        String nomor = a.ambil(9).getNomorPerkara();
        HasilCari h = a.cari(nomor.toLowerCase());
        assertTrue(h.ditemukan());
        assertEquals(9, h.getIndeks());
        assertTrue(a.cari("p003").ditemukan());
    }

    // ------------------------------------------------------ tambah, hapus

    @Test
    void hapusPertamaTengahDanTerakhirMenggeserUrutan() throws IOException {
        ArsipPutusan a = muat();
        assertEquals("P001", a.hapus("P001").getCaseId());
        assertEquals(49, a.jumlah());
        assertEquals("P002", a.ambil(0).getCaseId());
        a.periksaInvariant();

        assertEquals("P025", a.hapus("P025").getCaseId());
        assertEquals(48, a.jumlah());
        assertEquals("P024", a.ambil(22).getCaseId());
        assertEquals("P026", a.ambil(23).getCaseId());
        a.periksaInvariant();

        assertEquals("P050", a.hapus("P050").getCaseId());
        assertEquals(47, a.jumlah());
        assertEquals("P049", a.ambil(46).getCaseId());
        a.periksaInvariant();
    }

    @Test
    void hapusYangTidakAdaTidakMengubahArsip() throws IOException {
        ArsipPutusan a = muat();
        assertNull(a.hapus("P999"));
        assertEquals(50, a.jumlah());
        a.periksaInvariant();
    }

    @Test
    void tambahKembaliTigaRecordSetelahHapusMenjagaUrutanDanInvariant() throws IOException {
        ArsipPutusan a = muat();
        a.hapus("P001");
        a.hapus("P025");
        a.hapus("P050");
        assertTrue(a.tambah(baru(101, 10)));
        assertTrue(a.tambah(baru(102, 20)));
        assertTrue(a.tambah(baru(103, 30)));
        assertEquals(50, a.jumlah());
        assertEquals("P101", a.ambil(47).getCaseId());
        assertEquals("P102", a.ambil(48).getCaseId());
        assertEquals("P103", a.ambil(49).getCaseId());
        a.periksaInvariant();
    }

    @Test
    void tambahDuplikatDitolak() throws IOException {
        ArsipPutusan a = muat();
        Putusan duplikat = Putusan.dariKolom(new String[] {
            "P001", "999/Pid.Sus/2030/PN Kpn", "PN Kepanjen", "2030-01-01", "T900", "Sabu",
            "1", "a", "a", "1", "1", "1", "", "", "", ""
        });
        assertFalse(a.tambah(duplikat));
        assertEquals(50, a.jumlah());
    }

    // ------------------------------------------------------------ filter

    @Test
    void filterJenisSamaDenganHitunganManual() throws IOException {
        ArsipPutusan a = muat();
        int manual = 0;
        for (int i = 0; i < a.jumlah(); i++) {
            String[] jenis = a.ambil(i).getJenisNarkotika();
            for (String j : jenis) {
                if (j.equalsIgnoreCase("Ganja")) {
                    manual++;
                    break;
                }
            }
        }
        ArrayDinamis<Putusan> hasil = a.filter(new KriteriaFilter().jenis("ganja"));
        assertEquals(manual, hasil.size());
        assertTrue(manual > 0);
        assertEquals(50, a.jumlah());
    }

    @Test
    void filterGabunganJenisBeratPasalDanVonisSamaDenganPemeriksaanManual() throws IOException {
        ArsipPutusan a = muat();
        KriteriaFilter kr = new KriteriaFilter().jenis("Sabu").berat(1, 100).pasal("114")
                .vonis(60, 120);
        ArrayDinamis<Putusan> hasil = a.filter(kr);
        int manual = 0;
        for (int i = 0; i < a.jumlah(); i++) {
            Putusan p = a.ambil(i);
            boolean sabu = false;
            for (String j : p.getJenisNarkotika()) {
                sabu |= j.equalsIgnoreCase("Sabu");
            }
            boolean pasal = false;
            for (String s : p.getPasalTerbukti()) {
                pasal |= s.contains("114");
            }
            if (sabu && p.getBeratBarangBuktiGram() >= 1 && p.getBeratBarangBuktiGram() <= 100
                    && pasal && p.getVonisBulan() >= 60 && p.getVonisBulan() <= 120) {
                manual++;
            }
        }
        assertEquals(manual, hasil.size());
        for (int i = 0; i < hasil.size(); i++) {
            Putusan p = hasil.get(i);
            assertTrue(p.getVonisBulan() >= 60 && p.getVonisBulan() <= 120);
            assertTrue(p.getBeratBarangBuktiGram() >= 1 && p.getBeratBarangBuktiGram() <= 100);
        }
        hasil.validateInvariant();
    }

    @Test
    void filterTanpaKriteriaMengembalikanSemuaDanUrutanSama() throws IOException {
        ArsipPutusan a = muat();
        ArrayDinamis<Putusan> hasil = a.filter(new KriteriaFilter());
        assertEquals(50, hasil.size());
        assertEquals("P001", hasil.get(0).getCaseId());
        assertEquals("P050", hasil.get(49).getCaseId());
    }

    @Test
    void filterTanpaHasil() throws IOException {
        ArsipPutusan a = muat();
        assertTrue(a.filter(new KriteriaFilter().jenis("TidakAda")).isEmpty());
    }

    // --------------------------------------------------------- agregasi

    @Test
    void agregasiSamaDenganHitunganManual() throws IOException {
        ArsipPutusan a = muat();
        long denda = 0;
        long vonis = 0;
        int sabu = 0;
        for (int i = 0; i < a.jumlah(); i++) {
            Putusan p = a.ambil(i);
            denda += p.getDendaRupiah();
            vonis += p.getVonisBulan();
            for (String j : p.getJenisNarkotika()) {
                if (j.equalsIgnoreCase("Sabu")) {
                    sabu++;
                }
            }
        }
        Agregat g = a.agregasi();
        assertEquals(50, g.getJumlahPerkara());
        assertEquals(denda, g.getTotalDenda());
        assertEquals(vonis / 50.0, g.getRataRataVonisBulan(), 1e-9);
        int cacahSabu = -1;
        for (int i = 0; i < g.jumlahJenisBerbeda(); i++) {
            if (g.getNamaJenis(i).equalsIgnoreCase("Sabu")) {
                cacahSabu = g.getJumlahJenis(i);
            }
        }
        assertEquals(sabu, cacahSabu);
    }

    @Test
    void agregasiPadaArsipKosongAdalahNol() {
        Agregat g = new ArsipPutusan().agregasi();
        assertEquals(0, g.getJumlahPerkara());
        assertEquals(0.0, g.getRataRataVonisBulan(), 0.0);
        assertEquals(0L, g.getTotalDenda());
        assertEquals(0, g.jumlahJenisBerbeda());
    }

    // ------------------------------------------------------------ urutan

    @Test
    void urutMenaikDanMenurunBenarDanArsipAsliTidakBerubah() throws IOException {
        ArsipPutusan a = muat();
        HasilUrut naik = a.urutkanBerdasarkanVonis(true);
        HasilUrut turun = a.urutkanBerdasarkanVonis(false);
        assertEquals(50, naik.getData().size());
        for (int i = 1; i < 50; i++) {
            assertTrue(naik.getData().get(i - 1).getVonisBulan()
                    <= naik.getData().get(i).getVonisBulan());
            assertTrue(turun.getData().get(i - 1).getVonisBulan()
                    >= turun.getData().get(i).getVonisBulan());
        }
        assertEquals("P001", a.ambil(0).getCaseId());
        assertEquals("P050", a.ambil(49).getCaseId());
        a.periksaInvariant();
    }

    @Test
    void urutStabilVonisSamaTetapBerurutanSepertiAsli() throws IOException {
        ArsipPutusan a = muat();
        ArrayDinamis<Putusan> naik = a.urutkanBerdasarkanVonis(true).getData();
        for (int i = 1; i < naik.size(); i++) {
            Putusan sebelum = naik.get(i - 1);
            Putusan sesudah = naik.get(i);
            if (sebelum.getVonisBulan() == sesudah.getVonisBulan()) {
                assertTrue(sebelum.getCaseId().compareTo(sesudah.getCaseId()) < 0);
            }
        }
    }

    @Test
    void urutData_yangSudahTerurutHanyaPerluNMinSatuPerbandinganTanpaPergeseran()
            throws IOException {
        ArsipPutusan a = muat();
        ArsipPutusan terurut = new ArsipPutusan();
        ArrayDinamis<Putusan> data = a.urutkanBerdasarkanVonis(true).getData();
        for (int i = 0; i < data.size(); i++) {
            terurut.tambah(data.get(i));
        }
        HasilUrut lagi = terurut.urutkanBerdasarkanVonis(true);
        assertEquals(49, lagi.getPerbandingan());
        assertEquals(0, lagi.getPergeseran());
    }

    @Test
    void urutArsipKosongDanSatuElemen() {
        ArsipPutusan a = new ArsipPutusan();
        assertEquals(0, a.urutkanBerdasarkanVonis(true).getData().size());
        a.tambah(baru(1, 5));
        HasilUrut h = a.urutkanBerdasarkanVonis(true);
        assertEquals(1, h.getData().size());
        assertEquals(0, h.getPerbandingan());
    }

    // ------------------------------------------------------------ ekspor

    @Test
    void eksporLaluImporKembaliMenghasilkanDataSama() throws IOException {
        ArsipPutusan a = muat();
        String teks = EksporCsv.keTeks(a.semua());
        ArsipPutusan b = new ArsipPutusan();
        HasilImpor h = b.imporTeks(teks);
        assertEquals(50, h.getDiterima(), h.getPesanDitolak().isEmpty()
                ? "" : h.getPesanDitolak().get(0));
        assertEquals(0, h.getDitolak());
        for (int i = 0; i < 50; i++) {
            Putusan x = a.ambil(i);
            Putusan y = b.ambil(i);
            assertEquals(x.getCaseId(), y.getCaseId());
            assertEquals(x.getBeratBarangBuktiGram(), y.getBeratBarangBuktiGram(), 0.0);
            assertEquals(x.getDendaRupiah(), y.getDendaRupiah());
            assertEquals(x.getBarangBukti(), y.getBarangBukti());
            assertEquals(x.getTanggalPutusan(), y.getTanggalPutusan());
        }
    }

    @Test
    void eksporKeBerkasMenulisHeaderDanSatuBarisPerPutusan() throws IOException {
        ArsipPutusan a = muat();
        Path sementara = Files.createTempFile("ekspor-uji", ".csv");
        try {
            a.ekspor(sementara, a.filter(new KriteriaFilter().jenis("Ganja")));
            String isi = Files.readString(sementara);
            assertTrue(isi.startsWith("caseId,nomorPerkara,"));
            int baris = isi.split("\n").length;
            assertEquals(1 + a.filter(new KriteriaFilter().jenis("Ganja")).size(), baris);
        } finally {
            Files.deleteIfExists(sementara);
        }
    }

    // ------------------------------------------------------------- audit

    @Test
    void riwayatAuditMenaruhOperasiTerbaruDiPuncak() throws IOException {
        ArsipPutusan a = muat();
        a.cari("P001");
        a.hapus("P001");
        assertEquals(3, a.getRiwayat().size());
        assertTrue(a.getRiwayat().peek().startsWith("#3 HAPUS P001"));
        ArrayDinamis<String> r = a.riwayatTerbaruDulu();
        assertTrue(r.get(0).startsWith("#3 HAPUS"));
        assertTrue(r.get(1).startsWith("#2 CARI"));
        assertTrue(r.get(2).startsWith("#1 IMPOR"));
        a.periksaInvariant();
    }

    @Test
    void membacaRiwayatTidakMengubahUrutanStack() throws IOException {
        ArsipPutusan a = muat();
        a.cari("P002");
        int sebelum = a.getRiwayat().size();
        a.riwayatTerbaruDulu();
        a.riwayatTerbaruDulu();
        assertEquals(sebelum, a.getRiwayat().size());
        assertTrue(a.getRiwayat().peek().startsWith("#2 CARI"));
    }
}
