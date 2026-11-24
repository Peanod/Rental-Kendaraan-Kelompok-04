package rental.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import rental.AppContext;
import rental.model.Bus;
import rental.model.Kendaraan;
import rental.util.Format;

/** Halaman beranda: ringkasan angka penting dan aturan tarif. */
class DashboardPanel extends PanelData {
    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final JLabel lblTotal = angka();
    private final JLabel lblTersedia = angka();
    private final JLabel lblAktif = angka();
    private final JLabel lblPendapatan = angka();

    DashboardPanel(AppContext ctx) {
        this.ctx = ctx;

        JLabel judul = new JLabel("Dashboard Rental Kendaraan");
        judul.setFont(judul.getFont().deriveFont(Font.BOLD, 22f));
        add(judul, BorderLayout.NORTH);

        JPanel kartu = new JPanel(new GridLayout(1, 4, 12, 0));
        kartu.add(buatKartu("Total Kendaraan", lblTotal));
        kartu.add(buatKartu("Kendaraan Tersedia", lblTersedia));
        kartu.add(buatKartu("Sewa Aktif", lblAktif));
        kartu.add(buatKartu("Pendapatan (selesai)", lblPendapatan));

        JTextArea aturan = new JTextArea(
                "Aturan hitung sewa (polimorfisme hitungSewa):\n\n"
              + "  \u2022 Mobil : tarif x hari, diskon 10% bila sewa 7 hari atau lebih.\n"
              + "  \u2022 Motor : tarif x hari, diskon 5% bila sewa 3 hari atau lebih.\n"
              + "  \u2022 Bus   : (tarif + sopir " + Format.rupiah(Bus.BIAYA_SOPIR_PER_HARI)
              + "/hari) x hari, tanpa diskon.\n"
              + "  \u2022 Asuransi opsional: +" + (int) (Kendaraan.TARIF_ASURANSI * 100) + "% dari biaya sewa.");
        aturan.setEditable(false);
        aturan.setOpaque(false);
        aturan.setFont(aturan.getFont().deriveFont(14f));
        aturan.setBorder(BorderFactory.createTitledBorder("Informasi Tarif"));

        JPanel tengah = new JPanel(new BorderLayout(0, 16));
        tengah.add(kartu, BorderLayout.NORTH);
        tengah.add(aturan, BorderLayout.CENTER);
        add(tengah, BorderLayout.CENTER);
    }

    private static JLabel angka() {
        JLabel l = new JLabel("0", SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(Font.BOLD, 26f));
        return l;
    }

    private static JPanel buatKartu(String judul, JLabel nilai) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(judul), BorderFactory.createEmptyBorder(12, 8, 12, 8)));
        p.add(nilai, BorderLayout.CENTER);
        return p;
    }

    @Override
    void refresh() {
        lblTotal.setText(String.valueOf(ctx.kendaraan.jumlahSemua()));
        lblTersedia.setText(String.valueOf(ctx.kendaraan.jumlahTersedia()));
        lblAktif.setText(String.valueOf(ctx.penyewaan.jumlahAktif()));
        lblPendapatan.setText(Format.rupiah(ctx.penyewaan.totalPendapatan()));
    }
}
