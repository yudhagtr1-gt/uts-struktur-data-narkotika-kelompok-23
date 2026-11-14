package id.ac.umm.strukturdata;

import id.ac.umm.model.Putusan;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Skenario 6 panduan: pencarian pada Stack tidak mengubah urutan akhir.
 * Urutan diperiksa lewat referensi objek (assertSame), bukan hanya jumlah.
 */
class StackSkenarioTest {

    private Putusan buat(String id, int vonis) {
        return new Putusan(id, "N-" + id, "Pengadilan Negeri Kendal",
                LocalDate.of(2026, 6, 17), "Terdakwa-001A", "Sabu",
                1.0, "Pasal 114", "Pasal 114", 48, vonis, 0L,
                "barang bukti", "", "", "identitas:1");
    }

    /** P001 di dasar, P050 di puncak. */
    private Stack<Putusan> isi50() {
        Stack<Putusan> s = new Stack<>();
        for (int i = 1; i <= 50; i++) {
            s.push(buat(String.format("P%03d", i), 12 + i));
        }
        return s;
    }

    private Object[] foto(Stack<?> s) {
        Object[] f = new Object[s.size()];
        for (int i = 0; i < f.length; i++) {
            f[i] = s.get(i);
        }
        return f;
    }

    private void samaUrutan(Object[] sebelum, Stack<?> s) {
        assertEquals(sebelum.length, s.size());
        for (int i = 0; i < sebelum.length; i++) {
            assertSame(sebelum[i], s.get(i), "urutan berubah pada kedalaman " + i);
        }
        assertTrue(s.validateInvariant());
    }

    @Test
    void skenario6_searchPreservePutusan50Record() {
        Stack<Putusan> s = isi50();
        Object[] sebelum = foto(s);

        // objek pembanding berbeda tetapi caseId sama (equals berdasarkan caseId)
        int kedalaman = s.searchPreserve(buat("P025", 0));

        assertEquals(25, kedalaman);
        assertEquals(26, s.getLangkahTerakhir());
        assertEquals("P050", s.peek().caseId());
        samaUrutan(sebelum, s);
    }

    @Test
    void searchPreserveDiPuncakDanDasar() {
        Stack<Putusan> s = isi50();
        Object[] sebelum = foto(s);

        assertEquals(0, s.searchPreserve(buat("P050", 0)));
        assertEquals(1, s.getLangkahTerakhir());
        samaUrutan(sebelum, s);

        assertEquals(49, s.searchPreserve(buat("P001", 0)));
        assertEquals(50, s.getLangkahTerakhir());
        samaUrutan(sebelum, s);
    }

    @Test
    void searchPreserveTidakAdaPada50Record() {
        Stack<Putusan> s = isi50();
        Object[] sebelum = foto(s);

        assertEquals(-1, s.searchPreserve(buat("P999", 0)));
        assertEquals(50, s.getLangkahTerakhir());
        samaUrutan(sebelum, s);
    }

    @Test
    void searchPreserveBerulangMengesetLangkah() {
        Stack<Putusan> s = isi50();
        Object[] sebelum = foto(s);

        assertEquals(40, s.searchPreserve(buat("P010", 0)));
        assertEquals(41, s.getLangkahTerakhir());

        assertEquals(10, s.searchPreserve(buat("P040", 0)));
        assertEquals(11, s.getLangkahTerakhir());

        assertEquals(-1, s.searchPreserve(buat("P999", 0)));
        assertEquals(50, s.getLangkahTerakhir());

        samaUrutan(sebelum, s);
    }

    @Test
    void searchPreserveStackKosong() {
        Stack<Putusan> s = new Stack<>();
        assertEquals(-1, s.searchPreserve(buat("P001", 0)));
        assertEquals(0, s.getLangkahTerakhir());
        assertEquals(0, s.size());
        assertTrue(s.validateInvariant());
    }

    @Test
    void searchPreserveSetelahResize() {
        Stack<Integer> s = new Stack<>();
        for (int i = 1; i <= 25; i++) {
            s.push(i);
        }
        assertTrue(s.kapasitas() >= 25);

        assertEquals(24, s.searchPreserve(1));
        assertEquals(25, s.getLangkahTerakhir());
        assertEquals(25, s.size());
        for (int i = 0; i < 25; i++) {
            assertEquals(25 - i, s.get(i));
        }
        assertTrue(s.validateInvariant());
    }

    @Test
    void searchPreserveElemenNull() {
        Stack<Integer> s = new Stack<>();
        s.push(1);
        s.push(null);
        s.push(3);

        assertEquals(1, s.searchPreserve(null));
        assertEquals(2, s.getLangkahTerakhir());
        assertEquals(3, s.pop());
        assertNull(s.pop());
        assertEquals(1, s.pop());
        assertTrue(s.validateInvariant());
    }

    @Test
    void restoreTanpaPencarianTidakMengubahApaPun() {
        Stack<Integer> s = new Stack<>();
        s.push(1);
        s.push(2);
        s.push(3);
        s.restore();
        assertEquals(3, s.size());
        assertEquals(3, s.peek());
        assertEquals(1, s.get(2));
        assertTrue(s.validateInvariant());
    }

    @Test
    void removePuncakPadaStackKosongMelempar() {
        Stack<Integer> s = new Stack<>();
        assertThrows(IllegalStateException.class, () -> s.remove(0));
        assertTrue(s.validateInvariant());
    }
}