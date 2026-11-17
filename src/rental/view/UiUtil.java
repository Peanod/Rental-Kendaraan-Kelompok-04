package rental.view;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/** Fungsi bantu tampilan agar kode panel tidak berulang. */
final class UiUtil {
    private UiUtil() { }

    static void galat(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Input tidak valid", JOptionPane.ERROR_MESSAGE);
    }

    static void info(Component induk, String pesan) {
        JOptionPane.showMessageDialog(induk, pesan, "Informasi", JOptionPane.INFORMATION_MESSAGE);
    }

    static boolean konfirmasi(Component induk, String pesan) {
        int pilihan = JOptionPane.showConfirmDialog(induk, pesan, "Konfirmasi",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return pilihan == JOptionPane.YES_OPTION;
    }

    /** Tabel yang selnya tidak bisa diedit langsung. */
    static DefaultTableModel modelTabel(String... kolom) {
        return new DefaultTableModel(kolom, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    static JTable buatTabel(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(24);
        t.setRowSorter(new TableRowSorter<DefaultTableModel>(model));
        t.setFillsViewportHeight(true);
        t.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        return t;
    }

    /**
     * Mengisi ulang seluruh baris tabel. Sorter dilepas sementara agar tidak muncul peringatan Swing
     * akibat sisa indeks seleksi; urutan kolom yang dipilih pengguna tetap dipertahankan.
     */
    static void isiTabel(JTable tabel, DefaultTableModel model, java.util.List<Object[]> baris) {
        javax.swing.RowSorter<? extends javax.swing.table.TableModel> sorter = tabel.getRowSorter();
        tabel.clearSelection();
        tabel.setRowSorter(null);
        model.setRowCount(0);
        for (Object[] b : baris) model.addRow(b);
        tabel.setRowSorter(sorter);
        if (sorter instanceof javax.swing.DefaultRowSorter) {
            ((javax.swing.DefaultRowSorter<?, ?>) sorter).allRowsChanged();
        }
    }

    /** Mengembalikan isi kolom pertama (ID) pada baris terpilih, atau null bila tidak ada pilihan. */
    static String idTerpilih(JTable tabel, DefaultTableModel model) {
        int baris = tabel.getSelectedRow();
        if (baris < 0) return null;
        return (String) model.getValueAt(tabel.convertRowIndexToModel(baris), 0);
    }

    /** Menambah satu baris "label : komponen" pada form ber-GridBagLayout. */
    static void tambahField(JPanel form, int baris, String label, JComponent komponen) {
        tambahField(form, baris, new JLabel(label), komponen);
    }

    /** Versi dengan komponen label sendiri (mis. label yang teksnya berubah dinamis). */
    static void tambahField(JPanel form, int baris, JComponent label, JComponent komponen) {
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = baris;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.insets = new Insets(5, 6, 5, 8);
        form.add(label, g);

        g = new GridBagConstraints();
        g.gridx = 1;
        g.gridy = baris;
        g.weightx = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 0, 5, 6);
        form.add(komponen, g);
    }

    static JPanel panelForm() {
        return new JPanel(new GridBagLayout());
    }

    static DocumentListener saatBerubah(Runnable aksi) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { aksi.run(); }
            @Override public void removeUpdate(DocumentEvent e) { aksi.run(); }
            @Override public void changedUpdate(DocumentEvent e) { aksi.run(); }
        };
    }
}
