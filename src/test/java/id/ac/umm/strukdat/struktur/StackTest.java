package id.ac.umm.strukdat.struktur;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StackTest {

    /** Mengisi stack dengan "A", "B", ... sebanyak n elemen (puncak = elemen terakhir). */
    private Stack<String> isi(int n) {
        Stack<String> s = new Stack<>();
        for (int i = 0; i < n; i++) {
            s.push(String.valueOf((char) ('A' + i)));
        }
        s.validateInvariant();
        return s;
    }

    @Test
    void baruKosong() {
        Stack<String> s = new Stack<>();
        assertTrue(s.isEmpty());
        assertEquals(0, s.size());
        s.validateInvariant();
    }

    @Test
    void pushLaluPeekMelihatPuncakTanpaMenghapus() {
        Stack<String> s = isi(3);
        assertEquals("C", s.peek());
        assertEquals("C", s.peek());
        assertEquals(3, s.size());
    }

    @Test
    void popMengikutiUrutanLifo() {
        Stack<String> s = isi(3);
        assertEquals("C", s.pop());
        assertEquals("B", s.pop());
        assertEquals("A", s.pop());
        assertTrue(s.isEmpty());
        s.validateInvariant();
    }

    @Test
    void popDanPeekPadaStackKosongMelempar() {
        Stack<String> s = new Stack<>();
        assertThrows(IllegalStateException.class, s::pop);
        assertThrows(IllegalStateException.class, s::peek);
        s.validateInvariant();
    }

    @Test
    void pushNullDitolak() {
        Stack<String> s = isi(2);
        assertThrows(IllegalArgumentException.class, () -> s.push(null));
        assertEquals(2, s.size());
        s.validateInvariant();
    }

    @Test
    void addSamaDenganPush() {
        Stack<String> s = new Stack<>();
        s.add("X");
        s.add("Y");
        assertEquals("Y", s.peek());
        assertEquals(2, s.size());
    }

    @Test
    void getPosisiNolAdalahPuncak() {
        Stack<String> s = isi(3);
        assertEquals("C", s.get(0));
        assertEquals("B", s.get(1));
        assertEquals("A", s.get(2));
        assertEquals(3, s.size());
        assertThrows(IndexOutOfBoundsException.class, () -> s.get(3));
        assertThrows(IndexOutOfBoundsException.class, () -> s.get(-1));
    }

    @Test
    void removeHanyaBolehDiPuncak() {
        Stack<String> s = isi(3);
        assertThrows(UnsupportedOperationException.class, () -> s.remove(1));
        assertThrows(UnsupportedOperationException.class, () -> s.remove(2));
        assertEquals(3, s.size());
        assertEquals("C", s.remove(0));
        assertEquals(2, s.size());
        s.validateInvariant();
    }

    @Test
    void containsDanClear() {
        Stack<String> s = isi(3);
        assertTrue(s.contains("B"));
        assertFalse(s.contains("Z"));
        s.clear();
        assertTrue(s.isEmpty());
        s.validateInvariant();
    }

    @Test
    void searchPreserveMenemukanDanUrutanKembaliSama() {
        Stack<String> s = isi(5); // A B C D E, puncak E
        int posisi = s.searchPreserve("B");
        assertEquals(3, posisi);          // E=0, D=1, C=2, B=3
        assertEquals(4, s.getLangkahTerakhir());
        assertEquals(5, s.size());
        assertEquals("E", s.get(0));
        assertEquals("D", s.get(1));
        assertEquals("C", s.get(2));
        assertEquals("B", s.get(3));
        assertEquals("A", s.get(4));
        s.validateInvariant();
    }

    @Test
    void searchPreserveDiPuncakDanDiDasar() {
        Stack<String> s = isi(4);
        assertEquals(0, s.searchPreserve("D"));
        assertEquals(1, s.getLangkahTerakhir());
        assertEquals(3, s.searchPreserve("A"));
        assertEquals(4, s.getLangkahTerakhir());
        assertEquals(4, s.size());
        assertEquals("D", s.peek());
        s.validateInvariant();
    }

    @Test
    void searchPreserveTidakKetemuTetapMengembalikanSemuaElemen() {
        Stack<String> s = isi(4);
        assertEquals(-1, s.searchPreserve("Z"));
        assertEquals(4, s.getLangkahTerakhir());
        assertEquals(4, s.size());
        assertEquals("D", s.get(0));
        assertEquals("A", s.get(3));
        s.validateInvariant();
    }

    @Test
    void searchPreservePadaStackKosongDanNull() {
        Stack<String> s = new Stack<>();
        assertEquals(-1, s.searchPreserve("A"));
        Stack<String> t = isi(2);
        assertEquals(-1, t.searchPreserve(null));
        assertEquals(2, t.size());
        assertEquals("B", t.peek());
        t.validateInvariant();
    }

    @Test
    void restoreMengembalikanUrutanDariPenampung() {
        Stack<String> s = isi(3);
        Stack<String> penampung = new Stack<>();
        penampung.push(s.pop()); // C
        penampung.push(s.pop()); // B
        s.restore(penampung);
        assertTrue(penampung.isEmpty());
        assertEquals(3, s.size());
        assertEquals("C", s.get(0));
        assertEquals("B", s.get(1));
        assertEquals("A", s.get(2));
        s.validateInvariant();
    }

    @Test
    void muatBanyakElemenMelewatiResizeInternal() {
        Stack<Integer> s = new Stack<>();
        for (int i = 0; i < 50; i++) {
            s.push(i);
        }
        assertEquals(50, s.size());
        assertEquals(49, s.peek());
        for (int i = 49; i >= 0; i--) {
            assertEquals(i, s.pop());
        }
        s.validateInvariant();
    }
}
