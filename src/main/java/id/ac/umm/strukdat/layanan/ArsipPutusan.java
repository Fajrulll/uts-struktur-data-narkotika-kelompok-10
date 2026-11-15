package id.ac.umm.strukdat.layanan;

import id.ac.umm.strukdat.impor.EksporCsv;
import id.ac.umm.strukdat.impor.ImporPutusan;
import id.ac.umm.strukdat.impor.ImporPutusan.HasilImpor;
import id.ac.umm.strukdat.model.Putusan;
import id.ac.umm.strukdat.struktur.ArrayDinamis;
import id.ac.umm.strukdat.struktur.Stack;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Pusat semua fitur aplikasi. Semua putusan disimpan di struktur utama
 * ({@link ArrayDinamis}), dan setiap operasi pengguna dicatat di struktur
 * sekunder ({@link Stack}) sebagai riwayat audit.
 */
public class ArsipPutusan {

    private final ArrayDinamis<Putusan> arsip = new ArrayDinamis<>();
    private final Stack<String> riwayat = new Stack<>();

    // ------------------------------------------------------------- hasil

    /** Hasil pencarian satu putusan beserta jumlah langkah (perbandingan). */
    public static final class HasilCari {
        private final Putusan putusan;
        private final int indeks;
        private final int langkah;

        private HasilCari(Putusan putusan, int indeks, int langkah) {
            this.putusan = putusan;
            this.indeks = indeks;
            this.langkah = langkah;
        }

        public boolean ditemukan() {
            return putusan != null;
        }

        /** Putusan yang ditemukan, atau null. */
        public Putusan getPutusan() {
            return putusan;
        }

        /** Posisi di struktur utama, atau -1 jika tidak ditemukan. */
        public int getIndeks() {
            return indeks;
        }

        public int getLangkah() {
            return langkah;
        }
    }

    /** Hasil pengurutan beserta pencacah operasi. */
    public static final class HasilUrut {
        private final ArrayDinamis<Putusan> data;
        private final int perbandingan;
        private final int pergeseran;

        private HasilUrut(ArrayDinamis<Putusan> data, int perbandingan, int pergeseran) {
            this.data = data;
            this.perbandingan = perbandingan;
            this.pergeseran = pergeseran;
        }

        public ArrayDinamis<Putusan> getData() {
            return data;
        }

        public int getPerbandingan() {
            return perbandingan;
        }

        public int getPergeseran() {
            return pergeseran;
        }
    }

    /** Ringkasan agregasi. Satu putusan dengan dua jenis dihitung di kedua jenis. */
    public static final class Agregat {
        private int jumlahPerkara;
        private double rataRataVonisBulan;
        private long totalDenda;
        private final ArrayDinamis<String> namaJenis = new ArrayDinamis<>();
        private final ArrayDinamis<Integer> jumlahPerJenis = new ArrayDinamis<>();

        public int getJumlahPerkara() {
            return jumlahPerkara;
        }

        public double getRataRataVonisBulan() {
            return rataRataVonisBulan;
        }

        public long getTotalDenda() {
            return totalDenda;
        }

        public int jumlahJenisBerbeda() {
            return namaJenis.size();
        }

        public String getNamaJenis(int posisi) {
            return namaJenis.get(posisi);
        }

        public int getJumlahJenis(int posisi) {
            return jumlahPerJenis.get(posisi);
        }

        private void tambahJenis(String jenis) {
            for (int i = 0; i < namaJenis.size(); i++) {
                if (namaJenis.get(i).equalsIgnoreCase(jenis)) {
                    jumlahPerJenis.set(i, jumlahPerJenis.get(i) + 1);
                    return;
                }
            }
            namaJenis.add(jenis);
            jumlahPerJenis.add(1);
        }
    }

    // -------------------------------------------------------- akses dasar

    public int jumlah() {
        return arsip.size();
    }

    public Putusan ambil(int posisi) {
        return arsip.get(posisi);
    }

    /** Struktur utama (hanya untuk dibaca; ubah lewat tambah/hapus agar tercatat). */
    public ArrayDinamis<Putusan> semua() {
        return arsip;
    }

    public Stack<String> getRiwayat() {
        return riwayat;
    }

    public void periksaInvariant() {
        arsip.validateInvariant();
        riwayat.validateInvariant();
    }

    // -------------------------------------------------------------- impor

    public HasilImpor impor(Path berkas) throws IOException {
        HasilImpor h = ImporPutusan.dariFile(berkas, arsip);
        catat("IMPOR " + berkas + ": " + h.getDiterima() + " diterima, "
                + h.getDitolak() + " ditolak");
        return h;
    }

    public HasilImpor imporTeks(String teks) {
        HasilImpor h = ImporPutusan.dariTeks(teks, arsip);
        catat("IMPOR teks: " + h.getDiterima() + " diterima, " + h.getDitolak() + " ditolak");
        return h;
    }

    // ------------------------------------------------------ tambah dan hapus

    /** Menambah putusan di akhir. Ditolak (false) jika caseId atau nomor perkara sudah ada. */
    public boolean tambah(Putusan p) {
        for (int i = 0; i < arsip.size(); i++) {
            Putusan ada = arsip.get(i);
            if (ada.getCaseId().equals(p.getCaseId())
                    || ada.getNomorPerkara().equals(p.getNomorPerkara())) {
                catat("TAMBAH " + p.getCaseId() + " ditolak: duplikat");
                return false;
            }
        }
        arsip.add(p);
        catat("TAMBAH " + p.getCaseId());
        return true;
    }

    /** Menghapus putusan berdasarkan caseId. Mengembalikan yang dihapus, atau null. */
    public Putusan hapus(String caseId) {
        HasilCari h = cariTanpaCatat(caseId);
        if (!h.ditemukan()) {
            catat("HAPUS " + caseId + " gagal: tidak ditemukan");
            return null;
        }
        Putusan dihapus = arsip.remove(h.getIndeks());
        catat("HAPUS " + caseId + " (pergeseran " + arsip.getLangkahTerakhir() + " elemen)");
        return dihapus;
    }

    // --------------------------------------------------------------- cari

    /** Mencari berdasarkan caseId atau nomor perkara (persis, tidak peka huruf). */
    public HasilCari cari(String kunci) {
        HasilCari h = cariTanpaCatat(kunci);
        catat("CARI " + kunci + (h.ditemukan() ? " ditemukan di posisi " + h.getIndeks()
                : " tidak ditemukan") + " (" + h.getLangkah() + " langkah)");
        return h;
    }

    private HasilCari cariTanpaCatat(String kunci) {
        String k = kunci == null ? "" : kunci.trim();
        int langkah = 0;
        for (int i = 0; i < arsip.size(); i++) {
            langkah++;
            Putusan p = arsip.get(i);
            if (p.getCaseId().equalsIgnoreCase(k) || p.getNomorPerkara().equalsIgnoreCase(k)) {
                return new HasilCari(p, i, langkah);
            }
        }
        return new HasilCari(null, -1, langkah);
    }

    // ------------------------------------------------------------- filter

    /** Mengembalikan putusan yang cocok, urutannya sama seperti di struktur utama. */
    public ArrayDinamis<Putusan> filter(KriteriaFilter kriteria) {
        ArrayDinamis<Putusan> hasil = new ArrayDinamis<>();
        for (int i = 0; i < arsip.size(); i++) {
            Putusan p = arsip.get(i);
            if (kriteria.cocok(p)) {
                hasil.add(p);
            }
        }
        catat("FILTER menghasilkan " + hasil.size() + " putusan");
        return hasil;
    }

    // ----------------------------------------------------------- agregasi

    public Agregat agregasi() {
        Agregat a = hitungAgregat(arsip);
        catat("AGREGASI atas " + a.getJumlahPerkara() + " putusan");
        return a;
    }

    /** Menghitung agregat atas sembarang kumpulan putusan (misalnya hasil filter). */
    public static Agregat hitungAgregat(ArrayDinamis<Putusan> data) {
        Agregat a = new Agregat();
        long totalVonis = 0;
        for (int i = 0; i < data.size(); i++) {
            Putusan p = data.get(i);
            a.jumlahPerkara++;
            totalVonis += p.getVonisBulan();
            a.totalDenda += p.getDendaRupiah();
            String[] jenis = p.getJenisNarkotika();
            for (int j = 0; j < jenis.length; j++) {
                a.tambahJenis(jenis[j]);
            }
        }
        a.rataRataVonisBulan = a.jumlahPerkara == 0 ? 0 : (double) totalVonis / a.jumlahPerkara;
        return a;
    }

    // -------------------------------------------------------------- urutan

    /**
     * Menghasilkan salinan terurut menurut vonis (bulan) tanpa mengubah struktur
     * utama. Algoritma: insertion sort yang stabil (putusan dengan vonis sama
     * tetap berurutan seperti di struktur utama). Pencacah menghitung jumlah
     * perbandingan vonis dan jumlah pergeseran elemen.
     */
    public HasilUrut urutkanBerdasarkanVonis(boolean menaik) {
        ArrayDinamis<Putusan> salinan = new ArrayDinamis<>(Math.max(1, arsip.size()));
        for (int i = 0; i < arsip.size(); i++) {
            salinan.add(arsip.get(i));
        }
        int perbandingan = 0;
        int pergeseran = 0;
        for (int i = 1; i < salinan.size(); i++) {
            Putusan kunci = salinan.get(i);
            int j = i - 1;
            while (j >= 0) {
                perbandingan++;
                int vonisJ = salinan.get(j).getVonisBulan();
                boolean harusGeser = menaik ? vonisJ > kunci.getVonisBulan()
                        : vonisJ < kunci.getVonisBulan();
                if (!harusGeser) {
                    break;
                }
                salinan.set(j + 1, salinan.get(j));
                pergeseran++;
                j--;
            }
            salinan.set(j + 1, kunci);
        }
        salinan.validateInvariant();
        catat("URUT vonis " + (menaik ? "menaik" : "menurun") + ": " + perbandingan
                + " perbandingan, " + pergeseran + " pergeseran");
        return new HasilUrut(salinan, perbandingan, pergeseran);
    }

    // -------------------------------------------------------------- ekspor

    public void ekspor(Path berkas, ArrayDinamis<Putusan> data) throws IOException {
        EksporCsv.tulis(berkas, data);
        catat("EKSPOR " + data.size() + " putusan ke " + berkas);
    }

    // --------------------------------------------------------------- audit

    /** Riwayat dari yang terbaru (posisi 0 = puncak stack) sampai yang tertua. */
    public ArrayDinamis<String> riwayatTerbaruDulu() {
        ArrayDinamis<String> hasil = new ArrayDinamis<>();
        for (int i = 0; i < riwayat.size(); i++) {
            hasil.add(riwayat.get(i));
        }
        return hasil;
    }

    private void catat(String aksi) {
        riwayat.push("#" + (riwayat.size() + 1) + " " + aksi);
    }
}
