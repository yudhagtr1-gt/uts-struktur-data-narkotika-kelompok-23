package id.ac.umm.io;

import id.ac.umm.model.Putusan;
import id.ac.umm.strukturdata.ArrayDinamis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Membaca CSV putusan (skema 2.2). Baris tidak valid ditolak dan dicatat
 * sebagai galat tanpa menghentikan impor. Field tidak boleh berisi baris baru.
 */
public class CsvImporter {

    public static final int JUMLAH_KOLOM = 16;

    private static final String[] HEADER = {
        "caseId", "nomorPerkara", "pengadilan", "tanggalPutusan", "terdakwaKode",
        "jenisNarkotika", "beratBarangBuktiGram", "pasalDakwaan", "pasalTerbukti",
        "tuntutanBulan", "vonisBulan", "dendaRupiah", "barangBukti",
        "keadaanMemberatkan", "keadaanMeringankan", "sourcePages"
    };

    public HasilImpor imporFile(Path path) throws IOException {
        try (BufferedReader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return impor(r);
        }
    }

    public HasilImpor impor(Reader reader) throws IOException {
        BufferedReader r = reader instanceof BufferedReader b ? b : new BufferedReader(reader);
        HasilImpor hasil = new HasilImpor();
        boolean headerSudah = false;
        int no = 0;
        String baris;
        while ((baris = r.readLine()) != null) {
            no++;
            if (no == 1 && baris.startsWith("\uFEFF")) {
                baris = baris.substring(1);
            }
            if (baris.isBlank()) {
                continue;
            }
            if (!headerSudah) {
                cekHeader(baris);
                headerSudah = true;
                continue;
            }
            hasil.tambahBaris();
            proses(baris, no, hasil);
        }
        if (!headerSudah) {
            throw new IllegalArgumentException("File CSV kosong: header tidak ditemukan");
        }
        return hasil;
    }

    private void cekHeader(String baris) {
        ArrayDinamis<String> f = pecah(baris);
        boolean cocok = f.size() == HEADER.length;
        for (int i = 0; cocok && i < HEADER.length; i++) {
            cocok = f.get(i).trim().equals(HEADER[i]);
        }
        if (!cocok) {
            throw new IllegalArgumentException("Header CSV tidak sesuai skema 2.2: " + baris);
        }
    }

    private void proses(String baris, int no, HasilImpor hasil) {
        ArrayDinamis<String> f = pecah(baris);
        String id = f.get(0).trim();
        if (f.size() != JUMLAH_KOLOM) {
            hasil.tolak("Baris " + no + " (" + id + "): terbaca " + f.size()
                    + " kolom, seharusnya " + JUMLAH_KOLOM);
            return;
        }
        try {
            Putusan p = bentuk(f);
            if (hasil.putusan().contains(p)) {
                hasil.tolak("Baris " + no + " (" + id + "): caseId dobel");
                return;
            }
            hasil.putusan().add(p);
        } catch (IllegalArgumentException e) {
            hasil.tolak("Baris " + no + " (" + id + "): " + e.getMessage());
        }
    }

    private Putusan bentuk(ArrayDinamis<String> f) {
        return new Putusan(
                teks(f, 0), teks(f, 1), teks(f, 2), tanggal(teks(f, 3)),
                teks(f, 4), teks(f, 5), desimal(teks(f, 6), "beratBarangBuktiGram"),
                teks(f, 7), teks(f, 8),
                bulat(teks(f, 9), "tuntutanBulan"), bulat(teks(f, 10), "vonisBulan"),
                panjang(teks(f, 11), "dendaRupiah"),
                teks(f, 12), teks(f, 13), teks(f, 14), teks(f, 15));
    }

    private String teks(ArrayDinamis<String> f, int i) {
        return f.get(i).trim();
    }

    private LocalDate tanggal(String s) {
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("tanggalPutusan tidak valid (harus yyyy-MM-dd): " + s);
        }
    }

    private double desimal(String s, String nama) {
        if (s.isEmpty()) {
            return 0;
        }
        try {
            double v = Double.parseDouble(s);
            if (!Double.isFinite(v)) {
                throw new IllegalArgumentException(nama + " tidak valid: " + s);
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nama + " bukan angka: " + s);
        }
    }

    private int bulat(String s, String nama) {
        if (s.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nama + " bukan bilangan bulat: " + s);
        }
    }

    private long panjang(String s, String nama) {
        if (s.isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(nama + " bukan bilangan bulat: " + s);
        }
    }

    /** Memecah satu baris CSV menjadi field; mendukung kutip ganda dan "" sebagai kutip. */
    static ArrayDinamis<String> pecah(String baris) {
        ArrayDinamis<String> f = new ArrayDinamis<>();
        StringBuilder sb = new StringBuilder();
        boolean dalamKutip = false;
        for (int i = 0; i < baris.length(); i++) {
            char c = baris.charAt(i);
            if (dalamKutip) {
                if (c == '"') {
                    if (i + 1 < baris.length() && baris.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        dalamKutip = false;
                    }
                } else {
                    sb.append(c);
                }
            } else if (c == '"') {
                dalamKutip = true;
            } else if (c == ',') {
                f.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        f.add(sb.toString());
        return f;
    }
}
