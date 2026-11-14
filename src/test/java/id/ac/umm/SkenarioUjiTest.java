package id.ac.umm;

import id.ac.umm.io.CsvImporter;
import id.ac.umm.io.HasilImpor;
import id.ac.umm.model.Putusan;
import id.ac.umm.service.FilterService;
import id.ac.umm.service.FilterService.HasilFilter;
import id.ac.umm.service.FilterService.Kriteria;
import id.ac.umm.service.PutusanService;
import id.ac.umm.service.PutusanService.HasilCari;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Skenario 1-5 dan 7 dari panduan bagian 4.1, dijalankan berurutan pada 50 record.
 * Data uji: Pi (i = 1..50), jenis Ganja bila i kelipatan 3 selain itu Sabu,
 * berat = i gram, vonis = 12 + i bulan.
 */
class SkenarioUjiTest {

    private static final String HEADER =
            "caseId,nomorPerkara,pengadilan,tanggalPutusan,terdakwaKode,jenisNarkotika,"
            + "beratBarangBuktiGram,pasalDakwaan,pasalTerbukti,tuntutanBulan,vonisBulan,"
            + "dendaRupiah,barangBukti,keadaanMemberatkan,keadaanMeringankan,sourcePages";

    private String baris(int i) {
        String id = String.format("P%03d", i);
        String jenis = (i % 3 == 0) ? "Ganja" : "Sabu";
        return String.join(",", id, (100 + i) + "/Pid.Sus/2026/PN Kdl",
                "Pengadilan Negeri Kendal", "2026-06-17", "Terdakwa-" + id, jenis,
                String.valueOf(i), "Pasal 114", "Pasal 114", "48", String.valueOf(12 + i),
                "0", "barang bukti", "memberatkan", "meringankan", "identitas:1");
    }

    private String csv(int n) {
        StringBuilder sb = new StringBuilder(HEADER);
        for (int i = 1; i <= n; i++) {
            sb.append("\n").append(baris(i));
        }
        return sb.toString();
    }

    private HasilImpor impor(String isi) throws IOException {
        return new CsvImporter().impor(new StringReader(isi));
    }

    private PutusanService muat50() throws IOException {
        return new PutusanService(impor(csv(50)).putusan());
    }

    private Putusan buat(String id) {
        return new Putusan(id, "9" + id + "/Pid.Sus/2026/PN Kdl", "Pengadilan Negeri Kendal",
                LocalDate.of(2026, 7, 1), "Terdakwa-" + id, "Sabu", 1.0,
                "Pasal 114", "Pasal 114", 0, 24, 0L, "", "", "", "");
    }

    @Test
    void skenario1_muat50RecordDanSizeLima0() throws IOException {
        HasilImpor h = impor(csv(50));
        assertTrue(h.tepat(50));
        assertEquals(50, h.jumlahValid());
        assertEquals(0, h.jumlahDitolak());
        assertEquals(50, h.putusan().size());
        assertTrue(h.konsisten());
        assertTrue(h.putusan().validateInvariant());
    }

    @Test
    void skenario2_cariAdaTidakAdaDanBatas() throws IOException {
        PutusanService s = muat50();

        HasilCari awal = s.cari("P001");
        assertEquals(0, awal.posisi());
        assertEquals(1, awal.langkah());

        HasilCari tengah = s.cari("P025");
        assertEquals(24, tengah.posisi());
        assertEquals(25, tengah.langkah());

        HasilCari akhir = s.cari("P050");
        assertEquals(49, akhir.posisi());
        assertEquals(50, akhir.langkah());

        HasilCari lewatNomorPerkara = s.cari("125/Pid.Sus/2026/PN Kdl");
        assertEquals(24, lewatNomorPerkara.posisi());
        assertEquals(25, lewatNomorPerkara.langkah());

        HasilCari tidakAda = s.cari("P999");
        assertFalse(tidakAda.ditemukan());
        assertEquals(50, tidakAda.langkah());
    }

    @Test
    void skenario3dan4_hapusPertamaTengahTerakhirLaluTambahTigaRecord() throws IOException {
        PutusanService s = muat50();

        // pertama
        assertEquals("P001", s.hapusKunci("P001").putusan().caseId());
        assertEquals(49, s.jumlah());
        assertEquals("P002", s.ambil(0).caseId());
        assertEquals("P050", s.ambil(48).caseId());

        // tengah: setelah pergeseran, posisi 24 berisi P026
        assertEquals("P026", s.hapusPosisi(24).caseId());
        assertEquals(48, s.jumlah());
        assertEquals("P027", s.ambil(24).caseId());

        // terakhir
        assertEquals("P050", s.hapusPosisi(s.jumlah() - 1).caseId());
        assertEquals(47, s.jumlah());
        assertEquals("P049", s.ambil(46).caseId());
        assertTrue(s.valid());

        // tambah kembali tiga record: masuk di akhir sesuai urutan
        s.tambah(buat("P901"));
        s.tambah(buat("P902"));
        s.tambah(buat("P903"));
        assertEquals(50, s.jumlah());
        assertEquals("P901", s.ambil(47).caseId());
        assertEquals("P902", s.ambil(48).caseId());
        assertEquals("P903", s.ambil(49).caseId());
        assertEquals("P002", s.ambil(0).caseId());
        assertTrue(s.valid());
    }

    @Test
    void skenario5_filterGabunganCocokDenganHitunganManual() throws IOException {
        FilterService f = new FilterService(muat50());

        // sabu (bukan kelipatan 3) dengan vonis 30-50 -> i = 18..38 tanpa kelipatan 3 = 14 data
        HasilFilter h1 = f.filter(new Kriteria("sabu", null, null, null, 30, 50));
        assertEquals(14, h1.hasil().size());
        assertEquals("P019", h1.hasil().get(0).caseId());
        assertEquals("P038", h1.hasil().get(13).caseId());
        for (int i = 0; i < h1.hasil().size(); i++) {
            Putusan p = h1.hasil().get(i);
            assertTrue(p.jenisNarkotika().equals("Sabu"));
            assertTrue(p.vonisBulan() >= 30 && p.vonisBulan() <= 50);
        }

        // sabu dengan berat 10-12 gram -> P010, P011 (P012 Ganja)
        HasilFilter h2 = f.filter(new Kriteria("sabu", 10.0, 12.0, null, null, null));
        assertEquals(2, h2.hasil().size());
        assertEquals("P010", h2.hasil().get(0).caseId());
        assertEquals("P011", h2.hasil().get(1).caseId());
        assertEquals(50, h2.diperiksa());
    }

    @Test
    void skenario7_recordTidakValidDitolakStrukturTetapKonsisten() throws IOException {
        String isi = HEADER
                + "\n" + baris(1)
                + "\n" + baris(2).replace("2026-06-17", "17-06-2026")
                + "\n" + baris(3).replace(",48,15,0,", ",48,-5,0,")
                + "\n" + baris(4);
        HasilImpor h = impor(isi);

        assertEquals(2, h.jumlahValid());
        assertEquals(2, h.jumlahDitolak());
        assertTrue(h.konsisten());
        assertTrue(h.putusan().validateInvariant());

        PutusanService s = new PutusanService(h.putusan());
        assertThrows(IllegalArgumentException.class, () -> s.tambah(buat("P001")));
        assertEquals(2, s.jumlah());
        assertTrue(s.valid());
    }
}