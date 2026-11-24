package rental.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import rental.exception.DataTidakValidException;
import rental.exception.PenyimpananException;

/**
 * ABSTRACT CLASS untuk semua halaman data: menyediakan penanganan exception yang seragam
 * dan mewajibkan setiap halaman punya refresh().
 */
abstract class PanelData extends JPanel {
    private static final long serialVersionUID = 1L;

    /** Satu aksi pengguna yang mungkin melempar kesalahan validasi. */
    protected interface Aksi {
        void jalan() throws DataTidakValidException;
    }

    protected PanelData() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
    }

    /** Memuat ulang data dari controller ke tampilan. */
    abstract void refresh();

    /** Menjalankan aksi dan menampilkan pesan informatif bila terjadi kesalahan. */
    protected void jalankan(Aksi aksi) {
        try {
            aksi.jalan();
        } catch (DataTidakValidException e) {
            UiUtil.galat(this, e.getMessage());
        } catch (PenyimpananException e) {
            UiUtil.galat(this, "Gagal mengakses penyimpanan data: " + e.getMessage());
        }
    }

    /** Membungkus form dan tombol menjadi kolom samping dengan lebar tetap (tabel yang melebar saat resize). */
    protected JPanel kolomSamping(JPanel form, JPanel tombol, int lebar) {
        JPanel atas = new JPanel(new BorderLayout(0, 10));
        atas.add(form, BorderLayout.NORTH);
        atas.add(tombol, BorderLayout.CENTER);
        JPanel samping = new JPanel(new BorderLayout());
        samping.add(atas, BorderLayout.NORTH);
        samping.setPreferredSize(new Dimension(lebar, 0));
        return samping;
    }
}
