package rental.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import rental.exception.DataTidakValidException;
import rental.util.CsvUtil;
import rental.util.Validasi;

/** Akun yang boleh login ke aplikasi (kata sandi disimpan sebagai hash SHA-256). */
public class Pengguna implements DapatDisimpan {
    private String username;
    private String hashSandi;
    private String namaLengkap;
    private String peran; // ADMIN / PETUGAS

    public Pengguna(String username, String hashSandi, String namaLengkap, String peran)
            throws DataTidakValidException {
        this.username = Validasi.wajibIsi(username, "Username");
        this.hashSandi = Validasi.wajibIsi(hashSandi, "Kata sandi");
        this.namaLengkap = Validasi.wajibIsi(namaLengkap, "Nama lengkap");
        this.peran = Validasi.wajibIsi(peran, "Peran");
    }

    public static Pengguna baru(String username, String sandiAsli, String namaLengkap, String peran)
            throws DataTidakValidException {
        return new Pengguna(username, hash(Validasi.wajibIsi(sandiAsli, "Kata sandi")), namaLengkap, peran);
    }

    public boolean cocok(String sandiAsli) {
        return hashSandi.equals(hash(sandiAsli));
    }

    public static String hash(String teks) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            StringBuilder sb = new StringBuilder();
            for (byte b : md.digest(teks.getBytes(StandardCharsets.UTF_8))) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getId() { return username; }

    public String getUsername() { return username; }

    public String getNamaLengkap() { return namaLengkap; }

    public String getPeran() { return peran; }

    @Override
    public String toCsv() {
        return CsvUtil.gabung(username, hashSandi, namaLengkap, peran);
    }

    public static Pengguna dariCsv(String[] f) throws DataTidakValidException {
        if (f.length < 4) throw new DataTidakValidException("Baris data pengguna rusak.");
        return new Pengguna(f[0], f[1], f[2], f[3]);
    }
}
