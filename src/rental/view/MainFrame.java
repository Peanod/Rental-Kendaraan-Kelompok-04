package rental.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import rental.AppContext;
import rental.model.Pengguna;

/**
 * Tampilan 2: jendela utama. Menu bar + toolbar yang konsisten untuk berpindah antar halaman
 * (Dashboard, Kendaraan, Pelanggan, Penyewaan) yang disusun dengan CardLayout.
 */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final CardLayout kartu = new CardLayout();
    private final JPanel isi = new JPanel(kartu);
    private final Map<String, PanelData> halaman = new LinkedHashMap<>();

    public MainFrame(AppContext ctx) {
        super("Rental Kendaraan");
        this.ctx = ctx;
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) { keluarAplikasi(); }
        });

        halaman.put("Dashboard", new DashboardPanel(ctx));
        halaman.put("Kendaraan", new KendaraanPanel(ctx.kendaraan));
        halaman.put("Pelanggan", new PelangganPanel(ctx.pelanggan));
        halaman.put("Penyewaan", new PenyewaanPanel(ctx.penyewaan));
        for (Map.Entry<String, PanelData> e : halaman.entrySet()) isi.add(e.getValue(), e.getKey());

        setJMenuBar(buatMenuBar());
        add(buatToolbar(), BorderLayout.NORTH);
        add(isi, BorderLayout.CENTER);
        add(buatStatusBar(), BorderLayout.SOUTH);

        setMinimumSize(new Dimension(960, 560));
        setSize(1120, 680);
        setLocationRelativeTo(null);
        tampilkan("Dashboard");
    }

    // ------------------------------------------------------------ navigasi
    private void tampilkan(String nama) {
        halaman.get(nama).refresh();
        kartu.show(isi, nama);
        setTitle("Rental Kendaraan - " + nama);
    }

    private JMenuBar buatMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu berkas = new JMenu("Berkas");
        berkas.add(itemMenu("Keluar Akun (Logout)", null, this::logout));
        berkas.addSeparator();
        berkas.add(itemMenu("Keluar Aplikasi", null, this::keluarAplikasi));
        bar.add(berkas);

        JMenu data = new JMenu("Data");
        int tombolAngka = KeyEvent.VK_1;
        for (String nama : halaman.keySet()) {
            final String tujuan = nama;
            data.add(itemMenu(nama, KeyStroke.getKeyStroke(tombolAngka++, java.awt.event.InputEvent.CTRL_DOWN_MASK),
                    () -> tampilkan(tujuan)));
        }
        bar.add(data);

        JMenu bantuan = new JMenu("Bantuan");
        bantuan.add(itemMenu("Tentang Aplikasi", null, this::tentang));
        bar.add(bantuan);
        return bar;
    }

    private JMenuItem itemMenu(String teks, KeyStroke pintasan, Runnable aksi) {
        JMenuItem item = new JMenuItem(teks);
        if (pintasan != null) item.setAccelerator(pintasan);
        item.addActionListener(e -> aksi.run());
        return item;
    }

    private JToolBar buatToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        for (String nama : halaman.keySet()) {
            final String tujuan = nama;
            JButton b = new JButton(nama);
            b.addActionListener(e -> tampilkan(tujuan));
            tb.add(b);
        }
        return tb;
    }

    private JPanel buatStatusBar() {
        Pengguna p = ctx.auth.getPenggunaAktif();
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        bar.add(new JLabel("Masuk sebagai: " + p.getNamaLengkap() + " (" + p.getPeran() + ")"), BorderLayout.WEST);
        return bar;
    }

    // --------------------------------------------------------------- aksi
    private void logout() {
        if (!UiUtil.konfirmasi(this, "Keluar dari akun ini?")) return;
        ctx.auth.keluar();
        dispose();
        new LoginFrame(ctx).setVisible(true);
    }

    private void keluarAplikasi() {
        if (UiUtil.konfirmasi(this, "Tutup aplikasi Rental Kendaraan?")) {
            dispose();
            System.exit(0);
        }
    }

    private void tentang() {
        JOptionPane.showMessageDialog(this,
                "Aplikasi Rental Kendaraan\n"
              + "Tugas Proyek PBO - Implementasi GUI 2026\n\n"
              + "Kelompok 4:\n"
              + "  \u2022 Finodi\n"
              + "  \u2022 I Dewa Gede Agung Oka\n"
              + "  \u2022 Lionel Jeremy",
                "Tentang", JOptionPane.INFORMATION_MESSAGE);
    }
}
