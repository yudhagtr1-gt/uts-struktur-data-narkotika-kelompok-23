package id.ac.umm.service;

import id.ac.umm.model.Putusan;
import id.ac.umm.service.FilterService.HasilFilter;
import id.ac.umm.service.FilterService.Kriteria;
import id.ac.umm.strukturdata.ArrayDinamis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilterServiceTest {

    private PutusanService service;
    private FilterService filter;

    private Putusan buat(String id, String jenis, double berat, String pasal, int vonis) {
        return new Putusan(id, "N-" + id, "Pengadilan Negeri Kendal",
                LocalDate.of(2026, 6, 17), "Terdakwa-001A", jenis,
                berat, pasal, pasal, 48, vonis, 0L,
                "barang bukti", "", "", "identitas:1");
    }

    @BeforeEach
    void siapkan() {
        service = new PutusanService(new ArrayDinamis<>());
        service.tambah(buat("P1", "Sabu (Metamfetamina)", 1.0, "Pasal 114 ayat (1)", 24));
        service.tambah(buat("P2", "Sabu (Metamfetamina)", 5.0, "Pasal 112 ayat (1)", 60));
        service.tambah(buat("P3", "Ganja", 100.0, "Pasal 111 ayat (1)", 12));
        service.tambah(buat("P4", "Tembakau sintetis", 0.5, "Pasal 114 ayat (1)", 36));
        service.tambah(buat("P5", "Sabu dan ganja", 10.0, "Pasal 127 ayat (1)", 48));
        filter = new FilterService(service);
    }

    private String ids(HasilFilter h) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < h.hasil().size(); i++) {
            sb.append(h.hasil().get(i).caseId());
            if (i < h.hasil().size() - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    @Test
    void kriteriaKosongMengembalikanSemua() {
        HasilFilter h = filter.filter(Kriteria.kosong());
        assertEquals(5, h.hasil().size());
        assertEquals(5, h.diperiksa());
        assertEquals("P1,P2,P3,P4,P5", ids(h));
    }

    @Test
    void filterJenisTidakPeduliHurufBesar() {
        HasilFilter h = filter.filter(new Kriteria("SABU", null, null, null, null, null));
        assertEquals("P1,P2,P5", ids(h));
    }

    @Test
    void filterRentangBeratInklusif() {
        HasilFilter h = filter.filter(new Kriteria(null, 1.0, 10.0, null, null, null));
        assertEquals("P1,P2,P5", ids(h));
    }

    @Test
    void filterPasalTerbukti() {
        HasilFilter h = filter.filter(new Kriteria(null, null, null, "114", null, null));
        assertEquals("P1,P4", ids(h));
    }

    @Test
    void filterRentangVonisInklusif() {
        HasilFilter h = filter.filter(new Kriteria(null, null, null, null, 24, 48));
        assertEquals("P1,P4,P5", ids(h));
    }

    @Test
    void filterGabunganMenjagaUrutan() {
        HasilFilter h = filter.filter(new Kriteria("sabu", null, null, null, 30, null));
        assertEquals("P2,P5", ids(h));
        assertTrue(h.hasil().validateInvariant());
    }

    @Test
    void tidakAdaYangCocokMenghasilkanKosong() {
        HasilFilter h = filter.filter(new Kriteria("heroin", null, null, null, null, null));
        assertTrue(h.hasil().isEmpty());
        assertEquals(5, h.diperiksa());
        assertTrue(h.hasil().validateInvariant());
    }

    @Test
    void rentangTerbalikDitolak() {
        assertThrows(IllegalArgumentException.class,
                () -> new Kriteria(null, 10.0, 1.0, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Kriteria(null, null, null, null, 60, 12));
        assertThrows(IllegalArgumentException.class, () -> filter.filter(null));
    }

    @Test
    void arsipAsliTidakBerubah() {
        filter.filter(new Kriteria("sabu", null, null, null, null, null));
        assertEquals(5, service.jumlah());
        assertEquals("P1", service.ambil(0).caseId());
        assertEquals("P5", service.ambil(4).caseId());
        assertTrue(service.valid());
    }
}