# Penggunaan AI

Alat: Claude (Anthropic), melalui antarmuka chat. Anggota: Muhammad Rizki Adji Yudha.
Seluruh saran AI diuji (`mvn test`, uji manual lewat menu `Main`) dan dijalankan sendiri
sebelum di-commit. Data hukum tidak dimasukkan dari keluaran AI tanpa verifikasi ke PDF.

| Tanggal | Tujuan | Ringkasan prompt | Bagian yang dipakai | Perubahan saya | Verifikasi |
|---|---|---|---|---|---|
| 2026-11-14 | Issue #6: PutusanService dan Main | meminta rancangan service tampil/tambah/hapus/cari dan menu | kode PutusanService, Main, test | menyesuaikan dengan ArrayDinamis dan Putusan, menjalankan sendiri | 7 test PutusanService, uji manual menu |
| 2026-11-14 | Issue #7: FilterService dan AgregasiService | meminta filter gabungan dan agregasi | kode kedua service dan test | menetapkan aturan agregasi (vonis 0 dihitung, jenis kosong ke "(kosong)") | 9 + 7 test, uji manual menu |
| 2026-11-14 | Issue #8: UrutanService dan menu | meminta insertion sort stabil dan menu 6-8 | kode UrutanService, Main | memeriksa arsip utama tidak berubah | 7 test, uji manual |
| 2026-11-14 | Issue #9: audit dan ekspor | meminta menu ekspor dan riwayat | menu 9 dan 10 di Main | menjalankan dan memperbaiki path ekspor | uji manual, test AuditService dan CsvExporter |
| 2026-11-15 | Issue #10: test skenario dan Stack | meminta SkenarioUjiTest, StackSkenarioTest | kedua file test | menjalankan, menyesuaikan | 82 lalu 91 test lolos |
| 2026-11-15 | Issue #10: trace manual | meminta draf trace-manual.md dan TraceManualTest | dokumen dan test, kalimat dituliskan ulang | membaca dan mencocokkan dengan kode | 93 test lolos |
| 2026-11-15 | Issue #11: pemeriksaan data | meminta audit CSV dan pencocokan vonis, denda, dan halaman dengan teks PDF | daftar selisih dan draf halaman | memeriksa ke PDF dan memutuskan koreksi sendiri | cek manual ke PDF |
| 2026-11-15 | Issue #12: ADR dan README | meminta draf ADR 0001, 0002, dan README | ketiga dokumen | membaca dan menyesuaikan dengan keputusan sebenarnya | dibandingkan dengan kode dan test |

ISI_BARIS_ISSUE_1_SAMPAI_5: tambahkan baris untuk Issue #1-#5 sesuai penggunaan AI yang sebenarnya
(atau tulis "tidak memakai AI" bila memang tidak).