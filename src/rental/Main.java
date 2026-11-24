package rental;

import java.nio.file.Paths;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import rental.view.LoginFrame;

/**
 * Titik masuk aplikasi GUI Rental Kendaraan.
 * Data tersimpan di folder "data" (dibuat otomatis di folder proyek).
 */
public class Main {
    public static void main(String[] args) {
        pasangTema();
        AppContext ctx = new AppContext(Paths.get("data"));
        SwingUtilities.invokeLater(() -> new LoginFrame(ctx).setVisible(true));
    }

    private static void pasangTema() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // tema bawaan Java dipakai bila gagal
        }
    }
}
