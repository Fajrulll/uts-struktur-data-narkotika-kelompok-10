package id.ac.umm.strukdat.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PutusanTest {

    /** Satu baris valid; indeks sesuai urutan kolom CSV. */
    private String[] valid() {
        return new String[] {
            "P001", "419/Pid.Sus/2025/PN Kpn", "PN Kepanjen", "2026-01-05", "T001",
            "Sabu|Ganja", "6393.61", "Pasal 114 ayat (2)|Pasal 132 ayat (1)",
            "Pasal 114 ayat (2)", "168", "168", "450000000",
            "1 HP merk iPhone", "Meresahkan masyarakat", "Mengakui perbuatan", "1|2|3"
        };
    }

    private DataTidakValidException gagalDengan(int kolom, String nilai) {
        String[] k = valid();
        k[kolom] = nilai;
        return assertThrows(DataTidakValidException.class, () -> Putusan.dariKolom(k));
    }

    @Test
    void barisValidDibuatDenganSemuaNilaiBenar() {
        Putusan p = Putusan.dariKolom(valid());
        assertEquals("P001", p.getCaseId());
        assertEquals("419/Pid.Sus/2025/PN Kpn", p.getNomorPerkara());
        assertEquals("PN Kepanjen", p.getPengadilan());
        assertEquals(LocalDate.of(2026, 1, 5), p.getTanggalPutusan());
        assertEquals("T001", p.getTerdakwaKode());
        assertArrayEquals(new String[] {"Sabu", "Ganja"}, p.getJenisNarkotika());
        assertEquals(6393.61, p.getBeratBarangBuktiGram(), 1e-9);
        assertArrayEquals(new String[] {"Pasal 114 ayat (2)", "Pasal 132 ayat (1)"},
                p.getPasalDakwaan());
        assertArrayEquals(new String[] {"Pasal 114 ayat (2)"}, p.getPasalTerbukti());
        assertEquals(168, p.getTuntutanBulan());
        assertEquals(168, p.getVonisBulan());
        assertEquals(450_000_000L, p.getDendaRupiah());
        assertEquals("1|2|3", p.getSourcePages());
    }

    @Test
    void jumlahKolomSalahDitolak() {
        assertThrows(DataTidakValidException.class, () -> Putusan.dariKolom(new String[] {"P001"}));
        assertThrows(DataTidakValidException.class, () -> Putusan.dariKolom(null));
    }

    @Test
    void caseIdHarusBerformat() {
        assertTrue(gagalDengan(0, "X001").getMessage().contains("caseId"));
        assertTrue(gagalDengan(0, "P1").getMessage().contains("caseId"));
        assertTrue(gagalDengan(0, "").getMessage().contains("caseId"));
    }

    @Test
    void nomorPerkaraHarusBerformat() {
        assertTrue(gagalDengan(1, "abc").getMessage().contains("nomorPerkara"));
        assertTrue(gagalDengan(1, "").getMessage().contains("nomorPerkara"));
    }

    @Test
    void tanggalHarusValid() {
        assertTrue(gagalDengan(3, "05-01-2026").getMessage().contains("tanggalPutusan"));
        assertTrue(gagalDengan(3, "2026-02-30").getMessage().contains("tanggalPutusan"));
        assertTrue(gagalDengan(3, "").getMessage().contains("tanggalPutusan"));
    }

    @Test
    void kodeTerdakwaHarusBerformat() {
        assertTrue(gagalDengan(4, "Budi").getMessage().contains("terdakwaKode"));
    }

    @Test
    void beratTidakBolehNegatifBukanAngkaAtauTakHingga() {
        assertTrue(gagalDengan(6, "-1").getMessage().contains("beratBarangBuktiGram"));
        assertTrue(gagalDengan(6, "abc").getMessage().contains("beratBarangBuktiGram"));
        assertTrue(gagalDengan(6, "NaN").getMessage().contains("beratBarangBuktiGram"));
        assertTrue(gagalDengan(6, "5d").getMessage().contains("beratBarangBuktiGram"));
        assertTrue(gagalDengan(6, "").getMessage().contains("beratBarangBuktiGram"));
    }

    @Test
    void beratNolDiterima() {
        String[] k = valid();
        k[6] = "0";
        assertEquals(0.0, Putusan.dariKolom(k).getBeratBarangBuktiGram(), 0.0);
    }

    @Test
    void bulanDanDendaHarusBilanganBulatTakNegatif() {
        assertTrue(gagalDengan(9, "-5").getMessage().contains("tuntutanBulan"));
        assertTrue(gagalDengan(10, "1.5").getMessage().contains("vonisBulan"));
        assertTrue(gagalDengan(11, "abc").getMessage().contains("dendaRupiah"));
        assertTrue(gagalDengan(10, "99999999999").getMessage().contains("vonisBulan"));
    }

    @Test
    void kolomOpsionalBolehKosong() {
        String[] k = valid();
        k[5] = "";
        k[7] = "";
        k[8] = "";
        k[12] = "";
        k[13] = "";
        k[14] = "";
        k[15] = "";
        Putusan p = Putusan.dariKolom(k);
        assertEquals(0, p.getJenisNarkotika().length);
        assertEquals(0, p.getPasalDakwaan().length);
        assertEquals("", p.getBarangBukti());
    }

    @Test
    void larikYangDikembalikanTidakMengubahObjek() {
        Putusan p = Putusan.dariKolom(valid());
        String[] jenis = p.getJenisNarkotika();
        jenis[0] = "DIUBAH";
        assertEquals("Sabu", p.getJenisNarkotika()[0]);
    }

    @Test
    void perubahanLarikMasukanTidakMengubahObjek() {
        String[] k = valid();
        Putusan p = Putusan.dariKolom(k);
        k[0] = "P999";
        assertEquals("P001", p.getCaseId());
    }

    @Test
    void samaBerarti_caseIdSama() {
        Putusan a = Putusan.dariKolom(valid());
        String[] k = valid();
        k[1] = "420/Pid.Sus/2025/PN Kpn";
        Putusan b = Putusan.dariKolom(k);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        k[0] = "P002";
        assertFalse(a.equals(Putusan.dariKolom(k)));
    }

    @Test
    void ringkasanMemuatCaseIdDanVonis() {
        String r = Putusan.dariKolom(valid()).ringkasan();
        assertTrue(r.contains("P001"));
        assertTrue(r.contains("vonis 168 bln"));
    }

    @Test
    void namaKolomBerjumlahEnamBelas() {
        assertEquals(16, Putusan.namaKolom().length);
        assertEquals("caseId", Putusan.namaKolom()[0]);
        assertEquals("sourcePages", Putusan.namaKolom()[15]);
    }
}
