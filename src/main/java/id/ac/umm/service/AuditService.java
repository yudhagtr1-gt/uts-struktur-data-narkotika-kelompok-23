package id.ac.umm.service;

import id.ac.umm.strukturdata.Stack;

/**
 * Merekam operasi pengguna ke struktur sekunder (Stack).
 * Catatan terbaru ada di puncak. Menampilkan riwayat tidak mengubah isi stack.
 */
public class AuditService {

    /** Satu catatan audit. nomor naik satu per catatan. */
    public record Catatan(int nomor, String aksi, String detail) {

        public Catatan {
            if (aksi == null || aksi.isBlank()) {
                throw new IllegalArgumentException("aksi wajib diisi");
            }
            aksi = aksi.trim();
            detail = detail == null ? "" : detail.trim();
        }

        public String ringkas() {
            return "#" + nomor + " " + aksi + (detail.isEmpty() ? "" : ": " + detail);
        }
    }

    private final Stack<Catatan> riwayat = new Stack<>();
    private int nomorBerikut = 1;

    /** Mencatat operasi. Jika aksi tidak valid, stack tidak berubah. */
    public Catatan catat(String aksi, String detail) {
        Catatan c = new Catatan(nomorBerikut, aksi, detail);
        riwayat.push(c);
        nomorBerikut++;
        return c;
    }

    public int jumlah() {
        return riwayat.size();
    }

    public boolean kosong() {
        return riwayat.isEmpty();
    }

    /** Catatan terbaru (puncak). */
    public Catatan terakhir() {
        return riwayat.peek();
    }

    /** Posisi 0 = terbaru. */
    public Catatan ambil(int posisi) {
        return riwayat.get(posisi);
    }

    /** Jumlah catatan dengan aksi tertentu. */
    public int hitung(String aksi) {
        int n = 0;
        for (int i = 0; i < riwayat.size(); i++) {
            if (riwayat.get(i).aksi().equalsIgnoreCase(aksi)) {
                n++;
            }
        }
        return n;
    }

    /** Riwayat dari yang terbaru, satu catatan per baris. */
    public String tampilkan() {
        if (riwayat.isEmpty()) {
            return "(riwayat kosong)\n";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < riwayat.size(); i++) {
            sb.append(riwayat.get(i).ringkas()).append('\n');
        }
        return sb.toString();
    }

    public void bersihkan() {
        riwayat.clear();
        nomorBerikut = 1;
    }

    public boolean valid() {
        return riwayat.validateInvariant();
    }
}
