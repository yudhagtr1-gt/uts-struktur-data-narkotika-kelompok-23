package id.ac.umm.strukturdata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StackTest {

    private Stack<Integer> isi(int n) {
        Stack<Integer> s = new Stack<>();
        for (int i = 1; i <= n; i++) {
            s.push(i);
        }
        return s;
    }

    @Test
    void pushPopUrutanLifo() {
        Stack<Integer> s = isi(3);
        assertEquals(3, s.pop());
        assertEquals(2, s.pop());
        assertEquals(1, s.pop());
        assertTrue(s.isEmpty());
        assertTrue(s.validateInvariant());
    }

    @Test
    void peekTidakMenghapus() {
        Stack<Integer> s = isi(2);
        assertEquals(2, s.peek());
        assertEquals(2, s.size());
    }

    @Test
    void popDanPeekKosongMelempar() {
        Stack<Integer> s = new Stack<>();
        assertThrows(IllegalStateException.class, s::pop);
        assertThrows(IllegalStateException.class, s::peek);
    }

    @Test
    void resizeSaatPenuh() {
        Stack<Integer> s = isi(25);
        assertEquals(25, s.size());
        assertTrue(s.kapasitas() >= 25);
        assertEquals(25, s.peek());
        assertTrue(s.validateInvariant());
    }

    @Test
    void searchPreserveMenjagaUrutan() {
        Stack<Integer> s = isi(5);
        assertEquals(3, s.searchPreserve(2));
        assertEquals(4, s.getLangkahTerakhir());
        assertEquals(5, s.size());
        assertTrue(s.validateInvariant());
        for (int i = 5; i >= 1; i--) {
            assertEquals(i, s.pop());
        }
    }

    @Test
    void searchPreserveTidakDitemukan() {
        Stack<Integer> s = isi(5);
        assertEquals(-1, s.searchPreserve(99));
        assertEquals(5, s.getLangkahTerakhir());
        assertEquals(5, s.size());
        assertEquals(5, s.peek());
        assertEquals(1, s.get(4));
        assertTrue(s.validateInvariant());
    }

    @Test
    void containsTidakMengubahLangkahTerakhir() {
        Stack<Integer> s = isi(3);
        s.searchPreserve(1);
        int langkah = s.getLangkahTerakhir();
        assertTrue(s.contains(3));
        assertEquals(langkah, s.getLangkahTerakhir());
    }

    @Test
    void removeHanyaDariPuncak() {
        Stack<Integer> s = isi(3);
        assertThrows(UnsupportedOperationException.class, () -> s.remove(1));
        assertEquals(3, s.remove(0));
        assertEquals(2, s.size());
    }

    @Test
    void getIndexDiLuarBatasMelempar() {
        Stack<Integer> s = isi(1);
        assertThrows(IndexOutOfBoundsException.class, () -> s.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> s.get(1));
    }
}
