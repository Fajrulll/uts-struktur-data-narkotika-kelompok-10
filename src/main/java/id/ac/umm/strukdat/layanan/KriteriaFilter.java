package id.ac.umm.strukdat.layanan;

import id.ac.umm.strukdat.model.Putusan;

/**
 * Kriteria filter putusan. Semua kriteria yang diisi harus terpenuhi sekaligus
 * (AND). Kriteria yang tidak diisi tidak membatasi apa pun.
 */
public final class KriteriaFilter {

    private String jenis;
    private double beratMin = 0;
    private double beratMaks = Double.MAX_VALUE;
    private String pasal;
    private int vonisMin = 0;
    private int vonisMaks = Integer.MAX_VALUE;

    /** Jenis narkotika, misalnya "Sabu" (tidak peka huruf besar-kecil). */
    public KriteriaFilter jenis(String jenis) {
        this.jenis = jenis == null || jenis.isBlank() ? null : jenis.trim();
        return this;
    }

    /** Rentang berat barang bukti dalam gram, batas atas dan bawah termasuk. */
    public KriteriaFilter berat(double min, double maks) {
        this.beratMin = min;
        this.beratMaks = maks;
        return this;
    }

    /** Potongan nama pasal terbukti, misalnya "114" atau "Pasal 132". */
    public KriteriaFilter pasal(String pasal) {
        this.pasal = pasal == null || pasal.isBlank() ? null : pasal.trim();
        return this;
    }

    /** Rentang vonis dalam bulan, batas atas dan bawah termasuk. */
    public KriteriaFilter vonis(int min, int maks) {
        this.vonisMin = min;
        this.vonisMaks = maks;
        return this;
    }

    /** True jika putusan memenuhi semua kriteria yang diisi. */
    public boolean cocok(Putusan p) {
        if (jenis != null && !punyaJenis(p)) {
            return false;
        }
        double berat = p.getBeratBarangBuktiGram();
        if (berat < beratMin || berat > beratMaks) {
            return false;
        }
        if (pasal != null && !punyaPasal(p)) {
            return false;
        }
        int vonis = p.getVonisBulan();
        return vonis >= vonisMin && vonis <= vonisMaks;
    }

    private boolean punyaJenis(Putusan p) {
        String[] jenisPutusan = p.getJenisNarkotika();
        for (int i = 0; i < jenisPutusan.length; i++) {
            if (jenisPutusan[i].equalsIgnoreCase(jenis)) {
                return true;
            }
        }
        return false;
    }

    private boolean punyaPasal(Putusan p) {
        String cari = pasal.toLowerCase();
        String[] pasalPutusan = p.getPasalTerbukti();
        for (int i = 0; i < pasalPutusan.length; i++) {
            if (pasalPutusan[i].toLowerCase().contains(cari)) {
                return true;
            }
        }
        return false;
    }
}
