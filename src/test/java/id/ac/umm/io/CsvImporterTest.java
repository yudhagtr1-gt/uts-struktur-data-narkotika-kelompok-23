package id.ac.umm.io;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class CsvImporterTest {

    private static final String HEADER =
            "caseId,nomorPerkara,pengadilan,tanggalPutusan,terdakwaKode,jenisNarkotika,"
            + "beratBarangBuktiGram,pasalDakwaan,pasalTerbukti,tuntutanBulan,vonisBulan,"
            + "dendaRupiah,barangBukti,keadaanMemberatkan,keadaanMeringankan,sourcePages";

    private String baris(String id, String tgl, String berat, String vonis) {
        return id + ",38/Pid.Sus/2026/PN Kdl,Pengadilan Negeri Kendal," + tgl
                + ",Terdakwa-001A,Sabu," + berat + ",Pasal 114,Pasal 114,48," + vonis
                + ",0,\"paket, plastik\",memberatkan,meringankan,identitas:1";
    }

    private HasilImpor baca(String... isi) throws IOException {
        return new CsvImporter().impor(new StringReader(String.join("\n", isi)));
    }

    @Test
    void membacaDataValid() throws IOException {
        HasilImpor h = baca(HEADER,
                baris("P001", "2026-06-17", "3.16496", "24"),
                baris("P002", "2026-06-18", "1.5", "36"),
                baris("P003", "2026-06-19", "0.5", "12"));
        assertEquals(3, h.jumlahValid());
        assertEquals(0, h.jumlahDitolak());
        assertTrue(h.tepat(3));
        assertFalse(h.tepat(50));
        assertEquals("P001", h.putusan().get(0).caseId());
        assertEquals(3.16496, h.putusan().get(0).beratBarangBuktiGram(), 1e-9);
        assertTrue(h.konsisten());
    }

    @Test
    void komaDiDalamKutipTidakMemecahKolom() throws IOException {
        HasilImpor h = baca(HEADER, baris("P001", "2026-06-17", "1", "24"));
        assertEquals("paket, plastik", h.putusan().get(0).barangBukti());
    }

    @Test
    void nilaiKosongMenjadiNol() throws IOException {
        HasilImpor h = baca(HEADER, baris("P001", "2026-06-17", "", ""));
        assertEquals(1, h.jumlahValid());
        assertEquals(0.0, h.putusan().get(0).beratBarangBuktiGram(), 1e-9);
        assertEquals(0, h.putusan().get(0).vonisBulan());
    }

    @Test
    void barisTidakValidDitolakTanpaMenghentikanImpor() throws IOException {
        HasilImpor h = baca(HEADER,
                baris("P001", "2026-06-17", "1", "24"),
                baris("P002", "17-06-2026", "1", "24"),
                baris("P003", "2026-06-17", "1", "-5"),
                baris("P004", "2026-06-17", "abc", "24"),
                baris("P005", "2026-06-17", "1", "24"));
        assertEquals(2, h.jumlahValid());
        assertEquals(3, h.jumlahDitolak());
        assertTrue(h.galat().get(0).contains("Baris 3"));
        assertTrue(h.galat().get(0).contains("tanggalPutusan"));
        assertTrue(h.konsisten());
        assertTrue(h.putusan().validateInvariant());
    }

    @Test
    void jumlahKolomSalahDitolak() throws IOException {
        HasilImpor h = baca(HEADER, "P009,hanya,tiga");
        assertEquals(0, h.jumlahValid());
        assertEquals(1, h.jumlahDitolak());
        assertTrue(h.galat().get(0).contains("16"));
    }

    @Test
    void caseIdDobelDitolak() throws IOException {
        HasilImpor h = baca(HEADER,
                baris("P001", "2026-06-17", "1", "24"),
                baris("P001", "2026-06-18", "2", "36"));
        assertEquals(1, h.jumlahValid());
        assertTrue(h.galat().get(0).contains("dobel"));
        assertEquals(24, h.putusan().get(0).vonisBulan());
    }

    @Test
    void headerSalahMelempar() {
        assertThrows(IllegalArgumentException.class,
                () -> baca("caseId,nomor", "P001,x"));
        assertThrows(IllegalArgumentException.class, () -> baca(""));
    }

    @Test
    void barisKosongDanBomDiabaikan() throws IOException {
        HasilImpor h = baca("\uFEFF" + HEADER, "",
                baris("P001", "2026-06-17", "1", "24"), "");
        assertEquals(1, h.jumlahValid());
        assertEquals(1, h.jumlahBaris());
        assertEquals(0, h.jumlahDitolak());
    }

    @Test
    void strukturTetapKonsistenSetelahGalat() throws IOException {
        HasilImpor h = baca(HEADER,
                baris("P001", "2026-06-17", "1", "24"),
                "rusak",
                baris("P002", "2026-06-17", "1", "24"));
        assertEquals(2, h.putusan().size());
        assertEquals(3 - 1, h.jumlahBaris() - h.jumlahDitolak());
        assertTrue(h.konsisten());
    }
}
