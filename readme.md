# MiniMIRC 
##### Aplikasi Chat IRC dengan RPC Menggunakan Apache Thrift

Gunakan aplikasi NetBeans untuk building file project NetBeans MiniIRC. Aplikasi ini menggunakan MongoDB sehingga sebelumnya harus terinstalasi MongoDB pada komputer untuk testing. Setelah terinstalasi MongoDB, cara menjalankan program adalah menjalankan terminal pada lokasi file ```.jar``` lalu menjalankan MongoDB perintah berikut;

```sh
$ mongod 
```

Berikutnya, menjalankan aplikasi dengan perintah-perintah berikut pada terminal yang berbeda dengan perintah berikut;
```sh
$ java -jar "mini_mirc_server.jar"
$ java -jar "mini_mirc_client.jar"
```

## Penggunaan Program
 
Program client akan secara otomatis men-generate username untuk pengguna. Apabila pengguna ingin menggunakan username lain, dapat dilakukan pergantian username dengan command berikut;
```sh
$ /NICK <nama_username_baru>
```
Apabila pengguna ingin berpartisipasi dalam channel, maka pengguna dapat memasuki ataupun membuat channel sendiri dengan perintah berikut (Channel yang tidak ada akan dibuat secara langsung);
```sh
$ /JOIN <nama_channel>
```
Untuk meninggalkan channel, pengguna dapat menggunakan perintah berikut;
```sh
$ /LEAVE <nama_channel>
```
Untuk mengirim pesan ke channel, pengguna dapat menggunakan perintah berikut;
```sh
$ @<nama_channel> <isi_msg>
```
Perintah-perintah yang dimasukkan secara default adalah isi pesan yang akan di-broadcast ke seluruh channel di mana akun pengguna terdaftar sebagai partisipan. 
```sh
$ <isi_msg>
```
Untuk keluar dari aplikasi, pengguna dapat memasukkan perintah sebagai berikut;
```sh
$ /EXIT
```


## Testing Program
#### 1. Generate username random, register, ganti username  
*Hasil testing*: Sukses.  
Username berhasil di-*generate* saat awal dijalankan client, dan berhasil melakukan registrasi pada server. Muncul pesan sukses pada kedua aplikasi. Username bisa diganti dan tidak terdapat pesan error saat registrasi pada runtime.  
*Cara testing*: Menjalankan program dengan urutan normal. Mengganti username dengan perintah: ```/NICK sudib```
#### 2. Join channel  
*Hasil testing*: Sukses.  
Channel yang sudah di-join tidak dapat dijoin lagi. Channel baru dapat dibuat, dan dapat join ke channel yang telah dibuat oleh user lain.  
*Cara testing*: Membuat channel baru dengan perintah ```/JOIN channelBaru``` dan menyalakan client kedua yang melakukan join ke channel yang sudah ada tersebut.
#### 3. Menerima dan mengirim pesan ke channel  
*Hasil testing*: Sukses.   
Pesan terkirim dan tersimpan pada *collection* inbox pada server. Pesan kemudian dikirim ke client, dan ditampilkan begitu client mengirim update ke server dengan menjalankan salah satu perintah apapun. Client yang tidak terdaftar ke channel target pengiriman tidak mendapat pesan 'nyasar'. Apabila mengirim ke channel yang belum di-join, muncul pesan kesalahan.   
*Cara testing*: Mengirim ke channel yang telah di-join dengan ```@channelBaru ini message baru``` dan meminta update dari client kedua yang ter-join di channel yang sama.
#### 4. Menerima dan mengirim pesan broadcast  
*Hasil testing*: Sukses.  
Pesan terkirim dan tersampaikan ke seluruh akun yang terdaftar dalam channel. Tidak terdapat pesan error.   
*Cara testing*: Mengirim ke seluruh channel yang telah di-join dengan penggunaan ```ini isi message broadcast``` dan client yang terdaftar di channel-channel sama mendapat message broadcast tersebut.
#### 5. Leave channel  
*Hasil testing*: Sukses.  
Pengguna dihapus dari daftar pengguna di channel. Pengiriman pesan baru tidak tersampaikan ke pengguna yang terhapus.   
*Cara testing*: Keluar dari channel yang telah di-join dengan ```@leave channelBaru```
#### 6. Exit   
*Hasil testing*: Sukses.  
Program menampilkan pesan sukses dan program client berhenti.   
*Cara testing*: Memasukkan perintah ```/EXIT```.