package rental.model;

/**
 * INTERFACE: kontrak untuk semua objek yang bisa disimpan ke file.
 * Diimplementasikan oleh Kendaraan, Pelanggan, Penyewaan, dan Pengguna.
 */
public interface DapatDisimpan {
    String getId();

    /** Mengubah objek menjadi satu baris teks untuk disimpan. */
    String toCsv();
}
