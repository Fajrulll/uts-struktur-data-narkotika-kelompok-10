package id.ac.umm.strukdat.impor;

import id.ac.umm.strukdat.model.DataTidakValidException;
import id.ac.umm.strukdat.model.Putusan;
import id.ac.umm.strukdat.struktur.ArrayDinamis;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Mengimpor putusan dari CSV ke struktur utama ({@link ArrayDinamis}).
 *
 * <p>Record yang tidak valid atau duplikat ditolak dan dilaporkan di
 * {@link HasilImpor}, tetapi impor tetap berlanjut ke record berikutnya.
 * Record yang ditolak tidak pernah masuk ke struktur, jadi struktur selalu
 * konsisten. Hanya masalah pada berkas itu sendiri (header salah, kutip tidak
 * ditutup, berkas tidak terbaca) yang menghentikan impor.
 */
public final class ImporPutusan {

    private ImporPutusan() {
    }

    /** Laporan hasil impor. */
    public static final class HasilImpor {
        private int diterima;
        private int ditolak;
        private final ArrayDinamis<String> pesanDitolak = new ArrayDinamis<>();

        public int getDiterima() {
            return diterima;
        }

        public int getDitolak() {
            return ditolak;
        }

        /** Satu pesan per record yang ditolak, berurutan menurut nomor baris berkas. */
        public ArrayDinamis<String> getPesanDitolak() {
            return pesanDitolak;
        }

        private void tolak(int nomorBaris, String alasan) {
            ditolak++;
            pesanDitolak.add("Baris " + nomorBaris + " ditolak: " + alasan);
        }
    }

    /** Membaca berkas CSV (UTF-8). */
    public static HasilImpor dariFile(Path berkas, ArrayDinamis<Putusan> tujuan)
            throws IOException {
        String teks = Files.readString(berkas, StandardCharsets.UTF_8);
        return dariTeks(teks, tujuan);
    }

    /**
     * Mengimpor dari teks CSV. Baris pertama harus header yang sama persis dengan
     * {@link Putusan#namaKolom()}.
     *
     * @throws DataTidakValidException jika header tidak sesuai atau data kosong
     * @throws IllegalArgumentException jika format CSV rusak (kutip tidak ditutup)
     */
    public static HasilImpor dariTeks(String teks, ArrayDinamis<Putusan> tujuan) {
        ArrayDinamis<String[]> baris = CsvParser.parse(teks);
        if (baris.isEmpty()) {
            throw new DataTidakValidException("Berkas kosong, header tidak ditemukan");
        }
        periksaHeader(baris.get(0));

        HasilImpor hasil = new HasilImpor();
        for (int i = 1; i < baris.size(); i++) {
            int nomorBaris = i + 1;
            try {
                Putusan p = Putusan.dariKolom(baris.get(i));
                String duplikat = cariDuplikat(tujuan, p);
                if (duplikat != null) {
                    hasil.tolak(nomorBaris, duplikat);
                } else {
                    tujuan.add(p);
                    hasil.diterima++;
                }
            } catch (DataTidakValidException e) {
                hasil.tolak(nomorBaris, e.getMessage());
            }
        }
        return hasil;
    }

    private static void periksaHeader(String[] header) {
        String[] harapan = Putusan.namaKolom();
        if (header.length != harapan.length) {
            throw new DataTidakValidException(
                    "Header punya " + header.length + " kolom, seharusnya " + harapan.length);
        }
        for (int i = 0; i < harapan.length; i++) {
            if (!harapan[i].equals(header[i].trim())) {
                throw new DataTidakValidException("Header kolom ke-" + (i + 1)
                        + " '" + header[i] + "', seharusnya '" + harapan[i] + "'");
            }
        }
    }

    /** Mengembalikan alasan penolakan jika caseId atau nomor perkara sudah ada, selain itu null. */
    private static String cariDuplikat(ArrayDinamis<Putusan> tujuan, Putusan baru) {
        for (int i = 0; i < tujuan.size(); i++) {
            Putusan ada = tujuan.get(i);
            if (ada.getCaseId().equals(baru.getCaseId())) {
                return "caseId " + baru.getCaseId() + " sudah ada";
            }
            if (ada.getNomorPerkara().equals(baru.getNomorPerkara())) {
                return "nomor perkara " + baru.getNomorPerkara() + " sudah ada";
            }
        }
        return null;
    }
}
