package id.ac.umm.service;

import id.ac.umm.model.Putusan;
import id.ac.umm.service.UrutanService.Arah;
import id.ac.umm.service.UrutanService.HasilUrut;
import id.ac.umm.strukturdata.ArrayDinamis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UrutanServiceTest {

    private PutusanService service;
    private UrutanService urutan;

    private Putusan buat(String id, int vonis) {
        return new Putusan(id, "N-" + id, "Pengadilan Negeri Kendal",
                LocalDate.of(2026, 6, 17), "Terdakwa-001A", "Sabu",
                1.0, "Pasal 114", "Pasal 114", 48, vonis, 0L,
                "barang bukti", "", "", "identitas:1");
    }

    @BeforeEach
    void siapkan() {
        service = new PutusanService(new ArrayDinamis<>());
        service.tambah(buat("P1", 24));
        service.tambah(buat("P2", 60));
        service.tambah(buat("P3", 12));
        service.tambah(buat("P4", 36));
        service.tambah(buat("P5", 48));
        service.tambah(buat("P6", 24));
        urutan = new UrutanService(service);
    }

    private String ids(ArrayDinamis<Putusan> data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.size(); i++) {
            sb.append(data.get(i).caseId());
            if (i < data.size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    @Test
    void urutNaikDanStabil() {
        HasilUrut h = urutan.urutkanVonis(Arah.NAIK);
        assertEquals("P3,P1,P6,P4,P5,P2", ids(h.hasil()));
        assertTrue(h.hasil().validateInvariant());
    }

    @Test
    void urutTurunDanStabil() {
        HasilUrut h = urutan.urutkanVonis(Arah.TURUN);
        assertEquals("P2,P5,P4,P1,P6,P3", ids(h.hasil()));
    }

    @Test
    void arsipUtamaTidakBerubah() {
        urutan.urutkanVonis(Arah.NAIK);
        assertEquals(6, service.jumlah());
        assertEquals("P1", service.ambil(0).caseId());
        assertEquals("P6", service.ambil(5).caseId());
        assertTrue(service.valid());
    }

    @Test
    void sudahTerurutHanyaButuhNMinusSatuPerbandinganTanpaPergeseran() {
        HasilUrut pertama = urutan.urutkanVonis(Arah.NAIK);
        HasilUrut kedua = urutan.urutkan(pertama.hasil(), Arah.NAIK);
        assertEquals(5, kedua.perbandingan());
        assertEquals(0, kedua.pergeseran());
    }

    @Test
    void pergeseranSamaDenganJumlahInversi() {
        HasilUrut turun = urutan.urutkanVonis(Arah.TURUN);
        HasilUrut balik = urutan.urutkan(turun.hasil(), Arah.NAIK);
        assertEquals(14, balik.pergeseran());
        assertEquals("P3,P1,P6,P4,P5,P2", ids(balik.hasil()));
    }

    @Test
    void kosongDanSatuElemen() {
        UrutanService kosong = new UrutanService(new PutusanService(new ArrayDinamis<>()));
        HasilUrut h = kosong.urutkanVonis(Arah.NAIK);
        assertTrue(h.hasil().isEmpty());
        assertEquals(0, h.perbandingan());

        PutusanService satu = new PutusanService(new ArrayDinamis<>());
        satu.tambah(buat("P1", 10));
        HasilUrut h1 = new UrutanService(satu).urutkanVonis(Arah.TURUN);
        assertEquals(1, h1.hasil().size());
        assertEquals(0, h1.perbandingan());
    }

    @Test
    void nullDitolak() {
        assertThrows(IllegalArgumentException.class, () -> new UrutanService(null));
        assertThrows(IllegalArgumentException.class, () -> urutan.urutkan(null, Arah.NAIK));
        assertThrows(IllegalArgumentException.class, () -> urutan.urutkanVonis(null));
    }
}