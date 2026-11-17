package rental.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;

import rental.controller.PenyewaanController;
import rental.exception.DataTidakValidException;
import rental.model.Kendaraan;
import rental.model.Pelanggan;
import rental.model.Penyewaan;
import rental.model.StatusSewa;
import rental.util.Format;

/** Halaman transaksi penyewaan: mulai sewa, ubah, selesaikan (kembalikan), dan hapus. */
class PenyewaanPanel extends PanelData {
    private static final long serialVersionUID = 1L;

    private final PenyewaanController controller;

    private final DefaultTableModel model = UiUtil.modelTabel("ID", "Kendaraan", "Pelanggan", "Mulai",
            "Kembali", "Hari", "Asuransi", "Total", "Status");
    private final JTable tabel = UiUtil.buatTabel(model);

    private final JTextField txtCari = new JTextField(18);
    private final JComboBox<String> cmbFilter = new JComboBox<>(new String[] { "Semua", "Aktif", "Selesai" });

    private final JLabel lblId = new JLabel("(otomatis)");
    private final JComboBox<Kendaraan> cmbKendaraan = new JComboBox<>();
    private final JComboBox<Pelanggan> cmbPelanggan = new JComboBox<>();
    private final JSpinner spnTanggal = new JSpinner(new SpinnerDateModel());
    private final JSpinner spnHari = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
    private final JCheckBox chkAsuransi = new JCheckBox("Tambah asuransi (+5%)");
    private final JLabel lblEstimasi = new JLabel("Rp 0");

    private String idTerpilih;

    PenyewaanPanel(PenyewaanController controller) {
        this.controller = controller;
        susunTampilan();
        pasangEvent();
    }

    // ---------------------------------------------------------------- tampilan
    private void susunTampilan() {
        JPanel atas = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        atas.add(new JLabel("Cari:"));
        atas.add(txtCari);
        atas.add(new JLabel("Status:"));
        atas.add(cmbFilter);
        add(atas, BorderLayout.NORTH);

        add(new JScrollPane(tabel), BorderLayout.CENTER);

        spnTanggal.setEditor(new JSpinner.DateEditor(spnTanggal, "dd-MM-yyyy"));
        lblEstimasi.setFont(lblEstimasi.getFont().deriveFont(Font.BOLD, 16f));

        JPanel form = UiUtil.panelForm();
        form.setBorder(BorderFactory.createTitledBorder("Transaksi Sewa"));
        UiUtil.tambahField(form, 0, "ID", lblId);
        UiUtil.tambahField(form, 1, "Kendaraan", cmbKendaraan);
        UiUtil.tambahField(form, 2, "Pelanggan", cmbPelanggan);
        UiUtil.tambahField(form, 3, "Tanggal Mulai", spnTanggal);
        UiUtil.tambahField(form, 4, "Lama Sewa (hari)", spnHari);
        UiUtil.tambahField(form, 5, "Opsi", chkAsuransi);
        UiUtil.tambahField(form, 6, "Estimasi Total", lblEstimasi);

        JPanel tombol = new JPanel(new GridLayout(3, 2, 6, 6));
        tombol.add(buatTombol("Mulai Sewa", this::aksiSewa));
        tombol.add(buatTombol("Ubah", this::aksiUbah));
        tombol.add(buatTombol("Selesai / Kembali", this::aksiSelesai));
        tombol.add(buatTombol("Hapus", this::aksiHapus));
        tombol.add(buatTombol("Bersihkan", this::bersihkanForm));

        add(kolomSamping(form, tombol, 350), BorderLayout.EAST);
    }

    private JButton buatTombol(String teks, Runnable aksi) {
        JButton b = new JButton(teks);
        b.addActionListener(e -> aksi.run());
        return b;
    }

    // ------------------------------------------------------------------- event
    private void pasangEvent() {
        txtCari.getDocument().addDocumentListener(UiUtil.saatBerubah(this::refreshTabel));
        cmbFilter.addActionListener(e -> refreshTabel());
        cmbKendaraan.addActionListener(e -> perbaruiEstimasi());
        spnHari.addChangeListener(e -> perbaruiEstimasi());
        chkAsuransi.addActionListener(e -> perbaruiEstimasi());
        tabel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) isiFormDariBaris();
        });
    }

    private void perbaruiEstimasi() {
        Kendaraan k = (Kendaraan) cmbKendaraan.getSelectedItem();
        int hari = (Integer) spnHari.getValue();
        lblEstimasi.setText(Format.rupiah(controller.estimasi(k, hari, chkAsuransi.isSelected())));
    }

    private LocalDate tanggalDariSpinner() {
        return ((Date) spnTanggal.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void setTanggalSpinner(LocalDate t) {
        spnTanggal.setValue(Date.from(t.atStartOfDay(ZoneId.systemDefault()).toInstant()));
    }

    /** Mengisi combo kendaraan dengan kendaraan yang tersedia (+ kendaraan transaksi yang sedang dipilih). */
    private void muatComboKendaraan(Kendaraan tambahan) {
        cmbKendaraan.removeAllItems();
        if (tambahan != null) cmbKendaraan.addItem(tambahan);
        for (Kendaraan k : controller.kendaraanTersedia()) {
            if (tambahan == null || !k.getId().equals(tambahan.getId())) cmbKendaraan.addItem(k);
        }
    }

    private void muatComboPelanggan() {
        Pelanggan dipilih = (Pelanggan) cmbPelanggan.getSelectedItem();
        cmbPelanggan.removeAllItems();
        for (Pelanggan p : controller.semuaPelanggan()) cmbPelanggan.addItem(p);
        if (dipilih != null) {
            for (int i = 0; i < cmbPelanggan.getItemCount(); i++) {
                if (cmbPelanggan.getItemAt(i).getId().equals(dipilih.getId())) {
                    cmbPelanggan.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void isiFormDariBaris() {
        String id = UiUtil.idTerpilih(tabel, model);
        if (id == null) return;
        controller.cari(id).ifPresent(p -> {
            idTerpilih = p.getId();
            lblId.setText(p.getId());
            muatComboKendaraan(p.getKendaraan());
            cmbKendaraan.setSelectedIndex(0);
            cmbKendaraan.setEnabled(false); // kendaraan tidak bisa diganti pada transaksi yang ada
            for (int i = 0; i < cmbPelanggan.getItemCount(); i++) {
                if (cmbPelanggan.getItemAt(i).getId().equals(p.getPelanggan().getId())) {
                    cmbPelanggan.setSelectedIndex(i);
                    break;
                }
            }
            setTanggalSpinner(p.getTanggalMulai());
            spnHari.setValue(p.getLamaHari());
            chkAsuransi.setSelected(p.isAsuransi());
            perbaruiEstimasi();
        });
    }

    private void aksiSewa() {
        jalankan(() -> {
            if (idTerpilih != null) {
                throw new DataTidakValidException("Klik 'Bersihkan' dahulu untuk membuat transaksi baru.");
            }
            Penyewaan p = controller.sewa((Kendaraan) cmbKendaraan.getSelectedItem(),
                    (Pelanggan) cmbPelanggan.getSelectedItem(), tanggalDariSpinner(),
                    (Integer) spnHari.getValue(), chkAsuransi.isSelected());
            UiUtil.info(this, "Sewa dimulai. Total biaya: " + Format.rupiah(p.hitungTotal()));
            bersihkanForm();
            refresh();
        });
    }

    private void aksiUbah() {
        jalankan(() -> {
            pastikanAdaPilihan();
            controller.ubah(idTerpilih, (Pelanggan) cmbPelanggan.getSelectedItem(), tanggalDariSpinner(),
                    (Integer) spnHari.getValue(), chkAsuransi.isSelected());
            UiUtil.info(this, "Transaksi berhasil diubah.");
            bersihkanForm();
            refresh();
        });
    }

    private void aksiSelesai() {
        jalankan(() -> {
            pastikanAdaPilihan();
            if (!UiUtil.konfirmasi(this, "Tandai transaksi " + idTerpilih + " sebagai selesai?")) return;
            controller.selesaikan(idTerpilih);
            bersihkanForm();
            refresh();
        });
    }

    private void aksiHapus() {
        jalankan(() -> {
            pastikanAdaPilihan();
            if (!UiUtil.konfirmasi(this, "Hapus transaksi " + idTerpilih + " ?")) return;
            controller.hapus(idTerpilih);
            bersihkanForm();
            refresh();
        });
    }

    private void pastikanAdaPilihan() throws DataTidakValidException {
        if (idTerpilih == null) throw new DataTidakValidException("Pilih transaksi pada tabel terlebih dahulu.");
    }

    private void bersihkanForm() {
        idTerpilih = null;
        lblId.setText("(otomatis)");
        tabel.clearSelection();
        muatComboKendaraan(null);
        cmbKendaraan.setEnabled(true);
        spnHari.setValue(1);
        chkAsuransi.setSelected(false);
        setTanggalSpinner(LocalDate.now());
        perbaruiEstimasi();
    }

    private StatusSewa statusFilter() {
        String f = (String) cmbFilter.getSelectedItem();
        if ("Aktif".equals(f)) return StatusSewa.AKTIF;
        if ("Selesai".equals(f)) return StatusSewa.SELESAI;
        return null;
    }

    private void refreshTabel() {
        java.util.List<Object[]> baris = new java.util.ArrayList<>();
        for (Penyewaan p : controller.daftar(txtCari.getText(), statusFilter())) {
            baris.add(new Object[] { p.getId(),
                    p.getKendaraan().getPlat() + " - " + p.getKendaraan().getMerek(),
                    p.getPelanggan().getNama(), Format.tanggal(p.getTanggalMulai()),
                    Format.tanggal(p.getTanggalKembali()), p.getLamaHari(),
                    p.isAsuransi() ? "Ya" : "Tidak", Format.rupiah(p.hitungTotal()), p.getStatus().getLabel() });
        }
        UiUtil.isiTabel(tabel, model, baris);
    }

    @Override
    void refresh() {
        muatComboPelanggan();
        if (idTerpilih == null) {
            muatComboKendaraan(null);
            setTanggalSpinner(LocalDate.now());
        }
        refreshTabel();
        perbaruiEstimasi();
    }
}
