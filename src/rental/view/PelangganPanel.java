package rental.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import rental.controller.PelangganController;
import rental.exception.DataTidakValidException;
import rental.model.Pelanggan;

/** Halaman CRUD data pelanggan. */
class PelangganPanel extends PanelData {
    private static final long serialVersionUID = 1L;

    private final PelangganController controller;

    private final DefaultTableModel model =
            UiUtil.modelTabel("ID", "Nama", "No. KTP", "Telepon", "Alamat", "JK");
    private final JTable tabel = UiUtil.buatTabel(model);

    private final JTextField txtCari = new JTextField(20);

    private final JLabel lblId = new JLabel("(otomatis)");
    private final JTextField txtNama = new JTextField();
    private final JTextField txtKtp = new JTextField();
    private final JTextField txtTelepon = new JTextField();
    private final JTextArea txtAlamat = new JTextArea(3, 20);
    private final JRadioButton rdoLaki = new JRadioButton("Laki-laki");
    private final JRadioButton rdoPerempuan = new JRadioButton("Perempuan");
    private final ButtonGroup grupJk = new ButtonGroup();

    private String idTerpilih;

    PelangganPanel(PelangganController controller) {
        this.controller = controller;
        susunTampilan();
        pasangEvent();
    }

    private void susunTampilan() {
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        atas.add(new JLabel("Cari (nama / KTP / telepon):"));
        atas.add(txtCari);
        add(atas, BorderLayout.NORTH);

        add(new JScrollPane(tabel), BorderLayout.CENTER);

        grupJk.add(rdoLaki);
        grupJk.add(rdoPerempuan);
        JPanel panelJk = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        panelJk.add(rdoLaki);
        panelJk.add(rdoPerempuan);

        txtAlamat.setLineWrap(true);
        txtAlamat.setWrapStyleWord(true);

        JPanel form = UiUtil.panelForm();
        form.setBorder(BorderFactory.createTitledBorder("Data Pelanggan"));
        UiUtil.tambahField(form, 0, "ID", lblId);
        UiUtil.tambahField(form, 1, "Nama", txtNama);
        UiUtil.tambahField(form, 2, "No. KTP (16 digit)", txtKtp);
        UiUtil.tambahField(form, 3, "No. Telepon", txtTelepon);
        UiUtil.tambahField(form, 4, "Jenis Kelamin", panelJk);
        UiUtil.tambahField(form, 5, "Alamat", new JScrollPane(txtAlamat));

        JPanel tombol = new JPanel(new GridLayout(2, 2, 6, 6));
        tombol.add(buatTombol("Tambah", this::aksiTambah));
        tombol.add(buatTombol("Ubah", this::aksiUbah));
        tombol.add(buatTombol("Hapus", this::aksiHapus));
        tombol.add(buatTombol("Bersihkan", this::bersihkanForm));

        add(kolomSamping(form, tombol, 340), BorderLayout.EAST);
    }

    private JButton buatTombol(String teks, Runnable aksi) {
        JButton b = new JButton(teks);
        b.addActionListener(e -> aksi.run());
        return b;
    }

    private void pasangEvent() {
        txtCari.getDocument().addDocumentListener(UiUtil.saatBerubah(this::refresh));
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiFormDariBaris();
        });
    }

    private String jenisKelaminTerpilih() {
        if (rdoLaki.isSelected()) return "L";
        if (rdoPerempuan.isSelected()) return "P";
        return null;
    }

    private void isiFormDariBaris() {
        String id = UiUtil.idTerpilih(tabel, model);
        if (id == null) return;
        controller.cari(id).ifPresent(p -> {
            idTerpilih = p.getId();
            lblId.setText(p.getId());
            txtNama.setText(p.getNama());
            txtKtp.setText(p.getNoKtp());
            txtTelepon.setText(p.getTelepon());
            txtAlamat.setText(p.getAlamat());
            rdoLaki.setSelected("L".equals(p.getJenisKelamin()));
            rdoPerempuan.setSelected("P".equals(p.getJenisKelamin()));
        });
    }

    private void aksiTambah() {
        jalankan(() -> {
            controller.tambah(txtNama.getText(), txtKtp.getText(), txtTelepon.getText(),
                    txtAlamat.getText(), jenisKelaminTerpilih());
            UiUtil.info(this, "Pelanggan berhasil ditambahkan.");
            bersihkanForm();
            refresh();
        });
    }

    private void aksiUbah() {
        jalankan(() -> {
            pastikanAdaPilihan();
            controller.ubah(idTerpilih, txtNama.getText(), txtKtp.getText(), txtTelepon.getText(),
                    txtAlamat.getText(), jenisKelaminTerpilih());
            UiUtil.info(this, "Data pelanggan berhasil diubah.");
            bersihkanForm();
            refresh();
        });
    }

    private void aksiHapus() {
        jalankan(() -> {
            pastikanAdaPilihan();
            if (!UiUtil.konfirmasi(this, "Hapus pelanggan " + txtNama.getText() + " ?")) return;
            controller.hapus(idTerpilih);
            bersihkanForm();
            refresh();
        });
    }

    private void pastikanAdaPilihan() throws DataTidakValidException {
        if (idTerpilih == null) throw new DataTidakValidException("Pilih pelanggan pada tabel terlebih dahulu.");
    }

    private void bersihkanForm() {
        idTerpilih = null;
        lblId.setText("(otomatis)");
        txtNama.setText("");
        txtKtp.setText("");
        txtTelepon.setText("");
        txtAlamat.setText("");
        grupJk.clearSelection();
        tabel.clearSelection();
    }

    @Override
    void refresh() {
        java.util.List<Object[]> baris = new java.util.ArrayList<>();
        for (Pelanggan p : controller.daftar(txtCari.getText())) {
            baris.add(new Object[] { p.getId(), p.getNama(), p.getNoKtp(), p.getTelepon(),
                    p.getAlamat(), "L".equals(p.getJenisKelamin()) ? "L" : "P" });
        }
        UiUtil.isiTabel(tabel, model, baris);
    }
}
