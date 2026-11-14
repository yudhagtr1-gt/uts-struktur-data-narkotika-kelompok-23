package id.ac.umm.io;

import id.ac.umm.model.Putusan;
import id.ac.umm.service.AgregasiService;
import id.ac.umm.service.PutusanService;
import id.ac.umm.strukturdata.ArrayDinamis;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CsvExporterTest {

    private Putusan buat(String id, String jenis, double berat, int vonis, long denda, String bukti) {
        return new Putusan(id, "38/Pid.Sus/2026/PN Kdl", "Pengadilan Negeri Kendal",
                LocalDate.of(2026, 6, 17), "Terdakwa-001A", jenis, berat,
                "Pasal 114, ayat (1)", "Pasal 114", 48, vonis, denda, bukti,
                "memberatkan \"kutip\"", "meringankan", "identitas:1");
    }

    private ArrayDinamis<Putusan> contoh() {
        ArrayDinamis<Putusan> a = new ArrayDinamis<>();
        a.add(buat("P001", "Sabu", 3.16496, 24, 0L, "1 paket, 1 HP"));
        a.add(buat("P002", "Ganja", 0, 36, 1000000000L, "plastik"));
        a.add(buat("P003", "Sabu", 0.00012, 12, 0L, ""));
        return a;
    }

    @Test
    void ekspotLaluImporKembaliSama() throws IOException {
        ArrayDinamis<Putusan> asal = contoh();
        StringWriter sw = new StringWriter();
        assertEquals(3, new CsvExporter().eksporPutusan(asal, sw));

        HasilImpor h = new CsvImporter().impor(new StringReader(sw.toString()));
        assertEquals(3, h.jumlahValid());
        assertEquals(0, h.jumlahDitolak());
        for (int i = 0; i < 3; i++) {
            Putusan a = asal.get(i);
            Putusan b = h.putusan().get(i);
            assertEquals(a.caseId(), b.caseId());
            assertEquals(a.tanggalPutusan(), b.tanggalPutusan());
            assertEquals(a.jenisNarkotika(), b.jenisNarkotika());
            assertEquals(a.beratBarangBuktiGram(), b.beratBarangBuktiGram(), 1e-12);
            assertEquals(a.pasalDakwaan(), b.pasalDakwaan());
            assertEquals(a.vonisBulan(), b.vonisBulan());
            assertEquals(a.dendaRupiah(), b.dendaRupiah());
            assertEquals(a.barangBukti(), b.barangBukti());
            assertEquals(a.keadaanMemberatkan(), b.keadaanMemberatkan());
        }
        assertTrue(h.konsisten());
    }

    @Test
    void strukturSumberTidakBerubah() throws IOException {
        ArrayDinamis<Putusan> a = contoh();
        new CsvExporter().eksporPutusan(a, new StringWriter());
        assertEquals(3, a.size());
        assertEquals("P001", a.get(0).caseId());
        assertTrue(a.validateInvariant());
    }

    @Test
    void eksporKosongHanyaHeader() throws IOException {
        StringWriter sw = new StringWriter();
        assertEquals(0, new CsvExporter().eksporPutusan(new ArrayDinamis<>(), sw));
        assertEquals(1, sw.toString().split("\n").length);
        HasilImpor h = new CsvImporter().impor(new StringReader(sw.toString()));
        assertEquals(0, h.jumlahValid());
        assertEquals(0, h.jumlahBaris());
    }

    @Test
    void barisBaruDiDalamFieldDiganti() throws IOException {
        ArrayDinamis<Putusan> a = new ArrayDinamis<>();
        a.add(buat("P001", "Sabu", 1, 24, 0L, "baris satu\nbaris dua"));
        StringWriter sw = new StringWriter();
        new CsvExporter().eksporPutusan(a, sw);
        assertEquals(2, sw.toString().split("\n").length);
        HasilImpor h = new CsvImporter().impor(new StringReader(sw.toString()));
        assertEquals("baris satu baris dua", h.putusan().get(0).barangBukti());
    }

    @Test
    void eksporKeFileDiDisk(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("hasil.csv");
        assertEquals(3, new CsvExporter().eksporPutusan(contoh(), file));
        assertEquals(4, Files.readAllLines(file).size());
        HasilImpor h = new CsvImporter().imporFile(file);
        assertEquals(3, h.jumlahValid());
    }

    @Test
    void laporanAgregasiMemuatRingkasan() throws IOException {
        PutusanService service = new PutusanService(new ArrayDinamis<>());
        service.tambah(buat("P001", "Sabu", 1, 24, 0L, "a"));
        service.tambah(buat("P002", "Sabu", 2, 36, 1000L, "b"));
        AgregasiService.Ringkasan r = new AgregasiService(service).hitung();

        StringWriter sw = new StringWriter();
        new CsvExporter().eksporLaporan(r, sw);
        String teks = sw.toString();
        assertTrue(teks.contains("Jumlah perkara   : 2"));
        assertTrue(teks.contains("Total denda      : Rp1000"));
        assertTrue(teks.contains("30.00"));
        assertTrue(teks.contains("Sabu: 2"));
    }

    @Test
    void argumenNullDitolak() {
        CsvExporter e = new CsvExporter();
        assertThrows(IllegalArgumentException.class, () -> e.eksporPutusan(null, new StringWriter()));
        assertThrows(IllegalArgumentException.class, () -> e.eksporPutusan(contoh(), (java.io.Writer) null));
        assertThrows(IllegalArgumentException.class, () -> e.eksporLaporan(null, new StringWriter()));
    }
}
