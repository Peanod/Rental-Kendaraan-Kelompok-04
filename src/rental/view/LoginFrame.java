package rental.view;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import rental.AppContext;
import rental.exception.DataTidakValidException;
import rental.exception.PenyimpananException;

/** Tampilan 1: halaman login. */
public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final JTextField txtUsername = new JTextField(16);
    private final JPasswordField txtSandi = new JPasswordField(16);

    public LoginFrame(AppContext ctx) {
        super("Rental Kendaraan - Login");
        this.ctx = ctx;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JLabel judul = new JLabel("RENTAL KENDARAAN", SwingConstants.CENTER);
        judul.setFont(judul.getFont().deriveFont(Font.BOLD, 22f));
        JLabel sub = new JLabel("Silakan masuk untuk melanjutkan", SwingConstants.CENTER);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.add(judul, BorderLayout.CENTER);
        header.add(sub, BorderLayout.SOUTH);

        JPanel form = UiUtil.panelForm();
        UiUtil.tambahField(form, 0, "Username", txtUsername);
        UiUtil.tambahField(form, 1, "Kata sandi", txtSandi);

        JButton btnMasuk = new JButton("Masuk");
        btnMasuk.addActionListener(e -> prosesLogin());
        getRootPane().setDefaultButton(btnMasuk); // tombol Enter

        JLabel petunjuk = new JLabel("Akun demo: admin / admin123", SwingConstants.CENTER);
        petunjuk.setFont(petunjuk.getFont().deriveFont(Font.ITALIC, 11f));

        JPanel bawah = new JPanel(new BorderLayout(0, 8));
        bawah.add(btnMasuk, BorderLayout.NORTH);
        bawah.add(petunjuk, BorderLayout.SOUTH);

        JPanel isi = new JPanel(new BorderLayout(0, 16));
        isi.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));
        isi.add(header, BorderLayout.NORTH);
        isi.add(form, BorderLayout.CENTER);
        isi.add(bawah, BorderLayout.SOUTH);
        setContentPane(isi);

        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(null);
    }

    private void prosesLogin() {
        try {
            ctx.auth.masuk(txtUsername.getText(), new String(txtSandi.getPassword()));
            dispose();
            new MainFrame(ctx).setVisible(true);
        } catch (DataTidakValidException e) {
            UiUtil.galat(this, e.getMessage());
            txtSandi.setText("");
        } catch (PenyimpananException e) {
            UiUtil.galat(this, "Gagal membaca data pengguna: " + e.getMessage());
        }
    }
}
