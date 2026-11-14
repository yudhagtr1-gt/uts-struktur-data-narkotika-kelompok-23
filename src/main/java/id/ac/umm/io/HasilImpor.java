package id.ac.umm.io;

import id.ac.umm.model.Putusan;
import id.ac.umm.strukturdata.ArrayDinamis;

/**
 * Hasil impor CSV: putusan yang valid, pesan galat per baris yang ditolak,
 * dan jumlah baris data yang dibaca.
 */
public final class HasilImpor {

    private final ArrayDinamis<Putusan> putusan = new ArrayDinamis<>(64);
    private final ArrayDinamis<String> galat = new ArrayDinamis<>();
    private int jumlahBaris;

    void tambahBaris() {
        jumlahBaris++;
    }

    void tolak(String pesan) {
        galat.add(pesan);
    }

    public ArrayDinamis<Putusan> putusan() {
        return putusan;
    }

    public ArrayDinamis<String> galat() {
        return galat;
    }

    public int jumlahBaris() {
        return jumlahBaris;
    }

    public int jumlahValid() {
        return putusan.size();
    }

    public int jumlahDitolak() {
        return galat.size();
    }

    /** True bila jumlah putusan valid tepat sama dengan target (mis. 50). */
    public boolean tepat(int target) {
        return putusan.size() == target;
    }

    /** Setiap baris data harus berakhir sebagai valid atau ditolak, tidak ada yang hilang. */
    public boolean konsisten() {
        return putusan.size() + galat.size() == jumlahBaris && putusan.validateInvariant();
    }
}
