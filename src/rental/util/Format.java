package rental.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Pemformat tampilan (rupiah dan tanggal). */
public final class Format {
    public static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final NumberFormat RUPIAH = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("id-ID"));

    static {
        RUPIAH.setMaximumFractionDigits(0);
    }

    private Format() { }

    public static String rupiah(double nilai) {
        return RUPIAH.format(nilai);
    }

    public static String tanggal(LocalDate t) {
        return t == null ? "-" : t.format(TANGGAL);
    }
}
