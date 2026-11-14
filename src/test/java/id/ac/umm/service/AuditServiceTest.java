package id.ac.umm.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuditServiceTest {

    @Test
    void mencatatDenganNomorBerurutan() {
        AuditService a = new AuditService();
        assertEquals(1, a.catat("TAMBAH", "P001").nomor());
        assertEquals(2, a.catat("HAPUS", "P002").nomor());
        assertEquals(2, a.jumlah());
        assertTrue(a.valid());
    }

    @Test
    void terbaruDiPuncak() {
        AuditService a = new AuditService();
        a.catat("TAMBAH", "P001");
        a.catat("CARI", "P002");
        assertEquals("CARI", a.terakhir().aksi());
        assertEquals("CARI", a.ambil(0).aksi());
        assertEquals("TAMBAH", a.ambil(1).aksi());
    }

    @Test
    void tampilkanDariTerbaruTanpaMengubahIsi() {
        AuditService a = new AuditService();
        a.catat("TAMBAH", "P001");
        a.catat("HAPUS", "P001");
        String hasil = a.tampilkan();
        assertTrue(hasil.indexOf("HAPUS") < hasil.indexOf("TAMBAH"));
        assertEquals(2, a.jumlah());
        assertEquals("HAPUS", a.terakhir().aksi());
        assertTrue(a.valid());
    }

    @Test
    void riwayatKosongAman() {
        AuditService a = new AuditService();
        assertTrue(a.kosong());
        assertEquals("(riwayat kosong)\n", a.tampilkan());
        assertThrows(IllegalStateException.class, a::terakhir);
    }

    @Test
    void aksiTidakValidDitolakDanStackTetap() {
        AuditService a = new AuditService();
        a.catat("TAMBAH", "P001");
        assertThrows(IllegalArgumentException.class, () -> a.catat(" ", "x"));
        assertThrows(IllegalArgumentException.class, () -> a.catat(null, "x"));
        assertEquals(1, a.jumlah());
        assertEquals(2, a.catat("CARI", "").nomor());
        assertTrue(a.valid());
    }

    @Test
    void hitungPerAksi() {
        AuditService a = new AuditService();
        a.catat("CARI", "a");
        a.catat("TAMBAH", "b");
        a.catat("cari", "c");
        assertEquals(2, a.hitung("CARI"));
        assertEquals(0, a.hitung("EKSPOR"));
    }

    @Test
    void bersihkanMengosongkanDanMengulangNomor() {
        AuditService a = new AuditService();
        a.catat("TAMBAH", "P001");
        a.bersihkan();
        assertTrue(a.kosong());
        assertEquals(1, a.catat("TAMBAH", "P002").nomor());
        assertTrue(a.valid());
    }

    @Test
    void banyakCatatanMelewatiKapasitasAwal() {
        AuditService a = new AuditService();
        for (int i = 0; i < 25; i++) {
            a.catat("AKSI", "n" + i);
        }
        assertEquals(25, a.jumlah());
        assertEquals("n24", a.ambil(0).detail());
        assertEquals("n0", a.ambil(24).detail());
        assertTrue(a.valid());
    }
}
