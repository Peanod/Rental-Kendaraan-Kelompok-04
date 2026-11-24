package rental.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.filechooser.FileNameExtensionFilter;
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
            UiUtil.modelTabel("ID", "Jenis", "Plat", "Merek", "Tarif / Hari", "Detail", "Foto", "Status");
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

    // deskripsi kondisi + foto (pratinjau untuk calon penyewa)
    private final FotoPreview preview = new FotoPreview();
    private final JTextArea txtDeskripsi = new JTextArea(5, 30);
    private final JLabel lblHitung = new JLabel();

    private String idTerpilih;
    private File fotoBaru;        // foto yang baru dipilih pengguna (belum disimpan)
    private boolean hapusFoto;    // true bila pengguna menghapus foto kendaraan yang sudah ada

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

        JPanel tengah = new JPanel(new BorderLayout(0, 10));
        tengah.add(new JScrollPane(tabel), BorderLayout.CENTER);
        tengah.add(buatPanelDetail(), BorderLayout.SOUTH);
        add(tengah, BorderLayout.CENTER);

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

    /** Panel di bawah tabel: foto kendaraan (kiri) dan deskripsi kondisi (kanan). */
    private JPanel buatPanelDetail() {
        JPanel foto = new JPanel(new BorderLayout(0, 6));
        foto.add(preview, BorderLayout.CENTER);
        JPanel tombolFoto = new JPanel(new GridLayout(1, 2, 6, 0));
        tombolFoto.add(buatTombol("Pilih Foto...", this::aksiPilihFoto));
        tombolFoto.add(buatTombol("Hapus Foto", this::aksiHapusFoto));
        foto.add(tombolFoto, BorderLayout.SOUTH);

        txtDeskripsi.setLineWrap(true);
        txtDeskripsi.setWrapStyleWord(true);
        lblHitung.setFont(lblHitung.getFont().deriveFont(Font.PLAIN, 11f));
        perbaruiHitung();
        JPanel deskripsi = new JPanel(new BorderLayout(0, 4));
        deskripsi.add(new JLabel("Deskripsi kondisi (tahun, kelengkapan, kondisi mesin/bodi, catatan kerusakan):"),
                BorderLayout.NORTH);
        deskripsi.add(new JScrollPane(txtDeskripsi), BorderLayout.CENTER);
        deskripsi.add(lblHitung, BorderLayout.SOUTH);

        JPanel detail = new JPanel(new BorderLayout(12, 0));
        detail.setBorder(BorderFactory.createTitledBorder("Foto dan Deskripsi Kondisi"));
        detail.add(foto, BorderLayout.WEST);
        detail.add(deskripsi, BorderLayout.CENTER);
        return detail;
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
        txtDeskripsi.getDocument().addDocumentListener(UiUtil.saatBerubah(this::perbaruiHitung));
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
            txtDeskripsi.setText(k.getDeskripsi());
            txtDeskripsi.setCaretPosition(0);
            fotoBaru = null;
            hapusFoto = false;
            preview.tampilkan(controller.berkasFoto(k));
        });
    }

    private void aksiTambah() {
        jalankan(() -> {
            controller.tambah((String) cmbJenis.getSelectedItem(), txtPlat.getText(), txtMerek.getText(),
                    txtTarif.getText(), (Integer) spnKhusus.getValue(), txtDeskripsi.getText(), fotoBaru);
            UiUtil.info(this, "Kendaraan berhasil ditambahkan.");
            bersihkanForm();
            refresh();
        });
    }

    private void aksiUbah() {
        jalankan(() -> {
            pastikanAdaPilihan();
            controller.ubah(idTerpilih, (String) cmbJenis.getSelectedItem(), txtPlat.getText(),
                    txtMerek.getText(), txtTarif.getText(), (Integer) spnKhusus.getValue(),
                    txtDeskripsi.getText(), fotoBaru, hapusFoto);
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

    private void aksiPilihFoto() {
        JFileChooser pemilih = new JFileChooser();
        pemilih.setDialogTitle("Pilih foto kendaraan");
        pemilih.setFileFilter(new FileNameExtensionFilter("Gambar (JPG, PNG, GIF)",
                KendaraanController.EKSTENSI_FOTO));
        if (pemilih.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File dipilih = pemilih.getSelectedFile();
        jalankan(() -> {
            controller.cekFoto(dipilih); // menolak berkas bukan gambar / terlalu besar
            fotoBaru = dipilih;
            hapusFoto = false;
            preview.tampilkan(dipilih);
        });
    }

    private void aksiHapusFoto() {
        fotoBaru = null;
        hapusFoto = idTerpilih != null; // pada data baru cukup mengosongkan pilihan
        preview.kosong("Belum ada foto");
    }

    private void perbaruiHitung() {
        lblHitung.setText(txtDeskripsi.getText().length() + " / " + Kendaraan.MAKS_DESKRIPSI + " karakter");
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
        txtDeskripsi.setText("");
        fotoBaru = null;
        hapusFoto = false;
        preview.kosong("Belum ada foto");
        tabel.clearSelection();
    }

    @Override
    void refresh() {
        java.util.List<Object[]> baris = new java.util.ArrayList<>();
        for (Kendaraan k : controller.daftar(txtCari.getText(), (String) cmbFilter.getSelectedItem())) {
            baris.add(new Object[] { k.getId(), k.getJenis(), k.getPlat(), k.getMerek(),
                    Format.rupiah(k.getTarifPerHari()), k.getDetail(),
                    controller.berkasFoto(k) != null ? "Ada" : "-", k.isTersedia() ? "Tersedia" : "Disewa" });
        }
        UiUtil.isiTabel(tabel, model, baris);
    }
}
