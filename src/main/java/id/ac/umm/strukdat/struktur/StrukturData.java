package id.ac.umm.strukdat.struktur;

/**
 * Kontrak umum struktur data pada aplikasi putusan narkotika.
 * Dipakai oleh struktur utama (array dinamis) dan struktur sekunder (stack).
 *
 * @param <T> tipe elemen yang disimpan
 */
public interface StrukturData<T> {

    /** Menambahkan elemen di akhir struktur. */
    void add(T value);

    /** Mengambil elemen pada posisi tertentu tanpa menghapusnya. */
    T get(int position);

    /** Menghapus dan mengembalikan elemen pada posisi tertentu. */
    T remove(int position);

    /** Jumlah elemen yang sedang tersimpan. */
    int size();

    /** True jika tidak ada elemen. */
    boolean isEmpty();

    /** True jika ada elemen yang sama menurut equals. */
    boolean contains(T value);

    /** Mengosongkan struktur. */
    void clear();
}
