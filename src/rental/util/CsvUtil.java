package rental.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/** Utilitas sederhana untuk menyimpan/membaca satu baris data (field dienkode agar aman dari koma/baris baru). */
public final class CsvUtil {
    private static final String ENC = "UTF-8";

    private CsvUtil() { }

    public static String gabung(Object... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(encode(String.valueOf(fields[i])));
        }
        return sb.toString();
    }

    public static String[] pisah(String baris) {
        String[] bagian = baris.split(",", -1);
        for (int i = 0; i < bagian.length; i++) bagian[i] = decode(bagian[i]);
        return bagian;
    }

    private static String encode(String s) {
        try {
            return URLEncoder.encode(s, ENC);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String decode(String s) {
        try {
            return URLDecoder.decode(s, ENC);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
