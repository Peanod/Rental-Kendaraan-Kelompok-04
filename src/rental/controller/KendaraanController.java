package rental.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.imageio.ImageIO;

import rental.dao.KendaraanDao;
import rental.dao.PenyewaanDao;
import rental.exception.DataTidakValidException;
import rental.exception.PenyimpananException;
import rental.model.Kendaraan;
import rental.model.KendaraanFactory;
import rental.util.Validasi;

/** Logika bisnis data kendaraan (CRUD + pencarian + foto). View hanya memanggil method di sini. */
public class KendaraanController {
    /** Ukuran maksimum berkas foto (5 MB) agar penyimpanan dan tampilan tetap ringan. */
    public static final long MAKS_UKURAN_FOTO = 5L * 1024 * 1024;
    public static final String[] EKSTENSI_FOTO = { "jpg", "jpeg", "png", "gif" };

    private final KendaraanDao dao;
    private final PenyewaanDao penyewaanDao;
    private final Path folderFoto;

    public KendaraanController(KendaraanDao dao, PenyewaanDao penyewaanDao, Path folderFoto) {
        this.dao = dao;
        this.penyewaanDao = penyewaanDao;
        this.folderFoto = folderFoto;
    }

    /** jenis = null/"Semua" berarti tanpa filter jenis. */
    public List<Kendaraan> daftar(String kataKunci, String jenis) {
        String q = kataKunci == null ? "" : kataKunci.trim().toLowerCase();
        List<Kendaraan> hasil = new ArrayList<>();
        for (Kendaraan k : dao.semua()) {
            boolean cocokJenis = jenis == null || "Semua".equals(jenis) || k.getJenis().equals(jenis);
            boolean cocokTeks = q.isEmpty() || k.getPlat().toLowerCase().contains(q)
                    || k.getMerek().toLowerCase().contains(q) || k.getId().toLowerCase().contains(q);
            if (cocokJenis && cocokTeks) hasil.add(k);
        }
        return hasil;
    }

    public Kendaraan tambah(String jenis, String plat, String merek, String tarifTeks, int nilaiKhusus)
            throws DataTidakValidException {
        return tambah(jenis, plat, merek, tarifTeks, nilaiKhusus, "", null);
    }

    /** fotoSumber boleh null (tanpa foto); bila diisi, berkas disalin ke folder foto aplikasi. */
    public Kendaraan tambah(String jenis, String plat, String merek, String tarifTeks, int nilaiKhusus,
            String deskripsi, File fotoSumber) throws DataTidakValidException {
        double tarif = Validasi.parseAngka(tarifTeks, "Tarif per hari");
        String id = dao.idBerikut();
        Kendaraan k = KendaraanFactory.buat(jenis, id, plat, merek, tarif, nilaiKhusus, deskripsi, "");
        if (dao.platDipakai(k.getPlat(), null)) {
            throw new DataTidakValidException("Plat nomor " + k.getPlat() + " sudah terdaftar.");
        }
        if (fotoSumber != null) {
            cekFoto(fotoSumber);
            k.setFoto(salinFoto(id, fotoSumber));
        }
        try {
            dao.tambah(k);
        } catch (RuntimeException | DataTidakValidException e) {
            hapusBerkas(k.getFoto()); // jangan tinggalkan foto yatim bila data gagal disimpan
            throw e;
        }
        return k;
    }

    /** Versi lama: deskripsi dan foto yang sudah ada tidak diubah. */
    public Kendaraan ubah(String id, String jenis, String plat, String merek, String tarifTeks, int nilaiKhusus)
            throws DataTidakValidException {
        Kendaraan lama = dao.cari(id).orElseThrow(() -> new DataTidakValidException("Kendaraan tidak ditemukan."));
        return ubah(id, jenis, plat, merek, tarifTeks, nilaiKhusus, lama.getDeskripsi(), null, false);
    }

    /**
     * fotoBaru != null mengganti foto; hapusFoto = true menghapus foto yang ada;
     * keduanya tidak diisi berarti foto lama dipertahankan.
     */
    public Kendaraan ubah(String id, String jenis, String plat, String merek, String tarifTeks, int nilaiKhusus,
            String deskripsi, File fotoBaru, boolean hapusFoto) throws DataTidakValidException {
        Kendaraan lama = dao.cari(id).orElseThrow(() -> new DataTidakValidException("Kendaraan tidak ditemukan."));
        double tarif = Validasi.parseAngka(tarifTeks, "Tarif per hari");
        Kendaraan baru = KendaraanFactory.buat(jenis, id, plat, merek, tarif, nilaiKhusus, deskripsi, lama.getFoto());
        if (dao.platDipakai(baru.getPlat(), id)) {
            throw new DataTidakValidException("Plat nomor " + baru.getPlat() + " sudah terdaftar.");
        }
        if (fotoBaru != null) {
            cekFoto(fotoBaru);
            String nama = salinFoto(id, fotoBaru);
            if (!nama.equals(lama.getFoto())) hapusBerkas(lama.getFoto()); // ekstensi berbeda: buang yang lama
            baru.setFoto(nama);
        } else if (hapusFoto) {
            hapusBerkas(lama.getFoto());
            baru.setFoto("");
        }
        baru.setTersedia(lama.isTersedia());
        dao.ubah(baru);
        return baru;
    }

    public void hapus(String id) throws DataTidakValidException {
        if (penyewaanDao.adaKendaraan(id)) {
            throw new DataTidakValidException(
                    "Kendaraan tidak dapat dihapus karena tercatat pada transaksi penyewaan.");
        }
        Optional<Kendaraan> lama = dao.cari(id);
        dao.hapus(id);
        lama.ifPresent(k -> hapusBerkas(k.getFoto()));
    }

    // ------------------------------------------------------------------- foto
    /** Memeriksa berkas foto: harus ada, berekstensi gambar, maksimal 5 MB, dan benar-benar bisa dibaca. */
    public void cekFoto(File berkas) throws DataTidakValidException {
        if (berkas == null || !berkas.isFile()) {
            throw new DataTidakValidException("Berkas foto tidak ditemukan.");
        }
        String ekstensi = ekstensi(berkas.getName());
        boolean didukung = false;
        for (String e : EKSTENSI_FOTO) if (e.equals(ekstensi)) didukung = true;
        if (!didukung) {
            throw new DataTidakValidException("Format foto harus JPG, PNG, atau GIF.");
        }
        if (berkas.length() > MAKS_UKURAN_FOTO) {
            throw new DataTidakValidException("Ukuran foto maksimal 5 MB.");
        }
        try {
            if (ImageIO.read(berkas) == null) {
                throw new DataTidakValidException("Berkas bukan gambar yang valid.");
            }
        } catch (IOException e) {
            throw new DataTidakValidException("Berkas foto tidak dapat dibaca.");
        }
    }

    /** Berkas foto kendaraan di disk, atau null bila belum ada foto / berkasnya hilang. */
    public File berkasFoto(Kendaraan k) {
        if (k == null || !k.adaFoto()) return null;
        File f = folderFoto.resolve(k.getFoto()).toFile();
        return f.isFile() ? f : null;
    }

    private String salinFoto(String id, File sumber) {
        String nama = id + "." + ekstensi(sumber.getName());
        try {
            Files.createDirectories(folderFoto);
            Files.copy(sumber.toPath(), folderFoto.resolve(nama), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new PenyimpananException("Gagal menyimpan foto kendaraan.", e);
        }
        return nama;
    }

    private void hapusBerkas(String nama) {
        if (nama == null || nama.isEmpty()) return;
        try {
            Files.deleteIfExists(folderFoto.resolve(nama));
        } catch (IOException e) {
            // pembersihan berkas bersifat best-effort; data kendaraan tetap konsisten
        }
    }

    private static String ekstensi(String namaBerkas) {
        int titik = namaBerkas.lastIndexOf('.');
        return titik < 0 ? "" : namaBerkas.substring(titik + 1).toLowerCase(Locale.ROOT);
    }

    // ------------------------------------------------------------- statistik
    public int jumlahSemua() { return dao.semua().size(); }

    public int jumlahTersedia() {
        int n = 0;
        for (Kendaraan k : dao.semua()) if (k.isTersedia()) n++;
        return n;
    }

    public Optional<Kendaraan> cari(String id) { return dao.cari(id); }
}
