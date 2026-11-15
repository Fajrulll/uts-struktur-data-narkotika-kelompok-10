package id.ac.umm.strukdat.model;

/**
 * Dilempar saat satu record putusan tidak memenuhi aturan validasi.
 * Pesannya menyebut kolom yang bermasalah supaya mudah dilaporkan ke pengguna.
 */
public class DataTidakValidException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataTidakValidException(String pesan) {
        super(pesan);
    }
}
