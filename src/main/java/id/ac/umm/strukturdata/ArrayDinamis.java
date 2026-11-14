package id.ac.umm.strukturdata;

/**
 * Array dinamis generik dibuat dari nol memakai array mentah.
 * Tidak memakai koleksi dari java.util.
 */
public class ArrayDinamis<T> implements StrukturData<T> {

    private static final int KAPASITAS_AWAL = 10;

    private Object[] data;
    private int size;
    private int langkahTerakhir;

    public ArrayDinamis() {
        this(KAPASITAS_AWAL);
    }

    public ArrayDinamis(int kapasitasAwal) {
        if (kapasitasAwal <= 0) {
            throw new IllegalArgumentException("Kapasitas awal harus > 0: " + kapasitasAwal);
        }
        this.data = new Object[kapasitasAwal];
        this.size = 0;
        this.langkahTerakhir = 0;
    }

    /** Menambah kapasitas (dua kali lipat) saat array penuh. */
    private void resize() {
        Object[] baru = new Object[data.length * 2];
        for (int i = 0; i < size; i++) {
            baru[i] = data[i];
        }
        data = baru;
    }

    @Override
    public void add(T elemen) {
        if (size == data.length) {
            resize();
        }
        data[size] = elemen;
        size++;
    }

    /** Menyisipkan elemen di index (0..size), menggeser elemen setelahnya ke kanan. */
    public void insert(int index, T elemen) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index " + index + ", size " + size);
        }
        if (size == data.length) {
            resize();
        }
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
        }
        data[index] = elemen;
        size++;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(int index) {
        cekIndex(index);
        return (T) data[index];
    }

    /** Mengganti elemen di index dan mengembalikan nilai lamanya. */
    @SuppressWarnings("unchecked")
    public T set(int index, T elemen) {
        cekIndex(index);
        T lama = (T) data[index];
        data[index] = elemen;
        return lama;
    }

    /** Menghapus elemen di index, menggeser elemen setelahnya ke kiri. */
    @Override
    @SuppressWarnings("unchecked")
    public T remove(int index) {
        cekIndex(index);
        T dihapus = (T) data[index];
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
        }
        data[size - 1] = null;
        size--;
        return dihapus;
    }

    /**
     * Pencarian linear. Mengembalikan index pertama yang cocok, atau -1.
     * Jumlah langkah (perbandingan) disimpan di getLangkahTerakhir().
     */
    public int search(T elemen) {
        langkahTerakhir = 0;
        for (int i = 0; i < size; i++) {
            langkahTerakhir++;
            if (sama(data[i], elemen)) {
                return i;
            }
        }
        return -1;
    }

    public int getLangkahTerakhir() {
        return langkahTerakhir;
    }

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
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
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

    /** Invarian: size <= kapasitas, elemen aktif [0,size) dan sisanya null. */
    public boolean validateInvariant() {
        if (size < 0 || size > data.length) {
            return false;
        }
        for (int i = size; i < data.length; i++) {
            if (data[i] != null) {
                return false;
            }
        }
        return true;
    }

    private void cekIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + ", size " + size);
        }
    }

    private boolean sama(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
