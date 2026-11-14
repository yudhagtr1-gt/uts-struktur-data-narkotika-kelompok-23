package id.ac.umm.strukturdata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArrayDinamisTest {

    @Test
    void addMenambahDanResizeSaatPenuh() {
        ArrayDinamis<Integer> a = new ArrayDinamis<>(2);
        a.add(1);
        a.add(2);
        a.add(3);
        assertEquals(3, a.size());
        assertTrue(a.kapasitas() > 2);
        assertEquals(3, a.get(2));
        assertTrue(a.validateInvariant());
    }

    @Test
    void insertMenggeserKeKanan() {
        ArrayDinamis<String> a = new ArrayDinamis<>();
        a.add("A");
        a.add("C");
        a.insert(1, "B");
        assertEquals("B", a.get(1));
        assertEquals("C", a.get(2));
        assertEquals(3, a.size());
        assertTrue(a.validateInvariant());
    }

    @Test
    void removeMenggeserKeKiri() {
        ArrayDinamis<String> a = new ArrayDinamis<>();
        a.add("A");
        a.add("B");
        a.add("C");
        assertEquals("B", a.remove(1));
        assertEquals("C", a.get(1));
        assertEquals(2, a.size());
        assertTrue(a.validateInvariant());
    }

    @Test
    void setMengembalikanNilaiLama() {
        ArrayDinamis<String> a = new ArrayDinamis<>();
        a.add("A");
        assertEquals("A", a.set(0, "Z"));
        assertEquals("Z", a.get(0));
    }

    @Test
    void searchMelaporkanLangkah() {
        ArrayDinamis<Integer> a = new ArrayDinamis<>();
        a.add(10);
        a.add(20);
        a.add(30);
        assertEquals(2, a.search(30));
        assertEquals(3, a.getLangkahTerakhir());
        assertEquals(-1, a.search(99));
        assertEquals(3, a.getLangkahTerakhir());
        assertTrue(a.contains(20));
    }

    @Test
    void indexDiLuarBatasMelempar() {
        ArrayDinamis<Integer> a = new ArrayDinamis<>();
        a.add(1);
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> a.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> a.set(5, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> a.remove(1));
        assertThrows(IndexOutOfBoundsException.class, () -> a.insert(3, 0));
    }

    @Test
    void clearDanIsEmpty() {
        ArrayDinamis<Integer> a = new ArrayDinamis<>();
        assertTrue(a.isEmpty());
        a.add(1);
        assertFalse(a.isEmpty());
        a.clear();
        assertTrue(a.isEmpty());
        assertEquals(0, a.size());
        assertTrue(a.validateInvariant());
    }
}
