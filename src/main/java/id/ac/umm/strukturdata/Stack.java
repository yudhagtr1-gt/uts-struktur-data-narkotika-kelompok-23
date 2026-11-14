package id.ac.umm.strukturdata;

/**
 * Stack generik (LIFO) dibuat dari nol memakai array mentah.
 * Pemetaan ke StrukturData: add = push, get(0) = puncak, remove(0) = pop.
 */
public class Stack<T> implements StrukturData<T> {

    private static final int KAPASITAS_AWAL = 10;

    private Object[] data;
    private int size;
    private Object[] cadangan;
    private int jumlahCadangan;
    private int langkahTerakhir;

    public Stack() {
        this.data = new Object[KAPASITAS_AWAL];
        this.size = 0;
        this.cadangan = new Object[0];
        this.jumlahCadangan = 0;
        this.langkahTerakhir = 0;
    }

    private void resize() {
        Object[] baru = new Object[data.length * 2];
        for (int i = 0; i < size; i++) {
            baru[i] = data[i];
        }
        data = baru;
    }

    public void push(T elemen) {
        if (size == data.length) {
            resize();
        }
        data[size] = elemen;
        size++;
    }

    @SuppressWarnings("unchecked")
    public T pop() {
        if (size == 0) {
            throw new IllegalStateException("Stack kosong");
        }
        T atas = (T) data[size - 1];
        data[size - 1] = null;
        size--;
        return atas;
    }

    @SuppressWarnings("unchecked")
    public T peek() {
        if (size == 0) {
            throw new IllegalStateException("Stack kosong");
        }
        return (T) data[size - 1];
    }

    /**
     * Pencarian non-destruktif dari puncak. Elemen dipindah ke penyangga,
     * lalu restore() mengembalikannya. Mengembalikan kedalaman dari puncak
     * (0 = puncak) atau -1. Jumlah langkah ada di getLangkahTerakhir().
     */
    public int searchPreserve(T elemen) {
        langkahTerakhir = 0;
        cadangan = new Object[size];
        jumlahCadangan = 0;
        int hasil = -1;
        while (size > 0) {
            langkahTerakhir++;
            cadangan[jumlahCadangan] = data[size - 1];
            jumlahCadangan++;
            data[size - 1] = null;
            size--;
            if (sama(cadangan[jumlahCadangan - 1], elemen)) {
                hasil = jumlahCadangan - 1;
                break;
            }
        }
        restore();
        return hasil;
    }

    /** Mengembalikan elemen dari penyangga ke stack dengan urutan semula. */
    public void restore() {
        while (jumlahCadangan > 0) {
            jumlahCadangan--;
            data[size] = cadangan[jumlahCadangan];
            cadangan[jumlahCadangan] = null;
            size++;
        }
    }

    public int getLangkahTerakhir() {
        return langkahTerakhir;
    }

    @Override
    public void add(T elemen) {
        push(elemen);
    }

    /** Posisi 0 = puncak. */
    @Override
    @SuppressWarnings("unchecked")
    public T get(int position) {
        if (position < 0 || position >= size) {
            throw new IndexOutOfBoundsException("Posisi " + position + ", size " + size);
        }
        return (T) data[size - 1 - position];
    }

    /** Hanya posisi 0 (puncak) yang didukung. */
    @Override
    public T remove(int position) {
        if (position != 0) {
            throw new UnsupportedOperationException("Stack hanya boleh menghapus dari puncak (posisi 0)");
        }
        return pop();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /** Tidak mengubah langkahTerakhir. */
    @Override
    public boolean contains(T elemen) {
        for (int i = 0; i < size; i++) {
            if (sama(data[i], elemen)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            data[i] = null;
        }
        size = 0;
    }

    public int kapasitas() {
        return data.length;
    }

    /** Invarian: 0 <= size <= kapasitas, slot sisa null, penyangga kosong. */
    public boolean validateInvariant() {
        if (size < 0 || size > data.length || jumlahCadangan != 0) {
            return false;
        }
        for (int i = size; i < data.length; i++) {
            if (data[i] != null) {
                return false;
            }
        }
        return true;
    }

    private boolean sama(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
