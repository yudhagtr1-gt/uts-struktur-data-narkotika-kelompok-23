package id.ac.umm.service;

import id.ac.umm.model.Putusan;
import id.ac.umm.service.AgregasiService.Ringkasan;
import id.ac.umm.service.FilterService.Kriteria;
import id.ac.umm.strukturdata.ArrayDinamis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AgregasiServiceTest {

    private PutusanService service;
    private AgregasiService agregasi;

    private Putusan buat(String id, String jenis, int vonis, long denda) {
        return new Putusan(id, "N-" + id, "Pengadilan Negeri Kendal",
                LocalDate.of(2026, 6, 17), "Terdakwa-001A", jenis,
                1.0, "Pasal 114", "Pasal 114", 48, vonis, denda,
                "barang bukti", "", "", "identitas:1");
    }

    @BeforeEach
    void siapkan() {
        service = new PutusanService(new ArrayDinamis<>());
        service.tambah(buat("P1", "Sabu (Metamfetamina)", 24, 0L));
        service.tambah(buat("P2", "Sabu (Metamfetamina)", 60, 1_000_000_000L));
        service.tambah(buat("P3", "Ganja", 12, 800_000_000L));
        service.tambah(buat("P4", "Ganja", 36, 0L));
        service.tambah(buat("P5", "Tembakau sintetis", 48, 0L));
        agregasi = new AgregasiService(service);
    }

    @Test
    void menghitungJumlahRataRataDanTotalDenda() {
        Ringkasan r = agregasi.hitung();
        assertEquals(5, r.jumlahPerkara());
        assertEquals(36.0, r.rataRataVonis(), 1e-9);
        assertEquals(1_800_000_000L, r.totalDenda());
    }

    @Test
    void distribusiJenisSesuaiUrutanKemunculan() {
        Ringkasan r = agregasi.hitung();
        assertEquals(3, r.distribusi().size());
        assertEquals("Sabu (Metamfetamina)", r.distribusi().get(0).jenis());
        assertEquals(2, r.distribusi().get(0).jumlah());
        assertEquals("Ganja", r.distribusi().get(1).jenis());
        assertEquals(2, r.distribusi().get(1).jumlah());
        assertEquals("Tembakau sintetis", r.distribusi().get(2).jenis());
        assertEquals(1, r.distribusi().get(2).jumlah());
        assertTrue(r.distribusi().validateInvariant());
    }

    @Test
    void jumlahDistribusiSamaDenganJumlahPerkara() {
        Ringkasan r = agregasi.hitung();
        int total = 0;
        for (int i = 0; i < r.distribusi().size(); i++) {
            total += r.distribusi().get(i).jumlah();
        }
        assertEquals(r.jumlahPerkara(), total);
    }

    @Test
    void arsipKosongMenghasilkanNol() {
        AgregasiService kosong = new AgregasiService(new PutusanService(new ArrayDinamis<>()));
        Ringkasan r = kosong.hitung();
        assertEquals(0, r.jumlahPerkara());
        assertEquals(0.0, r.rataRataVonis(), 1e-9);
        assertEquals(0L, r.totalDenda());
        assertTrue(r.distribusi().isEmpty());
    }

    @Test
    void agregasiHasilFilter() {
        FilterService filter = new FilterService(service);
        FilterService.HasilFilter h = filter.filter(new Kriteria("sabu", null, null, null, null, null));
        Ringkasan r = agregasi.hitung(h.hasil());
        assertEquals(2, r.jumlahPerkara());
        assertEquals(42.0, r.rataRataVonis(), 1e-9);
        assertEquals(1_000_000_000L, r.totalDenda());
        assertEquals(1, r.distribusi().size());
    }

    @Test
    void jenisKosongDigabungKeLabelKosong() {
        PutusanService s = new PutusanService(new ArrayDinamis<>());
        s.tambah(buat("P1", "", 10, 0L));
        s.tambah(buat("P2", "", 20, 0L));
        s.tambah(buat("P3", "Sabu", 30, 0L));
        Ringkasan r = new AgregasiService(s).hitung();
        assertEquals(AgregasiService.LABEL_KOSONG, r.distribusi().get(0).jenis());
        assertEquals(2, r.distribusi().get(0).jumlah());
        assertEquals("Sabu", r.distribusi().get(1).jenis());
    }

    @Test
    void arsipTidakBerubahDanNullDitolak() {
        agregasi.hitung();
        assertEquals(5, service.jumlah());
        assertEquals("P1", service.ambil(0).caseId());
        assertTrue(service.valid());
        assertThrows(IllegalArgumentException.class, () -> agregasi.hitung(null));
        assertThrows(IllegalArgumentException.class, () -> new AgregasiService(null));
    }
}