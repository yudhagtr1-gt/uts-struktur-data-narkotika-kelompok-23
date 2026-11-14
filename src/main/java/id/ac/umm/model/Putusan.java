package id.ac.umm.model;

import java.time.LocalDate;

/**
 * Satu putusan pengadilan (skema minimum bagian 2.2 panduan).
 * Nilai angka yang tidak ditemukan pada putusan ditulis 0.
 * beratBarangBuktiGram = jumlah berat netto seluruh narkotika;
 * barang non-narkotika tidak dihitung.
 */
public record Putusan(
        String caseId,
        String nomorPerkara,
        String pengadilan,
        LocalDate tanggalPutusan,
        String terdakwaKode,
        String jenisNarkotika,
        double beratBarangBuktiGram,
        String pasalDakwaan,
        String pasalTerbukti,
        int tuntutanBulan,
        int vonisBulan,
        long dendaRupiah,
        String barangBukti,
        String keadaanMemberatkan,
        String keadaanMeringankan,
        String sourcePages) {

    public Putusan {
        if (caseId == null || caseId.isBlank()) {
            throw new IllegalArgumentException("caseId wajib diisi");
        }
        if (nomorPerkara == null || nomorPerkara.isBlank()) {
            throw new IllegalArgumentException("nomorPerkara wajib diisi");
        }
        if (tanggalPutusan == null) {
            throw new IllegalArgumentException("tanggalPutusan wajib diisi");
        }
        if (beratBarangBuktiGram < 0 || Double.isNaN(beratBarangBuktiGram)) {
            throw new IllegalArgumentException("beratBarangBuktiGram tidak valid: " + beratBarangBuktiGram);
        }
        if (tuntutanBulan < 0) {
            throw new IllegalArgumentException("tuntutanBulan tidak boleh negatif: " + tuntutanBulan);
        }
        if (vonisBulan < 0) {
            throw new IllegalArgumentException("vonisBulan tidak boleh negatif: " + vonisBulan);
        }
        if (dendaRupiah < 0) {
            throw new IllegalArgumentException("dendaRupiah tidak boleh negatif: " + dendaRupiah);
        }
        pengadilan = teks(pengadilan);
        terdakwaKode = teks(terdakwaKode);
        jenisNarkotika = teks(jenisNarkotika);
        pasalDakwaan = teks(pasalDakwaan);
        pasalTerbukti = teks(pasalTerbukti);
        barangBukti = teks(barangBukti);
        keadaanMemberatkan = teks(keadaanMemberatkan);
        keadaanMeringankan = teks(keadaanMeringankan);
        sourcePages = teks(sourcePages);
    }

    private static String teks(String s) {
        return s == null ? "" : s.trim();
    }

    /** Ringkasan satu baris untuk fitur Tampil. */
    public String ringkasan() {
        return caseId + " | " + nomorPerkara + " | " + tanggalPutusan
                + " | " + jenisNarkotika + " | vonis " + vonisBulan + " bln";
    }

    /** Identitas putusan = caseId. */
    @Override
    public boolean equals(Object o) {
        return o instanceof Putusan p && caseId.equals(p.caseId);
    }

    @Override
    public int hashCode() {
        return caseId.hashCode();
    }
}
