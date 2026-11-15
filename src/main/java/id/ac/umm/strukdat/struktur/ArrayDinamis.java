package id.ac.umm.strukdat.struktur;

/**
 * Array dinamis generik buatan sendiri (tanpa koleksi java.util).
 *
 * <p>Elemen disimpan di array mentah {@code Object[]}. Saat penuh, kapasitas
 * digandakan. Elemen aktif selalu menempati indeks 0 sampai size-1, dan slot
 * sisanya berisi null.
 *
 * <p>Invariant (diperiksa oleh {@link #validateInvariant()}):
 * <ul>
 *   <li>data tidak null dan kapasitas minimal 1</li>
 *   <li>0 &lt;= size &lt;= kapasitas</li>
 *   <li>slot 0..size-1 tidak null (struktur ini menolak elemen null)</li>
 *   <li>slot size..kapasitas-1 selalu null</li>
 * </ul>
 *
 * @param <T> tipe elemen
 */
public class ArrayDinamis<T> implements StrukturData<T> {

    private static final int KAPASITAS_AWAL = 4;

    private Object[] data;
    private int size;
    private int langkahTerakhir;

    public ArrayDinamis() {
        this(KAPASITAS_AWAL);
    }

    public ArrayDinamis(int kapasitasAwal) {
        if (kapasitasAwal < 1) {
            throw new IllegalArgumentException("Kapasitas awal minimal 1: " + kapasitasAwal);
        }
        this.data = new Object[kapasitasAwal];
        this.size = 0;
        this.langkahTerakhir = 0;
    }

    // ---------------------------------------------------------------- kontrak

    @Override
    public void add(T value) {
        insert(size, value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int position) {
        periksaPosisiElemen(position);
        return (T) data[position];
    }

    @Override
    @SuppressWarnings("unchecked")
    public T remove(int position) {
        periksaPosisiElemen(position);
        T dihapus = (T) data[position];
        int langkah = 0;
        for (int i = position; i < size - 1; i++) {
            data[i] = data[i + 1];
            langkah++;
        }
        data[size - 1] = null;
        size--;
        langkahTerakhir = langkah;
        return dihapus;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(T value) {
        return search(value) >= 0;
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            data[i] = null;
        }
        size = 0;
        langkahTerakhir = 0;
    }

    // ------------------------------------------------------- operasi tambahan

    /**
     * Menyisipkan elemen pada posisi tertentu. Elemen di posisi itu dan
     * sesudahnya bergeser satu langkah ke kanan. Posisi boleh sama dengan size
     * (sama artinya dengan add).
     */
    public void insert(int position, T value) {
        if (value == null) {
            throw new IllegalArgumentException("Elemen null tidak diizinkan");
        }
        if (position < 0 || position > size) {
            throw new IndexOutOfBoundsException(
                    "Posisi sisip " + position + " di luar 0.." + size);
        }
        if (size == data.length) {
            resize(data.length * 2);
        }
        int langkah = 0;
        for (int i = size; i > position; i--) {
            data[i] = data[i - 1];
            langkah++;
        }
        data[position] = value;
        size++;
        langkahTerakhir = langkah;
    }

    /** Mengganti elemen pada posisi tertentu dan mengembalikan nilai lamanya. */
    @SuppressWarnings("unchecked")
    public T set(int position, T value) {
        if (value == null) {
            throw new IllegalArgumentException("Elemen null tidak diizinkan");
        }
        periksaPosisiElemen(position);
        T lama = (T) data[position];
        data[position] = value;
        langkahTerakhir = 0;
        return lama;
    }

    /**
     * Pencarian linear. Mengembalikan indeks pertama yang cocok menurut equals,
     * atau -1 jika tidak ada. Jumlah perbandingan dicatat di
     * {@link #getLangkahTerakhir()}.
     */
    public int search(T value) {
        int langkah = 0;
        if (value != null) {
            for (int i = 0; i < size; i++) {
                langkah++;
                if (value.equals(data[i])) {
                    langkahTerakhir = langkah;
                    return i;
                }
            }
        }
        langkahTerakhir = langkah;
        return -1;
    }

    /**
     * Mengubah kapasitas array. Kapasitas baru tidak boleh lebih kecil dari
     * jumlah elemen yang tersimpan.
     */
    public void resize(int kapasitasBaru) {
        if (kapasitasBaru < 1 || kapasitasBaru < size) {
            throw new IllegalArgumentException(
                    "Kapasitas baru " + kapasitasBaru + " harus >= max(1, size=" + size + ")");
        }
        Object[] baru = new Object[kapasitasBaru];
        for (int i = 0; i < size; i++) {
            baru[i] = data[i];
        }
        data = baru;
    }

    public int kapasitas() {
        return data.length;
    }

    /**
     * Jumlah langkah (perbandingan atau pergeseran elemen) pada operasi terakhir
     * yaitu search, insert, add, atau remove. Dipakai untuk laporan langkah dan
     * analisis kompleksitas.
     */
    public int getLangkahTerakhir() {
        return langkahTerakhir;
    }

    /**
     * Memeriksa semua invariant. Melempar IllegalStateException bila ada yang
     * dilanggar. Dipanggil oleh test setelah setiap operasi.
     */
    public void validateInvariant() {
        if (data == null) {
            throw new IllegalStateException("data null");
        }
        if (data.length < 1) {
            throw new IllegalStateException("kapasitas < 1");
        }
        if (size < 0 || size > data.length) {
            throw new IllegalStateException(
                    "size " + size + " di luar 0.." + data.length);
        }
        for (int i = 0; i < size; i++) {
            if (data[i] == null) {
                throw new IllegalStateException("slot aktif " + i + " bernilai null");
            }
        }
        for (int i = size; i < data.length; i++) {
            if (data[i] != null) {
                throw new IllegalStateException("slot cadangan " + i + " tidak null");
            }
        }
    }

    // ----------------------------------------------------------------- bantu

    private void periksaPosisiElemen(int position) {
        if (position < 0 || position >= size) {
            throw new IndexOutOfBoundsException(
                    "Posisi " + position + " di luar 0.." + (size - 1));
        }
    }
}
