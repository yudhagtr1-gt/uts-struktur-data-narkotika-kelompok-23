# ADR 0003: Seleksi 50 putusan dari 63 putusan terkumpul

- Tanggal: 15 November 2026
- Status: diterima (draf; nilai kunci perlu diverifikasi manual terhadap PDF)
- Terkait: Issue #11

## Konteks

Terkumpul 63 putusan Pengadilan Negeri Kendal di `data/putusan-all-clean.csv`. Panduan UTS meminta tepat 50 record valid, nilai yang tidak ditemukan ditulis kosong (0 atau null) dan tidak diperkirakan, serta nama terdakwa disamarkan dengan kode.

## Keputusan

`data/putusan-50.csv` berisi 63 putusan dikurangi 13 baris yang disisihkan dengan aturan berikut, berurutan:

1. Sisihkan baris yang `vonisBulan`-nya kosong di CSV padahal tertulis di amar putusan (6 baris).
2. Sisihkan baris yang `vonisBulan`-nya di CSV berbeda dengan amar putusan (5 baris).
3. Sisihkan baris yang berisi kalimat pengganti, bukan sel kosong (1 baris tambahan: P010; P020 sudah masuk aturan 1).
4. Dari baris yang tersisa, sisihkan 1 baris dengan kolom kosong terbanyak. Bila seri, caseId yang lebih besar disisihkan lebih dulu (P012).

Baris yang lolos aturan di atas tidak diubah. Baris dipertahankan apa adanya, termasuk dua putusan dengan dua terdakwa (P024 dan P030, vonis dan denda sama untuk kedua terdakwa) dan satu putusan dengan jenis campuran (P056).

## Baris yang disisihkan

| caseId | nomorPerkara | Alasan | Vonis menurut amar PDF |
|---|---|---|---|
| P010 | 83/Pid.Sus/2021/PN Kdl | Berisi kalimat pengganti (bukan kosong) di keadaanMeringankan | - |
| P012 | 85/Pid.Sus/2025/PN Kdl | Kolom kosong terbanyak (5) di antara baris tersisa; seri dengan P011, caseId lebih besar disisihkan | - |
| P013 | 86/Pid.Sus/2021/PN Kdl | vonisBulan kosong di CSV, ada di amar | 90 bln (hal. 38) |
| P015 | 87/Pid.Sus/2025/PN Kdl | vonisBulan di CSV berbeda dengan amar | 120 bln (hal. 37) |
| P016 | 87/Pid.Sus/2026/PN Kdl | vonisBulan kosong di CSV, ada di amar | 66 bln (hal. 34) |
| P018 | 88/Pid.Sus/2025/PN Kdl | vonisBulan kosong di CSV, ada di amar | 96 bln (hal. 34) |
| P019 | 91/Pid.Sus/2021/PN Kdl | vonisBulan kosong di CSV, ada di amar | 24 bln (hal. 33) |
| P020 | 93/Pid.Sus/2021/PN Kdl | vonisBulan kosong di CSV, ada di amar; pasalTerbukti berisi kalimat pengganti | 12 bln (hal. 38) |
| P041 | 142/Pid.Sus/2025/PN Kdl | vonisBulan kosong di CSV, ada di amar | 108 bln (hal. 36) |
| P042 | 147/Pid.Sus/2025/PN Kdl | vonisBulan di CSV berbeda dengan amar | 96 bln (hal. 47) |
| P043 | 150/Pid.Sus/2024/PN Kdl | vonisBulan di CSV berbeda dengan amar | 12 bln (hal. 30) |
| P050 | 184/Pid.Sus/2025/PN Kdl | vonisBulan di CSV berbeda dengan amar | 36 bln (hal. 24) |
| P054 | 197/Pid.Sus/2025/PN Kdl | vonisBulan di CSV berbeda dengan amar | 54 bln (hal. 28) |

## Konsekuensi

- Pencocokan vonis dengan amar dilakukan otomatis dari teks PDF, sehingga hanya berfungsi sebagai penunjuk. Setiap nilai yang dipakai harus dibuka dan dicocokkan sendiri di halaman PDF-nya.
- Seluruh putusan berasal dari satu pengadilan (PN Kendal). Kesesuaian dengan ketentuan "pengadilan negeri yang berbeda" menunggu konfirmasi dosen pengampu.
- Bantuan AI: membandingkan CSV dengan teks PDF dan menyusun daftar seleksi. Keputusan dan verifikasi akhir dilakukan oleh anggota kelompok (dicatat di AI_USAGE.md).
