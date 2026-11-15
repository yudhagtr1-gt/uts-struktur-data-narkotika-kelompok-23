# uts-struktur-data-narkotika-kelompok-23
# Arsip Putusan Narkotika

Aplikasi berbasis Java untuk mengelola arsip putusan perkara narkotika. Program menyediakan fitur impor dan ekspor CSV, pengelolaan data putusan, pencarian, filter, agregasi, pengurutan, serta pencatatan riwayat aktivitas.

## Identitas

| Keterangan | Informasi |
|---|---|
| Pengerjaan | Individu |
| Nama | Muhammad Rizki Adji Yudha |

## Deskripsi Proyek

Proyek ini mengolah data putusan pengadilan terkait perkara narkotika. Data dibaca dari file CSV, disimpan menggunakan struktur data yang diimplementasikan sendiri, lalu dikelola melalui aplikasi berbasis terminal.

Setiap putusan memiliki identitas `caseId` dan memuat informasi seperti nomor perkara, pengadilan, tanggal putusan, jenis narkotika, berat barang bukti, pasal, tuntutan, vonis, denda, serta referensi halaman sumber.

## Fitur Aplikasi

1. **Impor CSV** — membaca data putusan dengan skema 16 kolom.
2. **Tampil semua** — menampilkan seluruh putusan.
3. **Tambah putusan** — menambahkan data putusan baru.
4. **Hapus putusan** — menghapus berdasarkan posisi, `caseId`, atau nomor perkara.
5. **Pencarian** — mencari berdasarkan `caseId` atau nomor perkara menggunakan Linear Search.
6. **Filter** — menyaring berdasarkan jenis narkotika, berat barang bukti, pasal terbukti, dan rentang vonis.
7. **Agregasi** — menghitung jumlah perkara, rata-rata vonis, total denda, dan distribusi jenis narkotika.
8. **Pengurutan** — mengurutkan putusan berdasarkan vonis menggunakan Insertion Sort.
9. **Ekspor** — menyimpan arsip atau hasil filter ke CSV.
10. **Riwayat audit** — menampilkan riwayat aktivitas menggunakan Stack.

## Struktur Data dan Algoritma

| Komponen | Penggunaan |
|---|---|
| StrukturData<T> | Interface operasi dasar struktur data. |
| ArrayDinamis<T> | Menyimpan arsip putusan. |
| Stack<T> | Menyimpan riwayat aktivitas dengan prinsip LIFO. |
| Linear Search | Mencari putusan berdasarkan identitas. |
| Insertion Sort | Mengurutkan putusan berdasarkan vonis. |

## Teknologi

- Java JDK 17
- Apache Maven
- JUnit Jupiter 5.10.2
- IntelliJ IDEA
- Git dan GitHub
- CSV

## Struktur Folder

```text
project/
├── data/
│   └── putusan-50.csv
├── docs/
│   └── data-provenance.csv
├── src/
│   ├── main/
│   │   └── java/
│   │       └── id/ac/umm/
│   │           ├── Main.java
│   │           ├── io/
│   │           ├── model/
│   │           ├── service/
│   │           └── strukturdata/
│   └── test/
│       └── java/
│           └── id/ac/umm/
├── .gitignore
├── AI_USAGE.md
├── CONTRIBUTIONS.md
├── pom.xml
└── README.md
```

## Dataset

Dataset utama berada di:

```text
data/putusan-50.csv
```

Dataset menggunakan 16 kolom:

```csv
caseId,nomorPerkara,pengadilan,tanggalPutusan,terdakwaKode,jenisNarkotika,beratBarangBuktiGram,pasalDakwaan,pasalTerbukti,tuntutanBulan,vonisBulan,dendaRupiah,barangBukti,keadaanMemberatkan,keadaanMeringankan,sourcePages
```

Tanggal putusan menggunakan format `yyyy-MM-dd`.

Dokumentasi provenance berada di:

```text
docs/data-provenance.csv
```

File provenance mencatat asal-usul data, halaman sumber, extractor, reviewer, dan status verifikasi.

## Persyaratan Sistem

- JDK 17 atau lebih baru.
- Apache Maven.
- IntelliJ IDEA.
- File dataset `data/putusan-50.csv`.
- File provenance untuk pengujian dataset.

## Cara Build

### Melalui IntelliJ IDEA

1. Buka project.
2. Pastikan Project SDK menggunakan JDK 17.
3. Buka `pom.xml`.
4. Pilih **Load Maven Project** jika diminta.
5. Buka panel Maven.
6. Jalankan lifecycle `clean`.
7. Jalankan lifecycle `compile`.

### Melalui Terminal

Jalankan dari direktori utama project:

```bash
mvn clean compile
```

Untuk membuat paket:

```bash
mvn clean package
```

## Cara Test

Jalankan seluruh pengujian menggunakan:

```bash
mvn test
```

Pengujian mencakup:

- PutusanTest
- ArrayDinamisTest
- StackTest
- CsvImporterTest
- CsvExporterTest
- PutusanServiceTest
- FilterServiceTest
- AgregasiServiceTest
- UrutanServiceTest
- AuditServiceTest
- DatasetProvenanceTest
- SkenarioUjiTest
- StackSkenarioTest
- TraceManualTest

## Cara Menjalankan Aplikasi

1. Pastikan dataset tersedia di `data/putusan-50.csv`.
2. Buka `Main.java`.
3. Klik tombol Run di samping method `main`.
4. Program akan berjalan melalui terminal IntelliJ.

Menu aplikasi:

```text
=== Arsip Putusan Narkotika ===
1. Impor CSV
2. Tampil semua
3. Tambah putusan
4. Hapus putusan
5. Cari (caseId / nomor perkara)
6. Filter
7. Agregasi
8. Urutkan berdasarkan vonis
9. Ekspor (CSV / laporan)
10. Riwayat audit
0. Keluar
```

## Cara Mengimpor Dataset

1. Jalankan aplikasi.
2. Pilih menu `1. Impor CSV`.
3. Ketika diminta memasukkan path CSV, gunakan:

```text
data/putusan-50.csv
```

4. Tekan Enter.
5. Periksa jumlah baris, data valid, data ditolak, dan status invariant.

Program tidak otomatis mengimpor dataset saat pertama kali dijalankan.

## Contoh Alur Penggunaan

1. Impor dataset.
2. Tampilkan seluruh putusan.
3. Cari putusan berdasarkan `caseId`.
4. Terapkan filter.
5. Lihat hasil agregasi.
6. Urutkan berdasarkan vonis.
7. Ekspor hasil.
8. Periksa riwayat audit.

## Validasi Data

- `caseId` dan `nomorPerkara` wajib diisi.
- Tanggal putusan harus valid.
- Berat barang bukti, tuntutan, vonis, dan denda tidak boleh negatif.
- `caseId` tidak boleh duplikat.
- CSV harus memiliki 16 kolom sesuai skema.
- Baris tidak valid ditolak dan dicatat sebagai galat.
- Struktur data diperiksa menggunakan invariant.

## Catatan

- File ekspor disimpan pada lokasi yang dipilih pengguna.
- Direktori tujuan ekspor dibuat otomatis jika belum tersedia.
- Hasil filter dapat digunakan kembali untuk agregasi dan ekspor selama arsip belum berubah.
- Jalankan `mvn test` setelah melakukan perubahan kode.
