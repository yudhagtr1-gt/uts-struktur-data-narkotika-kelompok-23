package id.ac.umm.service;

import id.ac.umm.model.Putusan;
import id.ac.umm.strukturdata.ArrayDinamis;

public class PutusanService {

    /** Hasil pencarian: posisi -1 berarti tidak ditemukan. */
    public record HasilCari(int posisi, int langkah, Putusan putusan) {
        public boolean ditemukan() {
            return posisi >= 0;
        }
    }

    private final ArrayDinamis<Putusan> arsip;

    public PutusanService(ArrayDinamis<Putusan> arsip) {
        if (arsip == null) {
            throw new IllegalArgumentException("arsip tidak boleh null");
        }
        this.arsip = arsip;
    }

    public int jumlah() {
        return arsip.size();
    }

    public Putusan ambil(int posisi) {
        return arsip.get(posisi);
    }

    public boolean valid() {
        return arsip.validateInvariant();
    }

    public String ringkasan(int posisi) {
        return "[" + posisi + "] " + arsip.get(posisi).ringkasan();
    }

    /** Semua putusan dalam urutan struktur utama. */
    public String tampil() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arsip.size(); i++) {
            sb.append(ringkasan(i)).append('\n');
        }
        return sb.toString();
    }

    /** Ditolak bila null atau caseId sudah ada; struktur tidak berubah saat ditolak. */
    public void tambah(Putusan p) {
        if (p == null) {
            throw new IllegalArgumentException("putusan tidak boleh null");
        }
        if (cari(p.caseId()).ditemukan()) {
            throw new IllegalArgumentException("caseId sudah ada: " + p.caseId());
        }
        arsip.add(p);
    }

    /** Pencarian linear by caseId atau nomorPerkara. Langkah = jumlah elemen yang dibandingkan. */
    public HasilCari cari(String kunci) {
        int langkah = 0;
        if (kunci != null) {
            for (int i = 0; i < arsip.size(); i++) {
                langkah++;
                Putusan p = arsip.get(i);
                if (kunci.equals(p.caseId()) || kunci.equals(p.nomorPerkara())) {
                    return new HasilCari(i, langkah, p);
                }
            }
        }
        return new HasilCari(-1, langkah, null);
    }

    public Putusan hapusPosisi(int posisi) {
        return arsip.remove(posisi);
    }

    /** Menghapus by kunci; posisi -1 pada hasil bila tidak ada. */
    public HasilCari hapusKunci(String kunci) {
        HasilCari hasil = cari(kunci);
        if (hasil.ditemukan()) {
            arsip.remove(hasil.posisi());
        }
        return hasil;
    }
}