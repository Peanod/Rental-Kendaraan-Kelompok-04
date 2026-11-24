package rental.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * Komponen pratinjau foto kendaraan: gambar diskalakan proporsional agar muat di kotak tetap.
 * Bila belum ada foto (atau berkas rusak), ditampilkan teks pengganti.
 */
class FotoPreview extends JLabel {
    private static final long serialVersionUID = 1L;

    static final int LEBAR = 260;
    static final int TINGGI = 170;

    FotoPreview() {
        super("Belum ada foto", SwingConstants.CENTER);
        setPreferredSize(new Dimension(LEBAR, TINGGI));
        setMinimumSize(new Dimension(LEBAR, TINGGI));
        setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setOpaque(true);
        setBackground(Color.WHITE);
        setForeground(Color.GRAY);
    }

    /** Menampilkan foto dari berkas; null berarti kosong. */
    void tampilkan(File berkas) {
        if (berkas == null) {
            kosong("Belum ada foto");
            return;
        }
        try {
            BufferedImage gambar = ImageIO.read(berkas);
            if (gambar == null) {
                kosong("Foto tidak dapat dibaca");
                return;
            }
            double skala = Math.min((LEBAR - 4.0) / gambar.getWidth(), (TINGGI - 4.0) / gambar.getHeight());
            int w = Math.max(1, (int) Math.round(gambar.getWidth() * skala));
            int h = Math.max(1, (int) Math.round(gambar.getHeight() * skala));
            setText(null);
            setIcon(new ImageIcon(gambar.getScaledInstance(w, h, Image.SCALE_SMOOTH)));
        } catch (IOException e) {
            kosong("Foto tidak dapat dibaca");
        }
    }

    void kosong(String pesan) {
        setIcon(null);
        setText(pesan);
    }
}
