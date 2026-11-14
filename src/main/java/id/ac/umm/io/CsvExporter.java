package id.ac.umm.io;

import id.ac.umm.model.Putusan;
import id.ac.umm.service.AgregasiService;
import id.ac.umm.strukturdata.StrukturData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Menyimpan hasil pencarian/filter/urutan ke CSV (skema 2.2, bisa diimpor lagi
 * oleh CsvImporter) dan laporan agregasi ke teks. Penyimpanan hanya membaca
 * struktur lewat get(i), tanpa mengubahnya. Baris baru di dalam field diganti spasi.
 */
public class CsvExporter {

    private static final String[] HEADER = {
        "caseId", "nomorPerkara", "pengadilan", "tanggalPutusan", "terdakwaKode",
        "jenisNarkotika", "beratBarangBuktiGram", "pasalDakwaan", "pasalTerbukti",
        "tuntutanBulan", "vonisBulan", "dendaRupiah", "barangBukti",
        "keadaanMemberatkan", "keadaanMeringankan", "sourcePages"
    };

    /** Menulis header dan satu baris per putusan; mengembalikan jumlah baris data. */
    public int eksporPutusan(StrukturData<Putusan> data, Writer out) throws IOException {
        if (data == null || out == null) {
            throw new IllegalArgumentException("data dan writer tidak boleh null");
        }
        out.write(String.join(",", HEADER));
        out.write('\n');
        for (int i = 0; i < data.size(); i++) {
            out.write(baris(data.get(i)));
            out.write('\n');
        }
        out.flush();
        return data.size();
    }

    public int eksporPutusan(StrukturData<Putusan> data, Path path) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            return eksporPutusan(data, w);
        }
    }

    /** Laporan agregasi sebagai teks, formatnya sama dengan menu Agregasi. */
    public void eksporLaporan(AgregasiService.Ringkasan r, Writer out) throws IOException {
        if (r == null || out == null) {
            throw new IllegalArgumentException("ringkasan dan writer tidak boleh null");
        }
        out.write("Jumlah perkara   : " + r.jumlahPerkara() + "\n");
        out.write("Rata-rata vonis  : " + String.format(Locale.ROOT, "%.2f", r.rataRataVonis()) + " bulan\n");
        out.write("Total denda      : Rp" + r.totalDenda() + "\n");
        out.write("Distribusi jenis narkotika:\n");
        for (int i = 0; i < r.distribusi().size(); i++) {
            AgregasiService.Distribusi d = r.distribusi().get(i);
            out.write("  - " + d.jenis() + ": " + d.jumlah() + "\n");
        }
        out.flush();
    }

    public void eksporLaporan(AgregasiService.Ringkasan r, Path path) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            eksporLaporan(r, w);
        }
    }

    private String baris(Putusan p) {
        return String.join(",",
                kolom(p.caseId()), kolom(p.nomorPerkara()), kolom(p.pengadilan()),
                p.tanggalPutusan().toString(), kolom(p.terdakwaKode()), kolom(p.jenisNarkotika()),
                BigDecimal.valueOf(p.beratBarangBuktiGram()).toPlainString(),
                kolom(p.pasalDakwaan()), kolom(p.pasalTerbukti()),
                String.valueOf(p.tuntutanBulan()), String.valueOf(p.vonisBulan()),
                String.valueOf(p.dendaRupiah()),
                kolom(p.barangBukti()), kolom(p.keadaanMemberatkan()),
                kolom(p.keadaanMeringankan()), kolom(p.sourcePages()));
    }

    /** Field yang berisi koma atau kutip dibungkus kutip ganda; kutip di dalamnya digandakan. */
    private String kolom(String s) {
        if (s == null) {
            return "";
        }
        String t = s.replace("\r", " ").replace("\n", " ");
        if (t.contains(",") || t.contains("\"")) {
            return "\"" + t.replace("\"", "\"\"") + "\"";
        }
        return t;
    }
}
