package rental.dao;

import rental.exception.DataTidakValidException;
import rental.model.Kendaraan;
import rental.model.KendaraanFactory;
import rental.model.Pelanggan;

/** Mengisi data contoh saat aplikasi pertama kali dijalankan (agar demo tidak kosong). */
public final class DataAwal {
    private DataAwal() { }

    public static void isiJikaKosong(KendaraanDao kendaraanDao, PelangganDao pelangganDao) {
        try {
            if (kendaraanDao.semua().isEmpty()) {
                // kolom terakhir: deskripsi kondisi (data contoh; ganti sesuai kondisi kendaraan sebenarnya)
                Object[][] k = {
                    { "Mobil", "H 1234 AB", "Toyota Avanza", 350_000.0, 7,
                        "Tahun 2021, transmisi manual, AC dingin, interior bersih tanpa bau rokok. "
                        + "Ban depan baru diganti, servis berkala terakhir bulan lalu." },
                    { "Mobil", "H 5678 CD", "Honda Brio", 300_000.0, 5,
                        "Tahun 2022, transmisi matic, irit BBM dan lincah untuk dalam kota. "
                        + "Body mulus, ada goresan tipis di bumper belakang." },
                    { "Mobil", "H 9012 EF", "Toyota Innova Zenix", 650_000.0, 7,
                        "Tahun 2023, hybrid matic, kabin luas dengan captain seat baris kedua. "
                        + "Kondisi sangat baik, dilengkapi kamera mundur dan charger USB." },
                    { "Motor", "H 3456 GH", "Honda Vario 160", 100_000.0, 160,
                        "Tahun 2022, matic, body mulus tanpa lecet berarti, rem dan ban masih tebal. "
                        + "Termasuk 2 helm SNI dan jas hujan." },
                    { "Motor", "H 7890 IJ", "Yamaha NMAX", 150_000.0, 155,
                        "Tahun 2021, matic, nyaman untuk perjalanan jauh. Ada baret halus di bodi samping kiri, "
                        + "mesin halus. Termasuk 2 helm dan bagasi luas." },
                    { "Motor", "H 2468 KL", "Honda Beat", 75_000.0, 110,
                        "Tahun 2020, matic, ringan dan hemat. Cat sedikit pudar di sayap depan, "
                        + "mesin dan rem berfungsi baik. Termasuk 1 helm." },
                    { "Bus", "H 1357 MN", "Hino Medium Bus", 1_500_000.0, 35,
                        "Tahun 2019, AC dan sound system berfungsi, kursi reclining bersih. "
                        + "Cocok untuk rombongan wisata, sudah termasuk sopir berpengalaman." },
                    { "Bus", "H 8642 OP", "Mercedes-Benz OH 1626", 2_800_000.0, 59,
                        "Tahun 2022, kelas big bus, kursi 2-2 dengan legrest, AC, TV, dan bagasi besar. "
                        + "Kondisi prima, sudah termasuk sopir." },
                };
                for (Object[] r : k) {
                    Kendaraan kd = KendaraanFactory.buat((String) r[0], kendaraanDao.idBerikut(), (String) r[1],
                            (String) r[2], (Double) r[3], (Integer) r[4], (String) r[5], "");
                    kendaraanDao.tambah(kd);
                }
            }
            if (pelangganDao.semua().isEmpty()) {
                String[][] p = {
                    { "Budi Santoso", "3374010101900001", "081234567890", "Jl. Pandanaran No. 10, Semarang", "L" },
                    { "Siti Rahayu", "3374014502920002", "081298765432", "Jl. Pemuda No. 25, Semarang", "P" },
                    { "Ahmad Fauzi", "3374012003880003", "085712345678", "Jl. Setiabudi No. 7, Semarang", "L" },
                };
                for (String[] r : p) {
                    pelangganDao.tambah(new Pelanggan(pelangganDao.idBerikut(), r[0], r[1], r[2], r[3], r[4]));
                }
            }
        } catch (DataTidakValidException e) {
            throw new IllegalStateException("Data awal tidak valid: " + e.getMessage(), e);
        }
    }
}
