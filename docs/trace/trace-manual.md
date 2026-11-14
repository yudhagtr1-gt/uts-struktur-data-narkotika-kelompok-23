# Trace Manual: ArrayDinamis dan Stack

Dokumen ini menelusuri apa yang terjadi di dalam array dan stack, langkah demi langkah.
Tanda `_` artinya slot masih kosong (null). Semua angka di sini dicek oleh `TraceManualTest`,
jadi kalau kodenya berubah dan trace ini tidak cocok lagi, test akan gagal.

## 1. ArrayDinamis: add dan resize (kapasitas awal 3)

Kapasitas awal sengaja dibuat kecil (3) supaya resize cepat terlihat. Tiga `add` pertama
masih muat. Pada `add(D)` array sudah penuh, jadi dibuat array baru berkapasitas dua kali
lipat (6), tiga elemen lama disalin ke sana, baru D ditaruh di slot berikutnya.

| Langkah | Isi array | size | kapasitas | Keterangan |
|---|---|---|---|---|
| awal | [_,_,_] | 0 | 3 | |
| add(A) | [A,_,_] | 1 | 3 | masih ada slot, langsung isi data[0] |
| add(B) | [A,B,_] | 2 | 3 | |
| add(C) | [A,B,C] | 3 | 3 | array sekarang penuh |
| add(D) | [A,B,C,D,_,_] | 4 | 6 | penuh, jadi resize dulu: array baru 6 slot, 3 elemen disalin, lalu D diisi |
| add(E) | [A,B,C,D,E,_] | 5 | 6 | |

Resize memang mahal karena menyalin semua elemen, tapi jarang terjadi sebab kapasitas
selalu digandakan. Itu sebabnya `add` dihitung O(1) rata-rata.

## 2. ArrayDinamis: remove dan pergeseran

Waktu satu elemen dihapus, ada lubang yang harus ditutup. Caranya, semua elemen sesudahnya
digeser satu slot ke kiri, lalu slot paling belakang dikosongkan dan size dikurangi satu.
Jadi makin depan posisi yang dihapus, makin banyak geserannya. Jumlah pergeseran =
size - 1 - index.

### remove(1) pada [A,B,C,D,E,_] (menghapus B)

| i | Operasi | Isi array |
|---|---|---|
| awal | | [A,B,C,D,E,_] |
| 1 | data[1] = data[2] | [A,C,C,D,E,_] |
| 2 | data[2] = data[3] | [A,C,D,D,E,_] |
| 3 | data[3] = data[4] | [A,C,D,E,E,_] |
| akhir | data[4] = null, size = 4 | [A,C,D,E,_,_] |

B dikembalikan ke pemanggil. Ada 3 pergeseran.

### remove(0) pada [A,C,D,E,_,_] (menghapus A, elemen pertama)

| i | Operasi | Isi array |
|---|---|---|
| 0 | data[0] = data[1] | [C,C,D,E,_,_] |
| 1 | data[1] = data[2] | [C,D,D,E,_,_] |
| 2 | data[2] = data[3] | [C,D,E,E,_,_] |
| akhir | data[3] = null, size = 3 | [C,D,E,_,_,_] |

Menghapus elemen pertama adalah kasus terburuk, karena semua elemen sisanya harus bergeser
(di sini 3 pergeseran).

### remove(2) pada [C,D,E,_,_,_] (menghapus E, elemen terakhir)

Di sini tidak ada yang perlu digeser, karena E sudah di ujung. Perulangan pergeseran tidak
berjalan (i = 2 tidak kurang dari size - 1 = 2). Yang terjadi hanya data[2] = null dan
size jadi 2, hasilnya [C,D,_,_,_,_]. Ini kasus terbaik, 0 pergeseran.

## 3. ArrayDinamis: insert

`insert` kebalikan dari `remove`: elemen digeser ke kanan untuk membuka tempat. Pergeseran
dimulai dari belakang supaya elemen yang belum sempat dipindah tidak tertimpa.

insert(1, X) pada [C,D,_,_,_,_] dengan size 2 (index 1 valid karena berada di 0 sampai size):

| i | Operasi | Isi array |
|---|---|---|
| awal | | [C,D,_,_,_,_] |
| 2 | data[2] = data[1] | [C,D,D,_,_,_] |
| tulis | data[1] = X, size = 3 | [C,X,D,_,_,_] |

## 4. ArrayDinamis: search (pencarian linear)

Pencarian memeriksa elemen satu per satu dari depan dan berhenti begitu ketemu. Tiap
perbandingan dihitung sebagai satu langkah.

| Panggilan | i = 0 | i = 1 | i = 2 | Hasil | langkahTerakhir |
|---|---|---|---|---|---|
| search(D) pada [C,X,D] | C != D (langkah 1) | X != D (langkah 2) | D == D (langkah 3) | 2 | 3 |
| search(Z) pada [C,X,D] | C != Z (langkah 1) | X != Z (langkah 2) | D != Z (langkah 3) | -1 | 3 |

Kalau elemennya ketemu, jumlah langkahnya posisi + 1. Kalau tidak ada, seluruh isi
diperiksa, jadi n langkah.

## 5. Stack: searchPreserve

Stack hanya bisa diakses dari puncak. Supaya bisa mencari tanpa merusak urutan, elemen
dipindah satu per satu dari puncak ke penyangga sampai ketemu. Setelah itu `restore()`
mengembalikan semuanya dari penyangga, dari yang terakhir masuk ke yang pertama masuk,
sehingga urutan stack persis seperti semula.

Stack setelah push 1, 2, 3, 4, 5: data = [1,2,3,4,5], size 5, puncak = 5.
Yang dicari: searchPreserve(2).

| Iterasi | langkah | Elemen dipindah | Isi data (size) | Isi penyangga | Cocok dengan 2? |
|---|---|---|---|---|---|
| 1 | 1 | 5 | [1,2,3,4] (4) | [5] | tidak |
| 2 | 2 | 4 | [1,2,3] (3) | [5,4] | tidak |
| 3 | 3 | 3 | [1,2] (2) | [5,4,3] | tidak |
| 4 | 4 | 2 | [1] (1) | [5,4,3,2] | ya, hasil = 4 - 1 = 3, berhenti |

Lalu `restore()` mengembalikan elemennya:

| Pengembalian | Elemen | Isi data (size) |
|---|---|---|
| 1 | 2 | [1,2] (2) |
| 2 | 3 | [1,2,3] (3) |
| 3 | 4 | [1,2,3,4] (4) |
| 4 | 5 | [1,2,3,4,5] (5) |

Hasilnya: elemen 2 ada di kedalaman 3 dari puncak, dicari dalam 4 langkah, urutan stack
kembali seperti awal, dan penyangga kosong lagi.

## 6. Ringkasan kompleksitas

| Operasi | Waktu | Catatan |
|---|---|---|
| ArrayDinamis add | O(1) rata-rata | O(n) saat resize, tapi jarang karena kapasitas digandakan |
| ArrayDinamis get dan set | O(1) | akses langsung lewat indeks |
| ArrayDinamis insert | O(n) | menggeser size - index elemen |
| ArrayDinamis remove | O(n) | menggeser size - 1 - index elemen |
| ArrayDinamis search dan contains | O(n) | langkah = posisi + 1, atau n kalau tidak ada |
| Stack push, pop, peek | O(1) | push jadi O(n) hanya saat resize |
| Stack searchPreserve | O(n) | butuh memori tambahan O(n) untuk penyangga |