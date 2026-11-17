package rental.model;

import rental.exception.DataTidakValidException;
import rental.util.CsvUtil;
import rental.util.Validasi;

/** Data penyewa kendaraan. */
public class Pelanggan implements DapatDisimpan {
    private String id;
    private String nama;
    private String noKtp;
    private String telepon;
    private String alamat;
    private String jenisKelamin; // "L" atau "P"

    public Pelanggan(String id, String nama, String noKtp, String telepon, String alamat, String jenisKelamin)
            throws DataTidakValidException {
        setId(id);
        setNama(nama);
        setNoKtp(noKtp);
        setTelepon(telepon);
        setAlamat(alamat);
        setJenisKelamin(jenisKelamin);
    }

    @Override
    public String getId() { return id; }

    public void setId(String id) throws DataTidakValidException {
        this.id = Validasi.wajibIsi(id, "ID pelanggan");
    }

    public String getNama() { return nama; }

    public void setNama(String nama) throws DataTidakValidException {
        this.nama = Validasi.wajibIsi(nama, "Nama");
    }

    public String getNoKtp() { return noKtp; }

    public void setNoKtp(String noKtp) throws DataTidakValidException {
        this.noKtp = Validasi.hanyaAngka(noKtp, 16, 16, "No. KTP");
    }

    public String getTelepon() { return telepon; }

    public void setTelepon(String telepon) throws DataTidakValidException {
        this.telepon = Validasi.hanyaAngka(telepon, 9, 15, "No. telepon");
    }

    public String getAlamat() { return alamat; }

    public void setAlamat(String alamat) throws DataTidakValidException {
        this.alamat = Validasi.wajibIsi(alamat, "Alamat");
    }

    public String getJenisKelamin() { return jenisKelamin; }

    public void setJenisKelamin(String jenisKelamin) throws DataTidakValidException {
        if (!"L".equals(jenisKelamin) && !"P".equals(jenisKelamin)) {
            throw new DataTidakValidException("Jenis kelamin harus dipilih.");
        }
        this.jenisKelamin = jenisKelamin;
    }

    @Override
    public String toCsv() {
        return CsvUtil.gabung(id, nama, noKtp, telepon, alamat, jenisKelamin);
    }

    public static Pelanggan dariCsv(String[] f) throws DataTidakValidException {
        if (f.length < 6) throw new DataTidakValidException("Baris data pelanggan rusak.");
        return new Pelanggan(f[0], f[1], f[2], f[3], f[4], f[5]);
    }

    @Override
    public String toString() {
        return id + " - " + nama;
    }
}
