package id.ac.umm.strukdat.impor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import id.ac.umm.strukdat.impor.ImporPutusan.HasilImpor;
import id.ac.umm.strukdat.model.DataTidakValidException;
import id.ac.umm.strukdat.model.Putusan;
import id.ac.umm.strukdat.struktur.ArrayDinamis;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ImporPutusanTest {

    private static final String HEADER = String.join(",", Putusan.namaKolom());

    /** Satu baris data valid dengan caseId dan nomor perkara yang bisa diganti. */
    private String baris(String caseId, String nomor) {
        return caseId + "," + nomor + ",PN Kepanjen,2026-01-05," + "T" + caseId.substring(1)
                + ",Sabu,1.5,Pasal 114 ayat (1),Pasal 114 ayat (1),60,48,800000000,"
                + "\"1 HP, 1 pipet\",Memberatkan,Meringankan,1|2";
    }

    @Test
    void duaRecordValidMasukKeStruktur() {
        String csv = HEADER + "\n" + baris("P001", "1/Pid.Sus/2025/PN Kpn") + "\n"
                + baris("P002", "2/Pid.Sus/2025/PN Kpn") + "\n";
        ArrayDinamis<Putusan> tujuan = new ArrayDinamis<>();
        HasilImpor h = ImporPutusan.dariTeks(csv, tujuan);
        assertEquals(2, h.getDiterima());
        assertEquals(0, h.getDitolak());
        assertEquals(2, tujuan.size());
        assertEquals("P001", tujuan.get(0).getCaseId());
        assertEquals("1 HP, 1 pipet", tujuan.get(0).getBarangBukti());
        tujuan.validateInvariant();
    }

    @Test
    void recordTidakValidDitolakTanpaMenghentikanImpor() {
        String rusak = "P002,2/Pid.Sus/2025/PN Kpn,PN Kepanjen,bukan-tanggal,T002,Sabu,1,"
                + "a,a,1,1,1,b,c,d,1";
        String csv = HEADER + "\n" + baris("P001", "1/Pid.Sus/2025/PN Kpn") + "\n"
                + rusak + "\n" + baris("P003", "3/Pid.Sus/2025/PN Kpn") + "\n";
        ArrayDinamis<Putusan> tujuan = new ArrayDinamis<>();
        HasilImpor h = ImporPutusan.dariTeks(csv, tujuan);
        assertEquals(2, h.getDiterima());
        assertEquals(1, h.getDitolak());
        assertEquals(2, tujuan.size());
        assertEquals("P003", tujuan.get(1).getCaseId());
        String pesan = h.getPesanDitolak().get(0);
        assertTrue(pesan.contains("Baris 3"), pesan);
        assertTrue(pesan.contains("tanggalPutusan"), pesan);
        tujuan.validateInvariant();
    }

    @Test
    void barisDenganJumlahKolomSalahDitolak() {
        String csv = HEADER + "\n" + "P001,hanya,tiga\n";
        ArrayDinamis<Putusan> tujuan = new ArrayDinamis<>();
        HasilImpor h = ImporPutusan.dariTeks(csv, tujuan);
        assertEquals(0, h.getDiterima());
        assertEquals(1, h.getDitolak());
        assertTrue(tujuan.isEmpty());
        assertTrue(h.getPesanDitolak().get(0).contains("Jumlah kolom"));
    }

    @Test
    void caseIdAtauNomorPerkaraDuplikatDitolak() {
        String csv = HEADER + "\n" + baris("P001", "1/Pid.Sus/2025/PN Kpn") + "\n"
                + baris("P001", "9/Pid.Sus/2025/PN Kpn") + "\n"
                + baris("P005", "1/Pid.Sus/2025/PN Kpn") + "\n";
        ArrayDinamis<Putusan> tujuan = new ArrayDinamis<>();
        HasilImpor h = ImporPutusan.dariTeks(csv, tujuan);
        assertEquals(1, h.getDiterima());
        assertEquals(2, h.getDitolak());
        assertTrue(h.getPesanDitolak().get(0).contains("caseId P001 sudah ada"));
        assertTrue(h.getPesanDitolak().get(1).contains("nomor perkara"));
        tujuan.validateInvariant();
    }

    @Test
    void headerSalahMenghentikanImporDanStrukturTidakBerubah() {
        ArrayDinamis<Putusan> tujuan = new ArrayDinamis<>();
        String csv = "caseId,nomor,x\nP001,1,2\n";
        assertThrows(DataTidakValidException.class, () -> ImporPutusan.dariTeks(csv, tujuan));
        assertTrue(tujuan.isEmpty());
    }

    @Test
    void namaKolomBerbedaDiHeaderDitolak() {
        String headerSalah = HEADER.replace("vonisBulan", "vonis");
        ArrayDinamis<Putusan> tujuan = new ArrayDinamis<>();
        DataTidakValidException e = assertThrows(DataTidakValidException.class,
                () -> ImporPutusan.dariTeks(headerSalah + "\n", tujuan));
        assertTrue(e.getMessage().contains("vonisBulan"));
    }

    @Test
    void berkasKosongMelemparKesalahan() {
        assertThrows(DataTidakValidException.class,
                () -> ImporPutusan.dariTeks("", new ArrayDinamis<>()));
    }

    @Test
    void hanyaHeaderTanpaDataMenghasilkanNolRecord() {
        ArrayDinamis<Putusan> tujuan = new ArrayDinamis<>();
        HasilImpor h = ImporPutusan.dariTeks(HEADER + "\n", tujuan);
        assertEquals(0, h.getDiterima());
        assertEquals(0, h.getDitolak());
        assertTrue(tujuan.isEmpty());
    }

    // ------------------------------------------------- berkas data sebenarnya

    private static final Path DATA = Path.of("data", "putusan-50.csv");

    @Test
    void muatLimaPuluhRecordAsliDanBuktikanSizeLimaPuluh() throws IOException {
        assumeTrue(Files.exists(DATA), "data/putusan-50.csv tidak ditemukan dari folder kerja");
        ArrayDinamis<Putusan> tujuan = new ArrayDinamis<>();
        HasilImpor h = ImporPutusan.dariFile(DATA, tujuan);
        assertEquals(50, h.getDiterima(), h.getPesanDitolak().isEmpty()
                ? "" : h.getPesanDitolak().get(0));
        assertEquals(0, h.getDitolak());
        assertEquals(50, tujuan.size());
        assertEquals("P001", tujuan.get(0).getCaseId());
        assertEquals("P050", tujuan.get(49).getCaseId());
        tujuan.validateInvariant();
    }

    @Test
    void mengimporDuaKaliBerkasYangSamaMenolakSemuaDuplikat() throws IOException {
        assumeTrue(Files.exists(DATA), "data/putusan-50.csv tidak ditemukan dari folder kerja");
        ArrayDinamis<Putusan> tujuan = new ArrayDinamis<>();
        ImporPutusan.dariFile(DATA, tujuan);
        HasilImpor kedua = ImporPutusan.dariFile(DATA, tujuan);
        assertEquals(0, kedua.getDiterima());
        assertEquals(50, kedua.getDitolak());
        assertEquals(50, tujuan.size());
        tujuan.validateInvariant();
    }

    @Test
    void semuaRecordAsliMemakaiPengadilanKepanjenDanKodeSamaranTerdakwa() throws IOException {
        assumeTrue(Files.exists(DATA), "data/putusan-50.csv tidak ditemukan dari folder kerja");
        ArrayDinamis<Putusan> tujuan = new ArrayDinamis<>();
        ImporPutusan.dariFile(DATA, tujuan);
        for (int i = 0; i < tujuan.size(); i++) {
            Putusan p = tujuan.get(i);
            assertEquals("PN Kepanjen", p.getPengadilan());
            assertTrue(p.getTerdakwaKode().matches("T\\d{3}"));
        }
    }
}
