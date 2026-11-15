package id.ac.umm.strukdat.impor;

import id.ac.umm.strukdat.model.Putusan;
import id.ac.umm.strukdat.struktur.ArrayDinamis;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Menulis putusan ke CSV dengan format yang sama seperti data/putusan-50.csv,
 * sehingga hasil ekspor bisa diimpor kembali.
 */
public final class EksporCsv {

    private EksporCsv() {
    }

    /** Membuat teks CSV (header + satu baris per putusan) dengan pemisah baris LF. */
    public static String keTeks(ArrayDinamis<Putusan> data) {
        StringBuilder sb = new StringBuilder();
        tulisBaris(sb, Putusan.namaKolom());
        for (int i = 0; i < data.size(); i++) {
            tulisBaris(sb, kolom(data.get(i)));
        }
        return sb.toString();
    }

    /** Menyimpan ke berkas UTF-8. Folder induk dibuat jika belum ada. */
    public static void tulis(Path berkas, ArrayDinamis<Putusan> data) throws IOException {
        Path induk = berkas.toAbsolutePath().getParent();
        if (induk != null) {
            Files.createDirectories(induk);
        }
        Files.writeString(berkas, keTeks(data), StandardCharsets.UTF_8);
    }

    private static String[] kolom(Putusan p) {
        return new String[] {
            p.getCaseId(),
            p.getNomorPerkara(),
            p.getPengadilan(),
            p.getTanggalPutusan().toString(),
            p.getTerdakwaKode(),
            String.join("|", p.getJenisNarkotika()),
            BigDecimal.valueOf(p.getBeratBarangBuktiGram()).toPlainString(),
            String.join("|", p.getPasalDakwaan()),
            String.join("|", p.getPasalTerbukti()),
            String.valueOf(p.getTuntutanBulan()),
            String.valueOf(p.getVonisBulan()),
            String.valueOf(p.getDendaRupiah()),
            p.getBarangBukti(),
            p.getKeadaanMemberatkan(),
            p.getKeadaanMeringankan(),
            p.getSourcePages()
        };
    }

    private static void tulisBaris(StringBuilder sb, String[] nilai) {
        for (int i = 0; i < nilai.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(kutipJikaPerlu(nilai[i]));
        }
        sb.append('\n');
    }

    private static String kutipJikaPerlu(String v) {
        if (v.indexOf(',') < 0 && v.indexOf('"') < 0 && v.indexOf('\n') < 0
                && v.indexOf('\r') < 0) {
            return v;
        }
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
