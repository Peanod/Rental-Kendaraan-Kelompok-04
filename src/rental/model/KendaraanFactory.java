package rental.model;

import rental.exception.DataTidakValidException;
import rental.util.Validasi;

/** Pabrik objek: membuat subkelas Kendaraan yang tepat; hasilnya dipegang lewat referensi kelas induk. */
public final class KendaraanFactory {
    public static final String[] JENIS = { "Mobil", "Motor", "Bus" };

    private KendaraanFactory() { }

    public static Kendaraan buat(String jenis, String id, String plat, String merek, double tarif, int nilaiKhusus)
            throws DataTidakValidException {
        switch (Validasi.wajibIsi(jenis, "Jenis kendaraan")) {
            case "Mobil": return new Mobil(id, plat, merek, tarif, nilaiKhusus);
            case "Motor": return new Motor(id, plat, merek, tarif, nilaiKhusus);
            case "Bus":   return new Bus(id, plat, merek, tarif, nilaiKhusus);
            default: throw new DataTidakValidException("Jenis kendaraan tidak dikenal: " + jenis);
        }
    }

    /** Format CSV: jenis,id,plat,merek,tarif,tersedia,nilaiKhusus */
    public static Kendaraan dariCsv(String[] f) throws DataTidakValidException {
        if (f.length < 7) throw new DataTidakValidException("Baris data kendaraan rusak.");
        try {
            Kendaraan k = buat(f[0], f[1], f[2], f[3], Double.parseDouble(f[4]), Integer.parseInt(f[6]));
            k.setTersedia(Boolean.parseBoolean(f[5]));
            return k;
        } catch (NumberFormatException e) {
            throw new DataTidakValidException("Baris data kendaraan rusak (angka tidak valid).");
        }
    }
}
