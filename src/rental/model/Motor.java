package rental.model;

import rental.exception.DataTidakValidException;
import rental.util.Validasi;

/** Turunan Kendaraan: diskon 5% bila sewa 3 hari atau lebih. */
public class Motor extends Kendaraan {
    private int cc;

    public Motor(String id, String plat, String merek, double tarifPerHari, int cc)
            throws DataTidakValidException {
        super(id, plat, merek, tarifPerHari);
        setCc(cc);
    }

    public int getCc() { return cc; }

    public void setCc(int cc) throws DataTidakValidException {
        this.cc = Validasi.rentang(cc, 50, 1500, "Kapasitas mesin (cc)");
    }

    @Override
    public double hitungSewa(int hari) {
        double biaya = getTarifPerHari() * hari;
        return hari >= 3 ? biaya * 0.95 : biaya;
    }

    @Override
    public String getJenis() { return "Motor"; }

    @Override
    public String getDetail() { return cc + " cc"; }

    @Override
    public int getNilaiKhusus() { return cc; }

    @Override
    public String toCsv() {
        return csv(cc);
    }
}
