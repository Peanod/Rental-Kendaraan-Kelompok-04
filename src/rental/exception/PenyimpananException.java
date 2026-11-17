package rental.exception;

/** Dilempar saat gagal membaca/menulis file penyimpanan (membungkus IOException). */
public class PenyimpananException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PenyimpananException(String pesan, Throwable sebab) {
        super(pesan, sebab);
    }
}
