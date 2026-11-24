package rental.model;

import rental.exception.DataTidakValidException;
import rental.util.Validasi;

/** Turunan Kendaraan: diskon 10% bila sewa 7 hari atau lebih. */
public class Mobil extends Kendaraan {
    private int kapasitas;

    public Mobil(String id, String plat, String merek, double tarifPerHari, int kapasitas)
            throws DataTidakValidException {
        super(id, plat, merek, tarifPerHari); // pemakaian super pada konstruktor
        setKapasitas(kapasitas);
    }

    public int getKapasitas() { return kapasitas; }

    public void setKapasitas(int kapasitas) throws DataTidakValidException {
        this.kapasitas = Validasi.rentang(kapasitas, 2, 15, "Kapasitas penumpang");
    }

    @Override
    public double hitungSewa(int hari) {
        double biaya = getTarifPerHari() * hari;
        return hari >= 7 ? biaya * 0.90 : biaya;
    }

    @Override
    public String getJenis() { return "Mobil"; }

    @Override
    public String getDetail() { return kapasitas + " penumpang"; }

    @Override
    public int getNilaiKhusus() { return kapasitas; }

    @Override
    public String toCsv() {
        return csv(kapasitas);
    }
}
