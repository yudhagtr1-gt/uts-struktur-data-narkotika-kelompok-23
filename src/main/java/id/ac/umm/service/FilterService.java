package id.ac.umm.service;

import id.ac.umm.model.Putusan;
import id.ac.umm.strukturdata.ArrayDinamis;

/**
 * Filter gabungan di atas arsip. Kriteria yang null dilewati.
 * Arsip tidak diubah; hasil dikembalikan sebagai ArrayDinamis baru
 * dengan urutan yang sama seperti di struktur utama.
 */
public class FilterService {

    /** Semua kriteria opsional (null = tidak dipakai). Rentang bersifat inklusif. */
    public record Kriteria(String jenis, Double beratMin, Double beratMax,
                           String pasalTerbukti, Integer vonisMin, Integer vonisMax) {

        public Kriteria {
            jenis = kosongJadiNull(jenis);
            pasalTerbukti = kosongJadiNull(pasalTerbukti);
            if (beratMin != null && beratMax != null && beratMin > beratMax) {
                throw new IllegalArgumentException("beratMin lebih besar dari beratMax");
            }
            if (vonisMin != null && vonisMax != null && vonisMin > vonisMax) {
                throw new IllegalArgumentException("vonisMin lebih besar dari vonisMax");
            }
        }

        public static Kriteria kosong() {
            return new Kriteria(null, null, null, null, null, null);
        }

        private static String kosongJadiNull(String s) {
            return (s == null || s.isBlank()) ? null : s.trim();
        }
    }

    /** Hasil filter beserta jumlah elemen yang diperiksa. */
    public record HasilFilter(ArrayDinamis<Putusan> hasil, int diperiksa) {
    }

    private final PutusanService service;

    public FilterService(PutusanService service) {
        if (service == null) {
            throw new IllegalArgumentException("service tidak boleh null");
        }
        this.service = service;
    }

    public HasilFilter filter(Kriteria k) {
        if (k == null) {
            throw new IllegalArgumentException("kriteria tidak boleh null");
        }
        ArrayDinamis<Putusan> hasil = new ArrayDinamis<>();
        int diperiksa = 0;
        for (int i = 0; i < service.jumlah(); i++) {
            diperiksa++;
            Putusan p = service.ambil(i);
            if (cocok(p, k)) {
                hasil.add(p);
            }
        }
        return new HasilFilter(hasil, diperiksa);
    }

    private boolean cocok(Putusan p, Kriteria k) {
        if (k.jenis() != null
                && !p.jenisNarkotika().toLowerCase().contains(k.jenis().toLowerCase())) {
            return false;
        }
        if (k.beratMin() != null && p.beratBarangBuktiGram() < k.beratMin()) {
            return false;
        }
        if (k.beratMax() != null && p.beratBarangBuktiGram() > k.beratMax()) {
            return false;
        }
        if (k.pasalTerbukti() != null
                && !p.pasalTerbukti().toLowerCase().contains(k.pasalTerbukti().toLowerCase())) {
            return false;
        }
        if (k.vonisMin() != null && p.vonisBulan() < k.vonisMin()) {
            return false;
        }
        if (k.vonisMax() != null && p.vonisBulan() > k.vonisMax()) {
            return false;
        }
        return true;
    }
}