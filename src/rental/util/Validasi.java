package rental.util;

import rental.exception.DataTidakValidException;

/** Kumpulan pemeriksaan validasi yang dipakai ulang oleh setter pada kelas model. */
public final class Validasi {
    private Validasi() { }

    public static String wajibIsi(String nilai, String namaField) throws DataTidakValidException {
        if (nilai == null || nilai.trim().isEmpty()) {
            throw new DataTidakValidException(namaField + " tidak boleh kosong.");
        }
        return nilai.trim();
    }

    public static double tidakNegatif(double nilai, String namaField) throws DataTidakValidException {
        if (nilai < 0) throw new DataTidakValidException(namaField + " tidak boleh negatif.");
        return nilai;
    }

    public static double positif(double nilai, String namaField) throws DataTidakValidException {
        if (nilai <= 0) throw new DataTidakValidException(namaField + " harus lebih dari 0.");
        return nilai;
    }

    public static int rentang(int nilai, int min, int maks, String namaField) throws DataTidakValidException {
        if (nilai < min || nilai > maks) {
            throw new DataTidakValidException(namaField + " harus antara " + min + " dan " + maks + ".");
        }
        return nilai;
    }

    public static String hanyaAngka(String nilai, int minDigit, int maksDigit, String namaField)
            throws DataTidakValidException {
        String n = wajibIsi(nilai, namaField);
        if (!n.matches("\\d{" + minDigit + "," + maksDigit + "}")) {
            String panjang = (minDigit == maksDigit) ? minDigit + " digit" : minDigit + "-" + maksDigit + " digit";
            throw new DataTidakValidException(namaField + " harus berupa angka (" + panjang + ").");
        }
        return n;
    }

    /** Mengubah teks angka (boleh memuat titik pemisah ribuan) menjadi double, atau melempar exception. */
    public static double parseAngka(String teks, String namaField) throws DataTidakValidException {
        String t = wajibIsi(teks, namaField).replace(".", "").replace(",", ".");
        try {
            return Double.parseDouble(t);
        } catch (NumberFormatException e) {
            throw new DataTidakValidException(namaField + " harus berupa angka.");
        }
    }
}
