package id.ac.umm.service;

import id.ac.umm.model.Putusan;
import id.ac.umm.service.PutusanService.HasilCari;
import id.ac.umm.strukturdata.ArrayDinamis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PutusanServiceTest {

    private PutusanService service;

    private Putusan buat(String id, String nomor) {
        return new Putusan(id, nomor, "Pengadilan Negeri Kendal",
                LocalDate.of(2026, 6, 17), "Terdakwa-001A", "Sabu",
                1.0, "Pasal 114", "Pasal 114", 48, 24, 0L,
                "1 paket", "", "", "identitas:1");
    }

    @BeforeEach
    void siapkan() {
        service = new PutusanService(new ArrayDinamis<>());
        for (int i = 1; i <= 5; i++) {
            service.tambah(buat("P" + i, "N" + i));
        }
    }

    @Test
    void tambahMenambahSatu() {
        service.tambah(buat("P6", "N6"));
        assertEquals(6, service.jumlah());
        assertEquals("P6", service.ambil(5).caseId());
        assertTrue(service.valid());
    }

    @Test
    void tambahDobelDitolakStrukturTetap() {
        assertThrows(IllegalArgumentException.class, () -> service.tambah(buat("P3", "NX")));
        assertThrows(IllegalArgumentException.class, () -> service.tambah(null));
        assertEquals(5, service.jumlah());
        assertTrue(service.valid());
    }

    @Test
    void cariPertamaTerakhirDanTidakAda() {
        HasilCari pertama = service.cari("P1");
        assertEquals(0, pertama.posisi());
        assertEquals(1, pertama.langkah());

        HasilCari terakhir = service.cari("P5");
        assertEquals(4, terakhir.posisi());
        assertEquals(5, terakhir.langkah());

        HasilCari tidakAda = service.cari("P99");
        assertFalse(tidakAda.ditemukan());
        assertEquals(5, tidakAda.langkah());
        assertNull(tidakAda.putusan());
    }

    @Test
    void cariByNomorPerkaraDanKunciNull() {
        assertEquals(2, service.cari("N3").posisi());
        assertFalse(service.cari(null).ditemukan());
    }

    @Test
    void hapusPertamaTengahTerakhir() {
        assertEquals("P1", service.hapusKunci("P1").putusan().caseId());
        assertEquals(4, service.jumlah());
        assertEquals("P2", service.ambil(0).caseId());

        assertEquals(1, service.hapusKunci("P3").posisi());
        assertEquals(3, service.jumlah());

        assertEquals(2, service.hapusKunci("P5").posisi());
        assertEquals(2, service.jumlah());

        assertEquals("P2", service.ambil(0).caseId());
        assertEquals("P4", service.ambil(1).caseId());
        assertTrue(service.valid());
    }

    @Test
    void hapusKunciTidakAdaTidakMengubahApaPun() {
        assertFalse(service.hapusKunci("P99").ditemukan());
        assertEquals(5, service.jumlah());
    }

    @Test
    void tampilSesuaiUrutanStruktur() {
        String[] baris = service.tampil().split("\n");
        assertEquals(5, baris.length);
        assertTrue(baris[0].contains("P1"));
        assertTrue(baris[4].contains("P5"));
    }
}