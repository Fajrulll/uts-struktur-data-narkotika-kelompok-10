package id.ac.umm.strukdat.model;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Satu putusan perkara narkotika. Objek ini tidak bisa diubah setelah dibuat
 * (immutable) dan hanya bisa dibuat lewat {@link #dariKolom(String[])}, yang
 * memvalidasi semua kolom CSV.
 *
 * <p>Dua putusan dianggap sama jika caseId-nya sama.
 */
public final class Putusan {

    /** Urutan kolom pada data/putusan-50.csv. */
    private static final String[] NAMA_KOLOM = {
        "caseId", "nomorPerkara", "pengadilan", "tanggalPutusan", "terdakwaKode",
        "jenisNarkotika", "beratBarangBuktiGram", "pasalDakwaan", "pasalTerbukti",
        "tuntutanBulan", "vonisBulan", "dendaRupiah", "barangBukti",
        "keadaanMemberatkan", "keadaanMeringankan", "sourcePages"
    };

    public static final int JUMLAH_KOLOM = NAMA_KOLOM.length;

    private final String caseId;
    private final String nomorPerkara;
    private final String pengadilan;
    private final LocalDate tanggalPutusan;
    private final String terdakwaKode;
    private final String[] jenisNarkotika;
    private final double beratBarangBuktiGram;
    private final String[] pasalDakwaan;
    private final String[] pasalTerbukti;
    private final int tuntutanBulan;
    private final int vonisBulan;
    private final long dendaRupiah;
    private final String barangBukti;
    private final String keadaanMemberatkan;
    private final String keadaanMeringankan;
    private final String sourcePages;

    private Putusan(String[] k) {
        caseId = wajib(k, 0);
        if (!caseId.matches("P\\d{3}")) {
            throw gagal(0, "harus berformat P diikuti 3 angka, ditemukan '" + caseId + "'");
        }
        nomorPerkara = wajib(k, 1);
        if (!nomorPerkara.matches("\\d+/Pid\\.Sus/\\d{4}/PN .+")) {
            throw gagal(1, "tidak berformat nomor perkara (contoh 132/Pid.Sus/2026/PN Kpn): '"
                    + nomorPerkara + "'");
        }
        pengadilan = wajib(k, 2);
        try {
            tanggalPutusan = LocalDate.parse(wajib(k, 3));
        } catch (DateTimeParseException e) {
            throw gagal(3, "bukan tanggal YYYY-MM-DD yang valid: '" + k[3] + "'");
        }
        terdakwaKode = wajib(k, 4);
        if (!terdakwaKode.matches("T\\d{3}")) {
            throw gagal(4, "harus berformat T diikuti 3 angka, ditemukan '" + terdakwaKode + "'");
        }
        jenisNarkotika = pisah(k[5]);
        beratBarangBuktiGram = desimalTakNegatif(k, 6);
        pasalDakwaan = pisah(k[7]);
        pasalTerbukti = pisah(k[8]);
        tuntutanBulan = bulat(k, 9);
        vonisBulan = bulat(k, 10);
        dendaRupiah = panjang(k, 11);
        barangBukti = opsional(k, 12);
        keadaanMemberatkan = opsional(k, 13);
        keadaanMeringankan = opsional(k, 14);
        sourcePages = opsional(k, 15);
    }

    /**
     * Membuat Putusan dari satu baris CSV (16 kolom, urutan sesuai header).
     *
     * @throws DataTidakValidException jika ada kolom yang tidak valid
     */
    public static Putusan dariKolom(String[] kolom) {
        if (kolom == null || kolom.length != JUMLAH_KOLOM) {
            int n = kolom == null ? 0 : kolom.length;
            throw new DataTidakValidException(
                    "Jumlah kolom " + n + ", seharusnya " + JUMLAH_KOLOM);
        }
        return new Putusan(kolom);
    }

    /** Salinan nama kolom sesuai header CSV. */
    public static String[] namaKolom() {
        return NAMA_KOLOM.clone();
    }

    // ---------------------------------------------------------------- getter

    public String getCaseId() {
        return caseId;
    }

    public String getNomorPerkara() {
        return nomorPerkara;
    }

    public String getPengadilan() {
        return pengadilan;
    }

    public LocalDate getTanggalPutusan() {
        return tanggalPutusan;
    }

    public String getTerdakwaKode() {
        return terdakwaKode;
    }

    public String[] getJenisNarkotika() {
        return jenisNarkotika.clone();
    }

    public double getBeratBarangBuktiGram() {
        return beratBarangBuktiGram;
    }

    public String[] getPasalDakwaan() {
        return pasalDakwaan.clone();
    }

    public String[] getPasalTerbukti() {
        return pasalTerbukti.clone();
    }

    public int getTuntutanBulan() {
        return tuntutanBulan;
    }

    public int getVonisBulan() {
        return vonisBulan;
    }

    public long getDendaRupiah() {
        return dendaRupiah;
    }

    public String getBarangBukti() {
        return barangBukti;
    }

    public String getKeadaanMemberatkan() {
        return keadaanMemberatkan;
    }

    public String getKeadaanMeringankan() {
        return keadaanMeringankan;
    }

    public String getSourcePages() {
        return sourcePages;
    }

    /** Ringkasan satu baris untuk tampilan daftar. */
    public String ringkasan() {
        return caseId + " | " + nomorPerkara + " | " + tanggalPutusan + " | "
                + String.join("|", jenisNarkotika) + " | vonis " + vonisBulan + " bln";
    }

    // ------------------------------------------------------- equals/hashCode

    @Override
    public boolean equals(Object lain) {
        if (this == lain) {
            return true;
        }
        if (!(lain instanceof Putusan)) {
            return false;
        }
        return caseId.equals(((Putusan) lain).caseId);
    }

    @Override
    public int hashCode() {
        return caseId.hashCode();
    }

    @Override
    public String toString() {
        return ringkasan();
    }

    // ------------------------------------------------------------- validasi

    private static DataTidakValidException gagal(int kolom, String alasan) {
        return new DataTidakValidException("Kolom " + NAMA_KOLOM[kolom] + " " + alasan);
    }

    private static String wajib(String[] k, int i) {
        String v = k[i] == null ? "" : k[i].trim();
        if (v.isEmpty()) {
            throw gagal(i, "tidak boleh kosong");
        }
        return v;
    }

    private static String opsional(String[] k, int i) {
        return k[i] == null ? "" : k[i].trim();
    }

    /** Memecah nilai yang dipisah "|" menjadi larik. Nilai kosong menjadi larik kosong. */
    private static String[] pisah(String nilai) {
        String v = nilai == null ? "" : nilai.trim();
        if (v.isEmpty()) {
            return new String[0];
        }
        String[] bagian = v.split("\\|");
        for (int i = 0; i < bagian.length; i++) {
            bagian[i] = bagian[i].trim();
        }
        return bagian;
    }

    private static double desimalTakNegatif(String[] k, int i) {
        String v = wajib(k, i);
        if (!v.matches("\\d+(\\.\\d+)?")) {
            throw gagal(i, "harus angka desimal tidak negatif, ditemukan '" + v + "'");
        }
        double d = Double.parseDouble(v);
        if (!Double.isFinite(d)) {
            throw gagal(i, "terlalu besar: '" + v + "'");
        }
        return d;
    }

    private static int bulat(String[] k, int i) {
        String v = wajib(k, i);
        if (!v.matches("\\d+")) {
            throw gagal(i, "harus bilangan bulat tidak negatif, ditemukan '" + v + "'");
        }
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            throw gagal(i, "terlalu besar: '" + v + "'");
        }
    }

    private static long panjang(String[] k, int i) {
        String v = wajib(k, i);
        if (!v.matches("\\d+")) {
            throw gagal(i, "harus bilangan bulat tidak negatif, ditemukan '" + v + "'");
        }
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            throw gagal(i, "terlalu besar: '" + v + "'");
        }
    }
}
