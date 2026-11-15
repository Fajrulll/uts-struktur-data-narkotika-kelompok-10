package id.ac.umm.strukdat.impor;

import id.ac.umm.strukdat.struktur.ArrayDinamis;

/**
 * Pembaca CSV sederhana buatan sendiri (RFC 4180 versi ringkas).
 *
 * <p>Aturan yang didukung:
 * <ul>
 *   <li>pemisah kolom koma, pemisah baris LF, CRLF, atau CR</li>
 *   <li>nilai boleh diapit tanda kutip ganda; di dalamnya koma dan baris baru
 *       dianggap isi, dan {@code ""} berarti satu tanda kutip</li>
 *   <li>BOM UTF-8 di awal teks dibuang</li>
 *   <li>baris yang sama sekali kosong dilewati</li>
 * </ul>
 *
 * <p>Hasilnya disimpan di {@link ArrayDinamis} berisi larik kolom per baris.
 */
public final class CsvParser {

    private CsvParser() {
    }

    /**
     * @throws IllegalArgumentException jika tanda kutip tidak ditutup
     */
    public static ArrayDinamis<String[]> parse(String teks) {
        ArrayDinamis<String[]> baris = new ArrayDinamis<>();
        if (teks == null || teks.isEmpty()) {
            return baris;
        }
        int mulai = teks.charAt(0) == '﻿' ? 1 : 0;

        ArrayDinamis<String> kolom = new ArrayDinamis<>();
        StringBuilder nilai = new StringBuilder();
        boolean dalamKutip = false;
        boolean pernahKutip = false;
        boolean adaIsiBaris = false;

        for (int i = mulai; i < teks.length(); i++) {
            char c = teks.charAt(i);
            if (dalamKutip) {
                if (c == '"') {
                    if (i + 1 < teks.length() && teks.charAt(i + 1) == '"') {
                        nilai.append('"');
                        i++;
                    } else {
                        dalamKutip = false;
                    }
                } else {
                    nilai.append(c);
                }
            } else if (c == '"') {
                dalamKutip = true;
                pernahKutip = true;
                adaIsiBaris = true;
            } else if (c == ',') {
                kolom.add(nilai.toString());
                nilai.setLength(0);
                adaIsiBaris = true;
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < teks.length() && teks.charAt(i + 1) == '\n') {
                    i++;
                }
                tutupBaris(baris, kolom, nilai, pernahKutip, adaIsiBaris);
                kolom = new ArrayDinamis<>();
                nilai.setLength(0);
                pernahKutip = false;
                adaIsiBaris = false;
            } else {
                nilai.append(c);
                adaIsiBaris = true;
            }
        }
        if (dalamKutip) {
            throw new IllegalArgumentException("Tanda kutip tidak ditutup di akhir data");
        }
        tutupBaris(baris, kolom, nilai, pernahKutip, adaIsiBaris);
        return baris;
    }

    /** Menutup satu baris. Baris yang sepenuhnya kosong dilewati. */
    private static void tutupBaris(ArrayDinamis<String[]> baris, ArrayDinamis<String> kolom,
            StringBuilder nilai, boolean pernahKutip, boolean adaIsiBaris) {
        if (!adaIsiBaris && kolom.isEmpty() && nilai.length() == 0 && !pernahKutip) {
            return;
        }
        kolom.add(nilai.toString());
        String[] larik = new String[kolom.size()];
        for (int i = 0; i < larik.length; i++) {
            larik[i] = kolom.get(i);
        }
        baris.add(larik);
    }
}
