package id.ac.umm.strukdat.struktur;

/**
 * Stack (tumpukan) generik buatan sendiri, dipakai sebagai struktur sekunder
 * untuk riwayat audit. Prinsipnya LIFO: elemen yang masuk terakhir keluar lebih
 * dulu.
 *
 * <p>Penyimpanan memakai {@link ArrayDinamis} buatan sendiri (bukan koleksi
 * java.util). Puncak stack adalah elemen terakhir pada array, jadi push dan pop
 * hanya menyentuh ujung array dan tidak ada pergeseran elemen.
 *
 * <p>Pemetaan ke kontrak {@link StrukturData}:
 * <ul>
 *   <li>{@code add(value)} sama dengan {@link #push(Object)}</li>
 *   <li>{@code get(position)}: posisi 0 adalah puncak, posisi 1 di bawahnya,
 *       dan seterusnya. Tidak menghapus elemen.</li>
 *   <li>{@code remove(position)}: hanya {@code remove(0)} yang diizinkan dan
 *       sama dengan {@link #pop()}. Posisi lain melempar
 *       UnsupportedOperationException karena stack tidak boleh menghapus
 *       elemen di tengah (penyimpangan yang disengaja).</li>
 * </ul>
 *
 * <p>Invariant: urutan LIFO terjaga, dan penyimpanan di dalamnya memenuhi
 * invariant {@link ArrayDinamis}.
 *
 * @param <T> tipe elemen
 */
public class Stack<T> implements StrukturData<T> {

    private final ArrayDinamis<T> isi;
    private int langkahTerakhir;

    public Stack() {
        this.isi = new ArrayDinamis<>();
        this.langkahTerakhir = 0;
    }

    // ---------------------------------------------------------- operasi stack

    /** Menaruh elemen di puncak. */
    public void push(T value) {
        isi.add(value);
        langkahTerakhir = 0;
    }

    /** Mengambil dan menghapus elemen di puncak. */
    public T pop() {
        periksaTidakKosong();
        langkahTerakhir = 0;
        return isi.remove(isi.size() - 1);
    }

    /** Melihat elemen di puncak tanpa menghapusnya. */
    public T peek() {
        periksaTidakKosong();
        return isi.get(isi.size() - 1);
    }

    /**
     * Mencari elemen dari puncak ke bawah tanpa mengubah isi maupun urutan
     * stack. Caranya klasik: elemen di-pop satu per satu ke stack penampung
     * sampai ketemu atau habis, lalu {@link #restore(Stack)} mengembalikan
     * semuanya ke urutan semula.
     *
     * @return posisi dari puncak (0 = puncak), atau -1 jika tidak ada. Jumlah
     *         elemen yang diperiksa tersedia di {@link #getLangkahTerakhir()}.
     */
    public int searchPreserve(T value) {
        Stack<T> penampung = new Stack<>();
        int posisi = -1;
        int langkah = 0;
        if (value != null) {
            while (!isEmpty()) {
                T teratas = pop();
                penampung.push(teratas);
                langkah++;
                if (value.equals(teratas)) {
                    posisi = langkah - 1;
                    break;
                }
            }
        }
        restore(penampung);
        langkahTerakhir = langkah;
        return posisi;
    }

    /**
     * Mengembalikan semua elemen dari stack penampung ke stack ini. Karena
     * penampung menyimpan elemen dalam urutan terbalik, memindahkannya lagi
     * mengembalikan urutan aslinya.
     */
    public void restore(Stack<T> penampung) {
        while (!penampung.isEmpty()) {
            push(penampung.pop());
        }
    }

    /** Jumlah elemen yang diperiksa pada searchPreserve terakhir. */
    public int getLangkahTerakhir() {
        return langkahTerakhir;
    }

    /** Memeriksa invariant penyimpanan. Melempar IllegalStateException jika rusak. */
    public void validateInvariant() {
        isi.validateInvariant();
    }

    // ------------------------------------------------------------- kontrak

    @Override
    public void add(T value) {
        push(value);
    }

    @Override
    public T get(int position) {
        if (position < 0 || position >= isi.size()) {
            throw new IndexOutOfBoundsException(
                    "Posisi " + position + " di luar 0.." + (isi.size() - 1));
        }
        return isi.get(isi.size() - 1 - position);
    }

    @Override
    public T remove(int position) {
        if (position != 0) {
            throw new UnsupportedOperationException(
                    "Stack hanya boleh menghapus puncak (posisi 0), bukan posisi " + position);
        }
        return pop();
    }

    @Override
    public int size() {
        return isi.size();
    }

    @Override
    public boolean isEmpty() {
        return isi.isEmpty();
    }

    @Override
    public boolean contains(T value) {
        return isi.contains(value);
    }

    @Override
    public void clear() {
        isi.clear();
        langkahTerakhir = 0;
    }

    // ----------------------------------------------------------------- bantu

    private void periksaTidakKosong() {
        if (isi.isEmpty()) {
            throw new IllegalStateException("Stack kosong");
        }
    }
}
