package rental.model;

/** Status sebuah transaksi penyewaan. */
public enum StatusSewa {
    AKTIF("Aktif"), SELESAI("Selesai");

    private final String label;

    StatusSewa(String label) { this.label = label; }

    public String getLabel() { return label; }
}
