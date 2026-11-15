package id.ac.umm.strukdat.struktur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ArrayDinamisTest {

    /** Mengisi array dengan "A", "B", ... sebanyak n elemen. */
    private ArrayDinamis<String> isi(int n) {
        ArrayDinamis<String> a = new ArrayDinamis<>();
        for (int i = 0; i < n; i++) {
            a.add(String.valueOf((char) ('A' + i)));
        }
        a.validateInvariant();
        return a;
    }

    @Test
    void baruKosong() {
        ArrayDinamis<String> a = new ArrayDinamis<>();
        assertTrue(a.isEmpty());
        assertEquals(0, a.size());
        a.validateInvariant();
    }

    @Test
    void kapasitasAwalTidakValid() {
        assertThrows(IllegalArgumentException.class, () -> new ArrayDinamis<String>(0));
    }

    @Test
    void addMenjagaUrutan() {
        ArrayDinamis<String> a = isi(3);
        assertEquals(3, a.size());
        assertEquals("A", a.get(0));
        assertEquals("B", a.get(1));
        assertEquals("C", a.get(2));
    }

    @Test
    void kapasitasTumbuhOtomatisSaatPenuh() {
        ArrayDinamis<String> a = new ArrayDinamis<>(2);
        a.add("A");
        a.add("B");
        assertEquals(2, a.kapasitas());
        a.add("C");
        assertEquals(4, a.kapasitas());
        assertEquals("C", a.get(2));
        a.validateInvariant();
    }

    @Test
    void muatLimaPuluhElemen() {
        ArrayDinamis<Integer> a = new ArrayDinamis<>();
        for (int i = 0; i < 50; i++) {
            a.add(i);
        }
        assertEquals(50, a.size());
        assertEquals(0, a.get(0));
        assertEquals(49, a.get(49));
        a.validateInvariant();
    }

    @Test
    void insertDiAwalMenggeserSemuaElemen() {
        ArrayDinamis<String> a = isi(3);
        a.insert(0, "X");
        assertEquals(4, a.size());
        assertEquals("X", a.get(0));
        assertEquals("A", a.get(1));
        assertEquals("C", a.get(3));
        assertEquals(3, a.getLangkahTerakhir());
        a.validateInvariant();
    }

    @Test
    void insertDiTengah() {
        ArrayDinamis<String> a = isi(4);
        a.insert(2, "X");
        assertEquals("A", a.get(0));
        assertEquals("B", a.get(1));
        assertEquals("X", a.get(2));
        assertEquals("C", a.get(3));
        assertEquals("D", a.get(4));
        assertEquals(2, a.getLangkahTerakhir());
        a.validateInvariant();
    }

    @Test
    void insertDiAkhirSamaDenganAdd() {
        ArrayDinamis<String> a = isi(2);
        a.insert(2, "X");
        assertEquals("X", a.get(2));
        assertEquals(0, a.getLangkahTerakhir());
        a.validateInvariant();
    }

    @Test
    void insertPosisiDiLuarBatas() {
        ArrayDinamis<String> a = isi(2);
        assertThrows(IndexOutOfBoundsException.class, () -> a.insert(-1, "X"));
        assertThrows(IndexOutOfBoundsException.class, () -> a.insert(3, "X"));
        assertEquals(2, a.size());
        a.validateInvariant();
    }

    @Test
    void elemenNullDitolakDanStrukturTetapKonsisten() {
        ArrayDinamis<String> a = isi(2);
        assertThrows(IllegalArgumentException.class, () -> a.add(null));
        assertThrows(IllegalArgumentException.class, () -> a.insert(0, null));
        assertThrows(IllegalArgumentException.class, () -> a.set(0, null));
        assertEquals(2, a.size());
        a.validateInvariant();
    }

    @Test
    void removeElemenPertamaMenggeserKeKiri() {
        ArrayDinamis<String> a = isi(4);
        assertEquals("A", a.remove(0));
        assertEquals(3, a.size());
        assertEquals("B", a.get(0));
        assertEquals("D", a.get(2));
        assertEquals(3, a.getLangkahTerakhir());
        a.validateInvariant();
    }

    @Test
    void removeElemenTengah() {
        ArrayDinamis<String> a = isi(5);
        assertEquals("C", a.remove(2));
        assertEquals("A", a.get(0));
        assertEquals("B", a.get(1));
        assertEquals("D", a.get(2));
        assertEquals("E", a.get(3));
        assertEquals(2, a.getLangkahTerakhir());
        a.validateInvariant();
    }

    @Test
    void removeElemenTerakhirTanpaPergeseran() {
        ArrayDinamis<String> a = isi(3);
        assertEquals("C", a.remove(2));
        assertEquals(2, a.size());
        assertEquals(0, a.getLangkahTerakhir());
        a.validateInvariant();
    }

    @Test
    void removeSampaiKosong() {
        ArrayDinamis<String> a = isi(3);
        a.remove(0);
        a.remove(0);
        a.remove(0);
        assertTrue(a.isEmpty());
        a.validateInvariant();
        assertThrows(IndexOutOfBoundsException.class, () -> a.remove(0));
    }

    @Test
    void tambahKembaliTigaElemenSetelahHapus() {
        ArrayDinamis<String> a = isi(5);
        a.remove(0);
        a.remove(2);
        a.remove(a.size() - 1);
        a.add("X");
        a.add("Y");
        a.add("Z");
        assertEquals(5, a.size());
        assertEquals("B", a.get(0));
        assertEquals("C", a.get(1));
        assertEquals("X", a.get(2));
        assertEquals("Y", a.get(3));
        assertEquals("Z", a.get(4));
        a.validateInvariant();
    }

    @Test
    void getDiLuarBatas() {
        ArrayDinamis<String> a = isi(2);
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(2));
    }

    @Test
    void setMengembalikanNilaiLama() {
        ArrayDinamis<String> a = isi(3);
        assertEquals("B", a.set(1, "X"));
        assertEquals("X", a.get(1));
        assertEquals(3, a.size());
        a.validateInvariant();
    }

    @Test
    void searchMelaporkanIndeksDanJumlahLangkah() {
        ArrayDinamis<String> a = isi(5);
        assertEquals(0, a.search("A"));
        assertEquals(1, a.getLangkahTerakhir());
        assertEquals(4, a.search("E"));
        assertEquals(5, a.getLangkahTerakhir());
        assertEquals(-1, a.search("Z"));
        assertEquals(5, a.getLangkahTerakhir());
    }

    @Test
    void containsDanSearchNull() {
        ArrayDinamis<String> a = isi(3);
        assertTrue(a.contains("B"));
        assertFalse(a.contains("Z"));
        assertFalse(a.contains(null));
    }

    @Test
    void searchTidakMengubahIsiAtauUrutan() {
        ArrayDinamis<String> a = isi(4);
        a.search("C");
        a.search("Z");
        assertEquals(4, a.size());
        assertEquals("A", a.get(0));
        assertEquals("D", a.get(3));
        a.validateInvariant();
    }

    @Test
    void resizeManual() {
        ArrayDinamis<String> a = isi(3);
        a.resize(10);
        assertEquals(10, a.kapasitas());
        assertEquals(3, a.size());
        assertEquals("C", a.get(2));
        a.validateInvariant();
        assertThrows(IllegalArgumentException.class, () -> a.resize(2));
        assertThrows(IllegalArgumentException.class, () -> a.resize(0));
    }

    @Test
    void clearMengosongkanTanpaMengubahKapasitas() {
        ArrayDinamis<String> a = isi(6);
        int kapasitas = a.kapasitas();
        a.clear();
        assertTrue(a.isEmpty());
        assertEquals(kapasitas, a.kapasitas());
        a.validateInvariant();
        a.add("X");
        assertEquals("X", a.get(0));
    }
}
