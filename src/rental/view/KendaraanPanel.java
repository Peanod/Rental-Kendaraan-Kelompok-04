package rental.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import rental.controller.KendaraanController;
import rental.exception.DataTidakValidException;
import rental.model.Kendaraan;
import rental.model.KendaraanFactory;
import rental.util.Format;

/** Halaman CRUD data kendaraan (Mobil / Motor / Bus). */
class KendaraanPanel extends PanelData {
    private static final long serialVersionUID = 1L;

    private final KendaraanController controller;

    private final DefaultTableModel model =
            UiUtil.modelTabel("ID", "Jenis", "Plat", "Merek", "Tarif / Hari", "Detail", "Status");
    private final JTable tabel = UiUtil.buatTabel(model);

    private final JTextField txtCari = new JTextField(18);
    private final JComboBox<String> cmbFilter = new JComboBox<>(new String[] { "Semua", "Mobil", "Motor", "Bus" });

    private final JLabel lblId = new JLabel("(otomatis)");
    private final JComboBox<String> cmbJenis = new JComboBox<>(KendaraanFactory.JENIS);
    private final JTextField txtPlat = new JTextField();
    private final JTextField txtMerek = new JTextField();
    private final JTextField txtTarif = new JTextField();
    private final JLabel lblKhusus = new JLabel();
    private final JSpinner spnKhusus = new JSpinner(new SpinnerNumberModel(5, 2, 15, 1));

    private String idTerpilih;

    KendaraanPanel(KendaraanController controller) {
        this.controller = controller;
        susunTampilan();
        pasangEvent();
        aturSpinnerSesuaiJenis();
    }

    // ---------------------------------------------------------------- tampilan
    private void susunTampilan() {
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        atas.add(new JLabel("Cari:"));
        atas.add(txtCari);
        atas.add(new JLabel("Jenis:"));
        atas.add(cmbFilter);
        add(atas, BorderLayout.NORTH);

        add(new JScrollPane(tabel), BorderLayout.CENTER);

        JPanel form = UiUtil.panelForm();
        form.setBorder(BorderFactory.createTitledBorder("Data Kendaraan"));
        UiUtil.tambahField(form, 0, "ID", lblId);
        UiUtil.tambahField(form, 1, "Jenis", cmbJenis);
        UiUtil.tambahField(form, 2, "Plat Nomor", txtPlat);
        UiUtil.tambahField(form, 3, "Merek / Tipe", txtMerek);
        UiUtil.tambahField(form, 4, "Tarif per Hari (Rp)", txtTarif);
        UiUtil.tambahField(form, 5, lblKhusus, spnKhusus);

        JPanel tombol = new JPanel(new GridLayout(2, 2, 6, 6));
        tombol.add(buatTombol("Tambah", this::aksiTambah));
        tombol.add(buatTombol("Ubah", this::aksiUbah));
        tombol.add(buatTombol("Hapus", this::aksiHapus));
        tombol.add(buatTombol("Bersihkan", this::bersihkanForm));

        add(kolomSamping(form, tombol, 330), BorderLayout.EAST);
    }

    private JButton buatTombol(String teks, Runnable aksi) {
        JButton b = new JButton(teks);
        b.addActionListener(e -> aksi.run());
        return b;
    }

    // ------------------------------------------------------------------- event
    private void pasangEvent() {
        txtCari.getDocument().addDocumentListener(UiUtil.saatBerubah(this::refresh));
        cmbFilter.addActionListener(e -> refresh());
        cmbJenis.addActionListener(e -> aturSpinnerSesuaiJenis());
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiFormDariBaris();
        });
    }

    private void aturSpinnerSesuaiJenis() {
        String jenis = (String) cmbJenis.getSelectedItem();
        if ("Motor".equals(jenis)) {
            lblKhusus.setText("Kapasitas Mesin (cc)");
            spnKhusus.setModel(new SpinnerNumberModel(125, 50, 1500, 5));
        } else if ("Bus".equals(jenis)) {
            lblKhusus.setText("Jumlah Kursi");
            spnKhusus.setModel(new SpinnerNumberModel(30, 16, 80, 1));
        } else {
            lblKhusus.setText("Kapasitas Penumpang");
            spnKhusus.setModel(new SpinnerNumberModel(5, 2, 15, 1));
        }
    }

    private void isiFormDariBaris() {
        String id = UiUtil.idTerpilih(tabel, model);
        if (id == null) return;
        controller.cari(id).ifPresent(k -> {
            idTerpilih = k.getId();
            lblId.setText(k.getId());
            cmbJenis.setSelectedItem(k.getJenis()); // memicu pengaturan spinner
            txtPlat.setText(k.getPlat());
            txtMerek.setText(k.getMerek());
            txtTarif.setText(String.valueOf((long) k.getTarifPerHari()));
            spnKhusus.setValue(k.getNilaiKhusus());
        });
    }

    private void aksiTambah() {
        jalankan(() -> {
            controller.tambah((String) cmbJenis.getSelectedItem(), txtPlat.getText(), txtMerek.getText(),
                    txtTarif.getText(), (Integer) spnKhusus.getValue());
            UiUtil.info(this, "Kendaraan berhasil ditambahkan.");
            bersihkanForm();
            refresh();
        });
    }

    private void aksiUbah() {
        jalankan(() -> {
            pastikanAdaPilihan();
            controller.ubah(idTerpilih, (String) cmbJenis.getSelectedItem(), txtPlat.getText(),
                    txtMerek.getText(), txtTarif.getText(), (Integer) spnKhusus.getValue());
            UiUtil.info(this, "Data kendaraan berhasil diubah.");
            bersihkanForm();
            refresh();
        });
    }

    private void aksiHapus() {
        jalankan(() -> {
            pastikanAdaPilihan();
            if (!UiUtil.konfirmasi(this, "Hapus kendaraan " + txtPlat.getText() + " ?")) return;
            controller.hapus(idTerpilih);
            bersihkanForm();
            refresh();
        });
    }

    private void pastikanAdaPilihan() throws DataTidakValidException {
        if (idTerpilih == null) throw new DataTidakValidException("Pilih kendaraan pada tabel terlebih dahulu.");
    }

    private void bersihkanForm() {
        idTerpilih = null;
        lblId.setText("(otomatis)");
        txtPlat.setText("");
        txtMerek.setText("");
        txtTarif.setText("");
        cmbJenis.setSelectedIndex(0);
        aturSpinnerSesuaiJenis();
        tabel.clearSelection();
    }

    @Override
    void refresh() {
        java.util.List<Object[]> baris = new java.util.ArrayList<>();
        for (Kendaraan k : controller.daftar(txtCari.getText(), (String) cmbFilter.getSelectedItem())) {
            baris.add(new Object[] { k.getId(), k.getJenis(), k.getPlat(), k.getMerek(),
                    Format.rupiah(k.getTarifPerHari()), k.getDetail(), k.isTersedia() ? "Tersedia" : "Disewa" });
        }
        UiUtil.isiTabel(tabel, model, baris);
    }
}
