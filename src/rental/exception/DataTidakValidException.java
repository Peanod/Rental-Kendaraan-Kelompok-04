package rental.exception;

/**
 * Custom exception untuk semua kesalahan validasi data / aturan bisnis.
 * Dilempar oleh setter (enkapsulasi), controller, dan DAO.
 */
public class DataTidakValidException extends Exception {
    private static final long serialVersionUID = 1L;

    public DataTidakValidException(String pesan) {
        super(pesan);
    }
}
