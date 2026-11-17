package rental.model;

import java.time.LocalDate;

import rental.exception.DataTidakValidException;
import rental.util.CsvUtil;
import rental.util.Validasi;

/** Transaksi sewa: menghubungkan satu Pelanggan dengan satu Kendaraan. */
public class Penyewaan implements DapatDisimpan {
    private String id;
    private Kendaraan kendaraan;
    private Pelanggan pelanggan;
    private LocalDate tanggalMulai;
    private int lamaHari;
    private boolean asuransi;
    private StatusSewa status = StatusSewa.AKTIF;

    public Penyewaan(String id, Kendaraan kendaraan, Pelanggan pelanggan, LocalDate tanggalMulai,
                     int lamaHari, boolean asuransi) throws DataTidakValidException {
        setId(id);
        setKendaraan(kendaraan);
        setPelanggan(pelanggan);
        setTanggalMulai(tanggalMulai);
        setLamaHari(lamaHari);
        this.asuransi = asuransi;
    }

    /** POLIMORFISME: pemanggilan lewat referensi Kendaraan, hasil bergantung jenis objek sebenarnya. */
    public double hitungTotal() {
        return kendaraan.hitungSewa(lamaHari, asuransi);
    }

    public LocalDate getTanggalKembali() {
        return tanggalMulai.plusDays(lamaHari);
    }

    @Override
    public String getId() { return id; }

    public void setId(String id) throws DataTidakValidException {
        this.id = Validasi.wajibIsi(id, "ID penyewaan");
    }

    public Kendaraan getKendaraan() { return kendaraan; }

    public void setKendaraan(Kendaraan kendaraan) throws DataTidakValidException {
        if (kendaraan == null) throw new DataTidakValidException("Kendaraan harus dipilih.");
        this.kendaraan = kendaraan;
    }

    public Pelanggan getPelanggan() { return pelanggan; }

    public void setPelanggan(Pelanggan pelanggan) throws DataTidakValidException {
        if (pelanggan == null) throw new DataTidakValidException("Pelanggan harus dipilih.");
        this.pelanggan = pelanggan;
    }

    public LocalDate getTanggalMulai() { return tanggalMulai; }

    public void setTanggalMulai(LocalDate tanggalMulai) throws DataTidakValidException {
        if (tanggalMulai == null) throw new DataTidakValidException("Tanggal mulai harus diisi.");
        this.tanggalMulai = tanggalMulai;
    }

    public int getLamaHari() { return lamaHari; }

    public void setLamaHari(int lamaHari) throws DataTidakValidException {
        this.lamaHari = Validasi.rentang(lamaHari, 1, 365, "Lama sewa (hari)");
    }

    public boolean isAsuransi() { return asuransi; }

    public void setAsuransi(boolean asuransi) { this.asuransi = asuransi; }

    public StatusSewa getStatus() { return status; }

    public void setStatus(StatusSewa status) { this.status = status; }

    @Override
    public String toCsv() {
        return CsvUtil.gabung(id, kendaraan.getId(), pelanggan.getId(), tanggalMulai, lamaHari, asuransi, status.name());
    }
}
