package id.ac.umm;

import id.ac.umm.io.CsvImporter;
import id.ac.umm.io.HasilImpor;
import id.ac.umm.model.Putusan;
import id.ac.umm.service.AgregasiService;
import id.ac.umm.service.FilterService;
import id.ac.umm.service.PutusanService;
import id.ac.umm.service.PutusanService.HasilCari;
import id.ac.umm.service.UrutanService;
import id.ac.umm.strukturdata.ArrayDinamis;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {

    private static final String CSV_DEFAULT = "data/putusan-50.csv";
    private static final int TARGET = 50;

    private static final Scanner in = new Scanner(System.in);
    private static PutusanService service = new PutusanService(new ArrayDinamis<>());
    /** Hasil filter terakhir untuk agregasi; di-reset saat arsip berubah supaya tidak basi. */
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
                    default -> System.out.println("Pilihan tidak dikenal.");
                }
            } catch (IllegalArgumentException | DateTimeParseException | IndexOutOfBoundsException e) {
                System.out.println("Ditolak: " + e.getMessage());
                System.out.println("Struktur tetap konsisten: " + service.valid());
            } catch (IOException e) {
                System.out.println("Gagal membaca file: " + e.getMessage());
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
            System.out.println("Dihapus: " + p.caseId() + " (elemen sesudahnya bergeser ke kiri)");
        } else {
            HasilCari h = service.hapusKunci(masukan);
            if (h.ditemukan()) {
                System.out.println("Dihapus: " + h.putusan().caseId() + " dari posisi " + h.posisi()
                        + " setelah " + h.langkah() + " langkah");
            } else {
                System.out.println("Tidak ditemukan setelah " + h.langkah() + " langkah.");
            }
        }
        hasilFilterTerakhir = null;
        System.out.println("Sisa: " + service.jumlah() + ". Invariant OK: " + service.valid());
    }

    private static void cari() {
        HasilCari h = service.cari(tanya("Kunci"));
        if (h.ditemukan()) {
            System.out.println("Ditemukan di posisi " + h.posisi() + " setelah " + h.langkah() + " langkah:");
            System.out.println(service.ringkasan(h.posisi()));
        } else {
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
    }

    private static void urutkan() {
        String masukan = tanya("Arah (naik/turun) [naik]");
        UrutanService.Arah arah = "turun".equalsIgnoreCase(masukan)
                ? UrutanService.Arah.TURUN : UrutanService.Arah.NAIK;
        UrutanService.HasilUrut h = new UrutanService(service).urutkanVonis(arah);
        cetak(h.hasil());
        System.out.println("Perbandingan: " + h.perbandingan() + ", pergeseran: " + h.pergeseran());
        System.out.println("Arsip utama tidak berubah. Invariant OK: " + service.valid());
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

    private static String tanya(String label) {
        System.out.print(label + ": ");
        return in.hasNextLine() ? in.nextLine().trim() : null;
    }
}