package rental.model;

import rental.exception.DataTidakValidException;
import rental.util.CsvUtil;
import rental.util.Validasi;

/**
 * ABSTRACT CLASS induk hierarki kendaraan (Mobil, Motor, Bus).
 * Semua atribut private; akses lewat getter/setter yang memvalidasi.
 */
public abstract class Kendaraan implements DapatDisimpan {
    public static final double TARIF_ASURANSI = 0.05; // 5% dari biaya sewa
    public static final int MAKS_DESKRIPSI = 500;     // panjang maksimum deskripsi kondisi

    private String id;
    private String plat;
    private String merek;
    private double tarifPerHari;
    private boolean tersedia = true;
    private String deskripsi = ""; // kondisi kendaraan (opsional) agar calon penyewa tahu
    private String foto = "";      // nama berkas foto di folder data/foto (opsional)

    protected Kendaraan(String id, String plat, String merek, double tarifPerHari)
            throws DataTidakValidException {
        setId(id);
        setPlat(plat);
        setMerek(merek);
        setTarifPerHari(tarifPerHari);
    }

    // ---- method abstrak: wajib di-override oleh setiap jenis kendaraan ----
    /** POLIMORFISME: aturan hitung sewa berbeda untuk tiap jenis. */
    public abstract double hitungSewa(int hari);

    public abstract String getJenis();

    /** Keterangan khusus tiap jenis (kapasitas, cc, jumlah kursi). */
    public abstract String getDetail();

    /** Nilai angka khusus tiap jenis (untuk mengisi form). */
    public abstract int getNilaiKhusus();

    /** OVERLOADING: versi lain dari hitungSewa dengan opsi asuransi. */
    public double hitungSewa(int hari, boolean denganAsuransi) {
        double biaya = hitungSewa(hari);
        return denganAsuransi ? biaya + biaya * TARIF_ASURANSI : biaya;
    }

    @Override
    public abstract String toCsv();

    /** Format CSV bersama: jenis,id,plat,merek,tarif,tersedia,nilaiKhusus,deskripsi,foto. */
    protected String csv(int nilaiKhusus) {
        return CsvUtil.gabung(getJenis(), getId(), getPlat(), getMerek(), getTarifPerHari(),
                isTersedia(), nilaiKhusus, deskripsi, foto);
    }

    // ---- getter & setter dengan validasi ----
    @Override
    public String getId() { return id; }

    public void setId(String id) throws DataTidakValidException {
        this.id = Validasi.wajibIsi(id, "ID kendaraan");
    }

    public String getPlat() { return plat; }

    public void setPlat(String plat) throws DataTidakValidException {
        this.plat = Validasi.wajibIsi(plat, "Plat nomor").toUpperCase();
    }

    public String getMerek() { return merek; }

    public void setMerek(String merek) throws DataTidakValidException {
        this.merek = Validasi.wajibIsi(merek, "Merek");
    }

    public double getTarifPerHari() { return tarifPerHari; }

    public void setTarifPerHari(double tarifPerHari) throws DataTidakValidException {
        this.tarifPerHari = Validasi.positif(tarifPerHari, "Tarif per hari");
    }

    public boolean isTersedia() { return tersedia; }

    public void setTersedia(boolean tersedia) { this.tersedia = tersedia; }

    public String getDeskripsi() { return deskripsi; }

    public void setDeskripsi(String deskripsi) throws DataTidakValidException {
        String d = deskripsi == null ? "" : deskripsi.trim();
        if (d.length() > MAKS_DESKRIPSI) {
            throw new DataTidakValidException("Deskripsi kondisi maksimal " + MAKS_DESKRIPSI
                    + " karakter (sekarang " + d.length() + ").");
        }
        this.deskripsi = d;
    }

    /** Nama berkas foto (bukan path penuh); string kosong berarti belum ada foto. */
    public String getFoto() { return foto; }

    public void setFoto(String foto) { this.foto = foto == null ? "" : foto.trim(); }

    public boolean adaFoto() { return !foto.isEmpty(); }

    @Override
    public String toString() {
        return id + " - " + plat + " - " + merek + " (" + getJenis() + ")";
    }
}
