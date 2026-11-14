package id.ac.umm.service;

import id.ac.umm.model.Putusan;
import id.ac.umm.strukturdata.ArrayDinamis;

/**
 * Mengurutkan putusan berdasarkan vonis. Hasilnya berupa salinan terurut,
 * arsip utama tidak diubah. Memakai insertion sort buatan sendiri (stabil),
 * tanpa Arrays.sort atau Collections.sort.
 */
public class UrutanService {

    public enum Arah {
        NAIK, TURUN
    }

    /** Hasil urutan beserta pencacah operasi untuk analisis kompleksitas. */
    public record HasilUrut(ArrayDinamis<Putusan> hasil, long perbandingan, long pergeseran) {
    }

    private final PutusanService service;

    public UrutanService(PutusanService service) {
        if (service == null) {
            throw new IllegalArgumentException("service tidak boleh null");
        }
        this.service = service;
    }

    /** Mengurutkan seluruh arsip berdasarkan vonis. */
    public HasilUrut urutkanVonis(Arah arah) {
        ArrayDinamis<Putusan> salinan = new ArrayDinamis<>();
        for (int i = 0; i < service.jumlah(); i++) {
            salinan.add(service.ambil(i));
        }
        return urutkanInPlace(salinan, arah);
    }

    /** Mengurutkan sebuah kumpulan putusan (mis. hasil filter); kumpulan asli tidak diubah. */
    public HasilUrut urutkan(ArrayDinamis<Putusan> data, Arah arah) {
        if (data == null) {
            throw new IllegalArgumentException("data tidak boleh null");
        }
        ArrayDinamis<Putusan> salinan = new ArrayDinamis<>();
        for (int i = 0; i < data.size(); i++) {
            salinan.add(data.get(i));
        }
        return urutkanInPlace(salinan, arah);
    }

    private HasilUrut urutkanInPlace(ArrayDinamis<Putusan> hasil, Arah arah) {
        if (arah == null) {
            throw new IllegalArgumentException("arah tidak boleh null");
        }
        long perbandingan = 0;
        long pergeseran = 0;
        for (int i = 1; i < hasil.size(); i++) {
            Putusan kunci = hasil.get(i);
            int j = i - 1;
            while (j >= 0) {
                perbandingan++;
                if (!harusSesudah(hasil.get(j), kunci, arah)) {
                    break;
                }
                hasil.set(j + 1, hasil.get(j));
                pergeseran++;
                j--;
            }
            hasil.set(j + 1, kunci);
        }
        return new HasilUrut(hasil, perbandingan, pergeseran);
    }

    /** Perbandingan ketat supaya elemen dengan vonis sama tidak saling melangkahi (stabil). */
    private boolean harusSesudah(Putusan a, Putusan b, Arah arah) {
        return arah == Arah.NAIK
                ? a.vonisBulan() > b.vonisBulan()
                : a.vonisBulan() < b.vonisBulan();
    }
}