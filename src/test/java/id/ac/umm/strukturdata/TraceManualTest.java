package id.ac.umm.strukturdata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Membuktikan bahwa angka dan kondisi di docs/trace/trace-manual.md cocok dengan kode. */
class TraceManualTest {

    private void cocok(ArrayDinamis<String> a, String... urut) {
        assertEquals(urut.length, a.size());
        for (int i = 0; i < urut.length; i++) {
            assertEquals(urut[i], a.get(i));
        }
        assertTrue(a.validateInvariant());
    }

    @Test
    void traceArrayDinamisResizeHapusInsertDanCari() {
        ArrayDinamis<String> a = new ArrayDinamis<>(3);
        a.add("A");
        a.add("B");
        a.add("C");
        assertEquals(3, a.kapasitas());
        a.add("D");
        assertEquals(6, a.kapasitas());
        a.add("E");
        cocok(a, "A", "B", "C", "D", "E");

        assertEquals("B", a.remove(1));
        cocok(a, "A", "C", "D", "E");
        assertEquals("A", a.remove(0));
        cocok(a, "C", "D", "E");
        assertEquals("E", a.remove(2));
        cocok(a, "C", "D");

        a.insert(1, "X");
        cocok(a, "C", "X", "D");

        assertEquals(2, a.search("D"));
        assertEquals(3, a.getLangkahTerakhir());
        assertEquals(-1, a.search("Z"));
        assertEquals(3, a.getLangkahTerakhir());
    }

    @Test
    void traceStackSearchPreserve() {
        Stack<Integer> s = new Stack<>();
        for (int i = 1; i <= 5; i++) {
            s.push(i);
        }
        assertEquals(3, s.searchPreserve(2));
        assertEquals(4, s.getLangkahTerakhir());
        assertEquals(5, s.size());
        for (int i = 0; i < 5; i++) {
            assertEquals(5 - i, s.get(i));
        }
        assertTrue(s.validateInvariant());
    }
}