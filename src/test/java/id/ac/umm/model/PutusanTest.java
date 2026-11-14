package id.ac.umm.model;

import id.ac.umm.strukturdata.ArrayDinamis;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PutusanTest {

    private Putusan buat(String id, int vonis) {
        return new Putusan(id, "38/Pid.Sus/2026/PN Kdl", "Pengadilan Negeri Kendal",
                LocalDate.of(2026, 6, 17), "Terdakwa-001A", "Sabu (Metamfetamina)",
                3.16496, "Pasal 114 ayat (1)", "Pasal 114 ayat (1)",
                48, vonis, 0L, "1 paket sabu", "meresahkan", "sopan", "identitas:1");
    }

    @Test
    void membuatPutusanValid() {
        Putusan p = buat("P001", 24);
        assertEquals("P001", p.caseId());
        assertEquals(24, p.vonisBulan());
        assertEquals(LocalDate.of(2026, 6, 17), p.tanggalPutusan());
    }

    @Test
    void teksNullMenjadiKosong() {
        Putusan p = new Putusan("P002", "42/Pid.Sus/2026/PN Kdl", null,
                LocalDate.of(2026, 6, 17), null, null,
                0, null, null, 0, 0, 0L, null, null, null, null);
        assertEquals("", p.pasalTerbukti());
        assertEquals("", p.sourcePages());
    }

    @Test
    void dataTidakValidDitolak() {
        LocalDate t = LocalDate.of(2026, 1, 1);
        assertThrows(IllegalArgumentException.class, () -> new Putusan(
                " ", "N", "", t, "", "", 0, "", "", 0, 0, 0L, "", "", "", ""));
        assertThrows(IllegalArgumentException.class, () -> new Putusan(
                "P1", "", "", t, "", "", 0, "", "", 0, 0, 0L, "", "", "", ""));
        assertThrows(IllegalArgumentException.class, () -> new Putusan(
                "P1", "N", "", null, "", "", 0, "", "", 0, 0, 0L, "", "", "", ""));
        assertThrows(IllegalArgumentException.class, () -> new Putusan(
                "P1", "N", "", t, "", "", -1, "", "", 0, 0, 0L, "", "", "", ""));
        assertThrows(IllegalArgumentException.class, () -> new Putusan(
                "P1", "N", "", t, "", "", 0, "", "", 0, -5, 0L, "", "", "", ""));
        assertThrows(IllegalArgumentException.class, () -> new Putusan(
                "P1", "N", "", t, "", "", 0, "", "", 0, 0, -1L, "", "", "", ""));
    }

    @Test
    void equalsBerdasarkanCaseId() {
        assertEquals(buat("P001", 24), buat("P001", 60));
        assertNotEquals(buat("P001", 24), buat("P002", 24));
        assertEquals(buat("P001", 24).hashCode(), buat("P001", 60).hashCode());
    }

    @Test
    void ringkasanMemuatIdentitas() {
        String r = buat("P001", 24).ringkasan();
        assertTrue(r.contains("P001"));
        assertTrue(r.contains("24 bln"));
    }

    @Test
    void bisaDisimpanDiArrayDinamis() {
        ArrayDinamis<Putusan> arsip = new ArrayDinamis<>();
        arsip.add(buat("P001", 24));
        arsip.add(buat("P002", 36));
        assertEquals(2, arsip.size());
        assertTrue(arsip.contains(buat("P002", 0)));
        assertEquals(1, arsip.search(buat("P001", 0)) + 1);
        assertTrue(arsip.validateInvariant());
    }
}
