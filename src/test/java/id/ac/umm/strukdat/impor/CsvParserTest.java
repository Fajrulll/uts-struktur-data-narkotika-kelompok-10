package id.ac.umm.strukdat.impor;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import id.ac.umm.strukdat.struktur.ArrayDinamis;
import org.junit.jupiter.api.Test;

class CsvParserTest {

    @Test
    void teksKosongAtauNullMenghasilkanNolBaris() {
        assertTrue(CsvParser.parse("").isEmpty());
        assertTrue(CsvParser.parse(null).isEmpty());
    }

    @Test
    void duaBarisSederhana() {
        ArrayDinamis<String[]> r = CsvParser.parse("a,b,c\n1,2,3\n");
        assertEquals(2, r.size());
        assertArrayEquals(new String[] {"a", "b", "c"}, r.get(0));
        assertArrayEquals(new String[] {"1", "2", "3"}, r.get(1));
        r.validateInvariant();
    }

    @Test
    void barisTerakhirTanpaPemisahBarisTetapDibaca() {
        ArrayDinamis<String[]> r = CsvParser.parse("a,b\n1,2");
        assertEquals(2, r.size());
        assertArrayEquals(new String[] {"1", "2"}, r.get(1));
    }

    @Test
    void pemisahBarisCrlfDanCr() {
        assertEquals(2, CsvParser.parse("a,b\r\n1,2\r\n").size());
        assertEquals(2, CsvParser.parse("a,b\r1,2\r").size());
    }

    @Test
    void nilaiBerkutipBolehMemuatKomaDanBarisBaru() {
        ArrayDinamis<String[]> r = CsvParser.parse("a,\"x,y\",\"baris1\nbaris2\"\n");
        assertEquals(1, r.size());
        assertArrayEquals(new String[] {"a", "x,y", "baris1\nbaris2"}, r.get(0));
    }

    @Test
    void kutipGandaDidalamNilaiBerkutipMenjadiSatuKutip() {
        ArrayDinamis<String[]> r = CsvParser.parse("\"label \"\"C\"\" berat\",z\n");
        assertArrayEquals(new String[] {"label \"C\" berat", "z"}, r.get(0));
    }

    @Test
    void nilaiKosongDipertahankan() {
        ArrayDinamis<String[]> r = CsvParser.parse("a,,c\n,,\n");
        assertArrayEquals(new String[] {"a", "", "c"}, r.get(0));
        assertArrayEquals(new String[] {"", "", ""}, r.get(1));
    }

    @Test
    void barisKosongDilewati() {
        ArrayDinamis<String[]> r = CsvParser.parse("a,b\n\n\n1,2\n\n");
        assertEquals(2, r.size());
    }

    @Test
    void bomDiAwalDibuang() {
        ArrayDinamis<String[]> r = CsvParser.parse("﻿caseId,x\nP001,1\n");
        assertEquals("caseId", r.get(0)[0]);
    }

    @Test
    void kutipTidakDitutupMelemparKesalahan() {
        assertThrows(IllegalArgumentException.class, () -> CsvParser.parse("a,\"belum ditutup\n"));
    }
}
