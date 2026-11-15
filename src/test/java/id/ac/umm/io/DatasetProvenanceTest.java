package id.ac.umm.io;

import id.ac.umm.model.Putusan;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bukti dataset (Issue #11): putusan-50.csv tepat 50 record valid, dan setiap
 * baris docs/data-provenance.csv cocok dengan nilainya di putusan-50.csv.
 */
class DatasetProvenanceTest {

    private static final Path DATASET = Paths.get("data/putusan-50.csv");
    private static final Path PROVENANCE = Paths.get("docs/data-provenance.csv");
    private static final String HEADER =
            "case_id,field_name,value,source_file,source_page,extractor,reviewer,status";

    @Test
    void datasetTepat50RecordValid() throws IOException {
        HasilImpor h = new CsvImporter().imporFile(DATASET);
        assertEquals(50, h.jumlahBaris());
        assertEquals(50, h.jumlahValid());
        assertEquals(0, h.jumlahDitolak());
        assertTrue(h.konsisten());
    }

    @Test
    void setiapBarisProvenanceCocokDenganCsv() throws IOException {
        HasilImpor h = new CsvImporter().imporFile(DATASET);
        List<String> baris = Files.readAllLines(PROVENANCE, StandardCharsets.UTF_8);
        assertEquals(HEADER, baris.get(0));
        assertTrue(baris.size() > 1, "provenance tidak boleh kosong");

        for (int i = 1; i < baris.size(); i++) {
            String[] k = baris.get(i).split(",", -1);
            assertEquals(8, k.length, "jumlah kolom di baris " + (i + 1));
            Putusan p = cari(h, k[0]);
            assertNotNull(p, "caseId tidak ada di dataset: " + k[0]);
            switch (k[1]) {
                case "vonisBulan" -> assertEquals(Integer.parseInt(k[2]), p.vonisBulan(), k[0]);
                case "tuntutanBulan" -> assertEquals(Integer.parseInt(k[2]), p.tuntutanBulan(), k[0]);
                case "dendaRupiah" -> assertEquals(Long.parseLong(k[2]), p.dendaRupiah(), k[0]);
                case "beratBarangBuktiGram" ->
                        assertEquals(Double.parseDouble(k[2]), p.beratBarangBuktiGram(), 1e-9, k[0]);
                default -> fail("field tidak dikenal di baris " + (i + 1) + ": " + k[1]);
            }
            assertTrue(k[3].endsWith(".pdf"), "source_file harus nama berkas PDF: " + k[3]);
            if (k[7].equals("verified")) {
                assertFalse(k[4].isBlank(), "baris verified harus punya source_page: " + k[0] + " " + k[1]);
                assertFalse(k[6].isBlank(), "baris verified harus punya reviewer: " + k[0] + " " + k[1]);
            }
        }
    }

    @Test
    void setiapPutusanPunyaProvenanceVonis() throws IOException {
        List<String> baris = Files.readAllLines(PROVENANCE, StandardCharsets.UTF_8);
        Set<String> adaVonis = new HashSet<>();
        for (int i = 1; i < baris.size(); i++) {
            String[] k = baris.get(i).split(",", -1);
            if (k[1].equals("vonisBulan")) {
                adaVonis.add(k[0]);
            }
        }
        assertEquals(50, adaVonis.size());
    }

    private Putusan cari(HasilImpor h, String caseId) {
        for (int i = 0; i < h.putusan().size(); i++) {
            if (h.putusan().get(i).caseId().equals(caseId)) {
                return h.putusan().get(i);
            }
        }
        return null;
    }
}
