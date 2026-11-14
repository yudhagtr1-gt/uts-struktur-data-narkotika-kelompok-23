package id.ac.umm.strukturdata;

/**
 * Kontrak struktur data utama (bagian 3.2 panduan).
 */
public interface StrukturData<T> {
    void add(T elemen);
    T get(int index);
    T remove(int index);
    int size();
    boolean isEmpty();
    boolean contains(T elemen);
    void clear();
}
