package id.ac.umm;

import id.ac.umm.io.CsvExporter;
import id.ac.umm.io.CsvImporter;
import id.ac.umm.io.HasilImpor;
import id.ac.umm.model.Putusan;
import id.ac.umm.service.AgregasiService;
import id.ac.umm.service.AuditService;
import id.ac.umm.service.FilterService;
import id.ac.umm.service.PutusanService;
import id.ac.umm.service.PutusanService.HasilCari;
import id.ac.umm.service.UrutanService;
import id.ac.umm.strukturdata.ArrayDinamis;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {

    private static final String CSV_DEFAULT = "data/putusan-50.csv";
    private static final int TARGET = 50;

    private static final Scanner in = new Scanner(System.in);
    private static PutusanService service = new PutusanService(new ArrayDinamis<>());
    /** Riwayat operasi pengguna; disimpan di Stack (struktur sekunder). */
    private static final AuditService audit = new AuditService();
    /** Hasil filter terakhir untuk agregasi dan ekspor; di-reset saat arsip berubah supaya tidak basi. */
    private static ArrayDinamis<Putusan> hasilFilterTerakhir = null;

    public static void main(String[] args) {
        String pathAwal = args.length > 0 ? args[0] : CSV_DEFAULT;
        while (true) {
            System.out.println();
            System.out.println("=== Arsip Putusan Narkotika (" + service.jumlah() + " putusan) ===");
            System.out.println("1. Impor CSV");
            System.out.println("2. Tampil semua");
            System.out.println("3. Tambah putusan");
            System.out.println("4. Hapus putusan");
            System.out.println("5. Cari (caseId / nomor perkara)");
            System.out.println("6. Filter");
            System.out.println("7. Agregasi");
            System.out.println("8. Urutkan berdasarkan vonis");
            System.out.println("9. Ekspor (CSV / laporan)");
            System.out.println("10. Riwayat audit");
            System.out.println("0. Keluar");
            String pilihan = tanya("Pilih");
            if (pilihan == null || pilihan.equals("0")) {
                System.out.println("Selesai.");
                return;
            }
            try {
                switch (pilihan) {
                    case "1" -> impor(pathAwal);
                    case "2" -> tampil();
                    case "3" -> tambah();
                    case "4" -> hapus();
                    case "5" -> cari();
                    case "6" -> filter();
                    case "7" -> agregasi();
                    case "8" -> urutkan();
                    case "9" -> ekspor();
                    case "10" -> riwayat();
                    default -> System.out.println("Pilihan tidak dikenal.");
                }
            } catch (IllegalArgumentException | DateTimeParseException | IndexOutOfBoundsException e) {
                System.out.println("Ditolak: " + e.getMessage());
                System.out.println("Struktur tetap konsisten: " + service.valid());
                audit.catat("DITOLAK", "menu " + pilihan + ": " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Gagal membaca/menulis file: " + e.getMessage());
                audit.catat("GAGAL-FILE", "menu " + pilihan + ": " + e.getMessage());
            }
        }
    }

    private static void impor(String pathAwal) throws IOException {
        String path = tanya("Path CSV [" + pathAwal + "]");
        if (path == null || path.isBlank()) {
            path = pathAwal;
        }
        HasilImpor hasil;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)) {
            hasil = new CsvImporter().impor(reader);
        }
        service = new PutusanService(hasil.putusan());
        hasilFilterTerakhir = null;
        System.out.println("Baris dibaca : " + hasil.jumlahBaris());
        System.out.println("Valid        : " + hasil.jumlahValid());
        System.out.println("Ditolak      : " + hasil.jumlahDitolak());
        for (int i = 0; i < hasil.galat().size(); i++) {
            System.out.println("  - " + hasil.galat().get(i));
        }
        if (!hasil.tepat(TARGET)) {
            System.out.println("PERINGATAN: jumlah valid " + hasil.jumlahValid() + ", target " + TARGET + ".");
        }
        System.out.println("Invariant OK : " + hasil.konsisten());
        audit.catat("IMPOR", path + " (valid " + hasil.jumlahValid() + ", ditolak " + hasil.jumlahDitolak() + ")");
    }

    private static void tampil() {
        if (service.jumlah() == 0) {
            System.out.println("Arsip kosong.");
            return;
        }
        System.out.print(service.tampil());
        System.out.println("Total: " + service.jumlah());
    }

    private static void tambah() {
        String caseId = tanya("caseId");
        String nomor = tanya("nomorPerkara");
        String pengadilan = tanya("pengadilan");
        LocalDate tanggal = LocalDate.parse(tanya("tanggalPutusan (yyyy-MM-dd)"));
        String jenis = tanya("jenisNarkotika");
        double berat = Double.parseDouble(tanya("beratBarangBuktiGram"));
        int vonis = Integer.parseInt(tanya("vonisBulan"));
        // Kolom lain dikosongkan (0 atau kosong), sesuai aturan: tidak ditemukan = kosong.
        Putusan p = new Putusan(caseId, nomor, pengadilan, tanggal, "", jenis,
                berat, "", "", 0, vonis, 0L, "", "", "", "");
        service.tambah(p);
        hasilFilterTerakhir = null;
        audit.catat("TAMBAH", caseId);
        System.out.println("Ditambahkan di posisi " + (service.jumlah() - 1)
                + ". Invariant OK: " + service.valid());
    }

    private static void hapus() {
        String masukan = tanya("Posisi (angka) atau caseId/nomor perkara");
        if (masukan == null || masukan.isBlank()) {
            System.out.println("Dibatalkan.");
            return;
        }
        if (masukan.matches("\\d+")) {
            Putusan p = service.hapusPosisi(Integer.parseInt(masukan));
            audit.catat("HAPUS", p.caseId() + " (posisi " + masukan + ")");
            System.out.println("Dihapus: " + p.caseId() + " (elemen sesudahnya bergeser ke kiri)");
        } else {
            HasilCari h = service.hapusKunci(masukan);
            if (h.ditemukan()) {
                audit.catat("HAPUS", h.putusan().caseId() + " (posisi " + h.posisi() + ")");
                System.out.println("Dihapus: " + h.putusan().caseId() + " dari posisi " + h.posisi()
                        + " setelah " + h.langkah() + " langkah");
            } else {
                audit.catat("HAPUS-GAGAL", masukan + " tidak ditemukan");
                System.out.println("Tidak ditemukan setelah " + h.langkah() + " langkah.");
            }
        }
        hasilFilterTerakhir = null;
        System.out.println("Sisa: " + service.jumlah() + ". Invariant OK: " + service.valid());
    }

    private static void cari() {
        String kunci = tanya("Kunci");
        HasilCari h = service.cari(kunci);
        if (h.ditemukan()) {
            audit.catat("CARI", kunci + " ditemukan di posisi " + h.posisi() + " (" + h.langkah() + " langkah)");
            System.out.println("Ditemukan di posisi " + h.posisi() + " setelah " + h.langkah() + " langkah:");
            System.out.println(service.ringkasan(h.posisi()));
        } else {
            audit.catat("CARI", kunci + " tidak ditemukan (" + h.langkah() + " langkah)");
            System.out.println("Tidak ditemukan setelah " + h.langkah() + " langkah.");
        }
    }

    private static void filter() {
        System.out.println("Kosongkan isian untuk melewati kriteria.");
        String jenis = tanya("jenisNarkotika (mengandung teks)");
        Double beratMin = ubahDouble(tanya("beratMin (gram)"));
        Double beratMax = ubahDouble(tanya("beratMax (gram)"));
        String pasal = tanya("pasalTerbukti (mengandung teks)");
        Integer vonisMin = ubahInt(tanya("vonisMin (bulan)"));
        Integer vonisMax = ubahInt(tanya("vonisMax (bulan)"));

        FilterService.Kriteria kriteria =
                new FilterService.Kriteria(jenis, beratMin, beratMax, pasal, vonisMin, vonisMax);
        FilterService.HasilFilter h = new FilterService(service).filter(kriteria);
        hasilFilterTerakhir = h.hasil();

        cetak(h.hasil());
        audit.catat("FILTER", h.hasil().size() + " cocok dari " + h.diperiksa());
        System.out.println("Cocok: " + h.hasil().size() + " dari " + h.diperiksa() + " putusan diperiksa.");
    }

    private static void agregasi() {
        String pilih = tanya("Data: 1 = seluruh arsip, 2 = hasil filter terakhir [1]");
        AgregasiService agregasi = new AgregasiService(service);
        AgregasiService.Ringkasan r;
        if ("2".equals(pilih)) {
            if (hasilFilterTerakhir == null) {
                System.out.println("Belum ada hasil filter yang berlaku. Jalankan menu 6 dulu.");
                return;
            }
            r = agregasi.hitung(hasilFilterTerakhir);
            System.out.println("Sumber: hasil filter terakhir");
        } else {
            r = agregasi.hitung();
            System.out.println("Sumber: seluruh arsip");
        }
        System.out.println("Jumlah perkara   : " + r.jumlahPerkara());
        System.out.println("Rata-rata vonis  : " + String.format("%.2f", r.rataRataVonis()) + " bulan");
        System.out.println("Total denda      : Rp" + r.totalDenda());
        System.out.println("Distribusi jenis narkotika:");
        for (int i = 0; i < r.distribusi().size(); i++) {
            AgregasiService.Distribusi d = r.distribusi().get(i);
            System.out.println("  - " + d.jenis() + ": " + d.jumlah());
        }
        audit.catat("AGREGASI", "2".equals(pilih) ? "hasil filter" : "seluruh arsip");
    }

    private static void urutkan() {
        String masukan = tanya("Arah (naik/turun) [naik]");
        UrutanService.Arah arah = "turun".equalsIgnoreCase(masukan)
                ? UrutanService.Arah.TURUN : UrutanService.Arah.NAIK;
        UrutanService.HasilUrut h = new UrutanService(service).urutkanVonis(arah);
        cetak(h.hasil());
        audit.catat("URUT", "vonis " + arah);
        System.out.println("Perbandingan: " + h.perbandingan() + ", pergeseran: " + h.pergeseran());
        System.out.println("Arsip utama tidak berubah. Invariant OK: " + service.valid());
    }

    private static void ekspor() throws IOException {
        String pilih = tanya("Ekspor: 1 = seluruh arsip (CSV), 2 = hasil filter terakhir (CSV), "
                + "3 = laporan agregasi arsip (teks) [1]");
        CsvExporter exporter = new CsvExporter();
        if ("3".equals(pilih)) {
            Path path = Paths.get(tanyaDefault("Path file", "docs/demo/laporan-agregasi.txt"));
            buatFolder(path);
            exporter.eksporLaporan(new AgregasiService(service).hitung(), path);
            audit.catat("EKSPOR", "laporan agregasi -> " + path);
            System.out.println("Laporan disimpan ke " + path);
            return;
        }
        ArrayDinamis<Putusan> data;
        String sumber;
        String defaultPath;
        if ("2".equals(pilih)) {
            if (hasilFilterTerakhir == null) {
                System.out.println("Belum ada hasil filter yang berlaku. Jalankan menu 6 dulu.");
                return;
            }
            data = hasilFilterTerakhir;
            sumber = "hasil filter terakhir";
            defaultPath = "docs/demo/ekspor-filter.csv";
        } else {
            data = salinArsip();
            sumber = "seluruh arsip";
            defaultPath = "docs/demo/ekspor-arsip.csv";
        }
        Path path = Paths.get(tanyaDefault("Path file", defaultPath));
        buatFolder(path);
        int jumlah = exporter.eksporPutusan(data, path);
        audit.catat("EKSPOR", sumber + " (" + jumlah + " baris) -> " + path);
        System.out.println(jumlah + " putusan (" + sumber + ") disimpan ke " + path);
    }

    private static void riwayat() {
        System.out.print(audit.tampilkan());
        System.out.println("Total catatan: " + audit.jumlah() + " (terbaru di atas). Invariant OK: " + audit.valid());
    }

    /** Salinan urutan arsip lewat ambil(i); arsip utama tidak diubah. */
    private static ArrayDinamis<Putusan> salinArsip() {
        ArrayDinamis<Putusan> salinan = new ArrayDinamis<>(Math.max(service.jumlah(), 1));
        for (int i = 0; i < service.jumlah(); i++) {
            salinan.add(service.ambil(i));
        }
        return salinan;
    }

    private static void buatFolder(Path path) throws IOException {
        Path induk = path.getParent();
        if (induk != null) {
            Files.createDirectories(induk);
        }
    }

    private static void cetak(ArrayDinamis<Putusan> data) {
        if (data.isEmpty()) {
            System.out.println("(tidak ada putusan)");
            return;
        }
        for (int i = 0; i < data.size(); i++) {
            System.out.println("[" + i + "] " + data.get(i).ringkasan());
        }
    }

    private static Double ubahDouble(String s) {
        return (s == null || s.isBlank()) ? null : Double.valueOf(s.trim());
    }

    private static Integer ubahInt(String s) {
        return (s == null || s.isBlank()) ? null : Integer.valueOf(s.trim());
    }

    private static String tanyaDefault(String label, String nilaiAwal) {
        String s = tanya(label + " [" + nilaiAwal + "]");
        return (s == null || s.isBlank()) ? nilaiAwal : s;
    }

    private static String tanya(String label) {
        System.out.print(label + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }
}