# ADR 0002: Strategi pencarian dan pengurutan

Status: Diterima
Tanggal: 2026-11-15

## Konteks

Aplikasi harus mencari putusan berdasarkan `caseId` atau nomor perkara dan melaporkan jumlah
langkah (panduan bagian 4), serta menghasilkan tampilan terurut berdasarkan vonis tanpa merusak
invariant struktur utama. `Collections.sort` dan `Arrays.sort` tidak boleh dipakai.

## Keputusan

Pencarian:

- Pencarian linear. `PutusanService.cari(kunci)` membandingkan kunci dengan `caseId` atau
  `nomorPerkara` setiap elemen dari indeks 0 dan mengembalikan posisi, jumlah langkah (jumlah
  elemen yang dibandingkan), dan putusan.
- Jumlah langkah dihitung di service, tidak bergantung pada `langkahTerakhir` di `ArrayDinamis`.
- Terbaik 1 langkah, terburuk n langkah, dan n juga bila tidak ditemukan.

Pengurutan:

- Insertion sort stabil pada salinan arsip, berdasarkan `vonisBulan`, naik atau turun
  (`UrutanService`). Arsip utama tidak diubah, sehingga urutan impor tetap terjaga.
- Pencacah perbandingan dan pergeseran dikembalikan bersama hasil, sebagai bukti analisis.
- O(n) terbaik (data sudah terurut), O(n^2) terburuk. Stabil: putusan dengan vonis sama
  mempertahankan urutan semula.

Alasan (tulis ulang dengan kata sendiri):

- Pada 50 data, linear search dan insertion sort cukup cepat dan mudah dibuktikan lewat trace manual.
- Pengurutan pada salinan menjaga "tampilkan sesuai urutan struktur utama" tetap benar.

## Alternatif yang dipertimbangkan

| Alternatif | Alasan tidak dipilih |
|---|---|
| Binary search | Butuh data terurut berdasarkan kunci pencarian. Mengurutkan arsip utama mengubah urutan yang harus ditampilkan sesuai struktur utama. |
| Merge sort | O(n log n), tetapi butuh buffer tambahan dan lebih rumit. Pada 50 data selisih waktunya tidak berarti. |
| Mengurutkan arsip utama langsung | Merusak urutan struktur utama dan menyulitkan pembuktian invariant. |

## Konsekuensi

- Pencarian tidak mengubah struktur dan tidak membutuhkan data terurut.
- Kesalahan yang sudah ditemukan: `ArrayDinamis.contains()` awalnya memanggil `search()` dan menimpa
  `langkahTerakhir` (Issue #15). Diperbaiki dengan loop sendiri dan dicegah oleh test regresi
  `containsTidakMengubahLangkahTerakhir`.
- Jika data tumbuh jauh di atas 50, merge sort atau indeks pencarian layak ditinjau ulang.