package rental.model;

import rental.exception.DataTidakValidException;
import rental.util.CsvUtil;
import rental.util.Validasi;

/** Turunan Kendaraan: tidak ada diskon, tetapi wajib biaya sopir per hari. */
public class Bus extends Kendaraan {
    public static final double BIAYA_SOPIR_PER_HARI = 250_000;

    private int jumlahKursi;

    public Bus(String id, String plat, String merek, double tarifPerHari, int jumlahKursi)
            throws DataTidakValidException {
        super(id, plat, merek, tarifPerHari);
        setJumlahKursi(jumlahKursi);
    }

    public int getJumlahKursi() { return jumlahKursi; }

    public void setJumlahKursi(int jumlahKursi) throws DataTidakValidException {
        this.jumlahKursi = Validasi.rentang(jumlahKursi, 16, 80, "Jumlah kursi");
    }

    @Override
    public double hitungSewa(int hari) {
        return (getTarifPerHari() + BIAYA_SOPIR_PER_HARI) * hari;
    }

    @Override
    public String getJenis() { return "Bus"; }

    @Override
    public String getDetail() { return jumlahKursi + " kursi (+sopir)"; }

    @Override
    public int getNilaiKhusus() { return jumlahKursi; }

    @Override
    public String toCsv() {
        return CsvUtil.gabung(getJenis(), getId(), getPlat(), getMerek(), getTarifPerHari(), isTersedia(), jumlahKursi);
    }
}
