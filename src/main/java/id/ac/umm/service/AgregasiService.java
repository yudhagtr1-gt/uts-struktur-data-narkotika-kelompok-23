package id.ac.umm.service;

import id.ac.umm.model.Putusan;
import id.ac.umm.strukturdata.ArrayDinamis;

/**
 * Agregasi: jumlah perkara, rata-rata vonis, total denda, distribusi jenis narkotika.
 * Aturan: vonis 0 ikut dihitung dalam rata-rata; jenis dikelompokkan sesuai teks asli,
 * jenis kosong masuk label "(kosong)". Tidak memakai koleksi dari java.util.
 */
public class AgregasiService {

    public static final String LABEL_KOSONG = "(kosong)";

    public record Distribusi(String jenis, int jumlah) {
    }

    public record Ringkasan(int jumlahPerkara, double rataRataVonis, long totalDenda,
                            ArrayDinamis<Distribusi> distribusi) {
    }

    private final PutusanService service;

    public AgregasiService(PutusanService service) {
        if (service == null) {
            throw new IllegalArgumentException("service tidak boleh null");
        }
        this.service = service;
    }

    /** Agregasi seluruh arsip. */
    public Ringkasan hitung() {
        Akumulator a = new Akumulator();
        for (int i = 0; i < service.jumlah(); i++) {
            a.tambah(service.ambil(i));
        }
        return a.hasil();
    }

    /** Agregasi sebuah kumpulan putusan, misalnya hasil FilterService. */
    public Ringkasan hitung(ArrayDinamis<Putusan> data) {
        if (data == null) {
            throw new IllegalArgumentException("data tidak boleh null");
        }
        Akumulator a = new Akumulator();
        for (int i = 0; i < data.size(); i++) {
            a.tambah(data.get(i));
        }
        return a.hasil();
    }

    private static final class Akumulator {
        private int jumlah;
        private long totalVonis;
        private long totalDenda;
        private final ArrayDinamis<Distribusi> distribusi = new ArrayDinamis<>();

        void tambah(Putusan p) {
            jumlah++;
            totalVonis += p.vonisBulan();
            totalDenda += p.dendaRupiah();
            String label = p.jenisNarkotika().isBlank() ? LABEL_KOSONG : p.jenisNarkotika();
            for (int i = 0; i < distribusi.size(); i++) {
                Distribusi d = distribusi.get(i);
                if (d.jenis().equals(label)) {
                    distribusi.set(i, new Distribusi(label, d.jumlah() + 1));
                    return;
                }
            }
            distribusi.add(new Distribusi(label, 1));
        }

        Ringkasan hasil() {
            double rata = jumlah == 0 ? 0.0 : (double) totalVonis / jumlah;
            return new Ringkasan(jumlah, rata, totalDenda, distribusi);
        }
    }
}