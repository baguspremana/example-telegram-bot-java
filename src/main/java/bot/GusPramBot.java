package bot;

import helper.*;
import model.*;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.List;

public class GusPramBot extends TelegramLongPollingBot {

    public void onUpdateReceived(Update update) {

        /*===============================================================================================
                                            INISIALISASI
        ===============================================================================================*/
        String command = update.getMessage().getText();
        command = command.toLowerCase();
        Long chat_id = update.getMessage().getChatId();
        Outbox outbox = new Outbox(chat_id);
        Inbox inbox = new Inbox(chat_id, command);
        MessageBotDAO messageBotDAO = new MessageBotDAO();
        PhotoBotDAO photoBotDAO = new PhotoBotDAO();
        LastMenuHelper lastMenuHelper = new LastMenuHelper();
        FileHelper fileHelper = new FileHelper();
        MahasiswaHelper mahasiswaHelper = new MahasiswaHelper();
        List<Mahasiswa> mahasiswaList;
        String separator = "#";
        SendMessage message = new SendMessage();
        String[] arrExplode = explodeStringUsingCoreJava(command, separator);
        LastMenu lastMenu = new LastMenu();
        String menuTerakhir;

        /*===============================================================================================
                                          SELECT LAST MENU INPUT BY USER
        ===============================================================================================*/
        lastMenuHelper.selectLastMenu(chat_id, lastMenu);
        menuTerakhir = lastMenu.getLast_menu();

        /*===============================================================================================
                                            PRINT HASIL EXPLODE PESAN
        ===============================================================================================*/
        for (int i=0; i<arrExplode.length; i++){
            System.out.println(i + "-" +arrExplode[i]);
        }

        /*===============================================================================================
                                                CHECK USER INPUT
        =================================================================================================*/
        if (command.equals("1")||command.equals("2")||command.equals("3")||command.equals("4")
            ||command.equals("5")||command.equals("6")||command.equals("7")||command.equals("8")||
            command.equals("9")){
            messageBotDAO.getFormat(inbox, outbox);
            System.out.println(outbox.getMessage());
            message.setText(outbox.getMessage());

            message.setChatId(update.getMessage().getChatId());
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            lastMenuHelper.insertLastMenu(chat_id, command, lastMenu);

            /*===============================================================================================
                                                CHECK LAST MENU USER
            =================================================================================================*/
        }else if (menuTerakhir.equals("1")||menuTerakhir.equals("2")||menuTerakhir.equals("3")||menuTerakhir.equals("4")||
                menuTerakhir.equals("5")||menuTerakhir.equals("6")||menuTerakhir.equals("7")||menuTerakhir.equals("8")||
                menuTerakhir.equals("9")){
            if (arrExplode[0].equals("cari mahasiswa")||arrExplode[0].equals("lihat semua mahasiswa")
                    ||arrExplode[0].equals("cari dosen")||arrExplode[0].equals("lihat semua dosen")){
                if (menuTerakhir.equals("1") && arrExplode[0].equals("cari mahasiswa")){
                    messageBotDAO.getSQL(arrExplode[0], arrExplode[1], arrExplode[2], outbox, chat_id, command);
                }else if (menuTerakhir.equals("3") && arrExplode[0].equals("cari dosen")){
                    messageBotDAO.getSQL(arrExplode[0], arrExplode[1], arrExplode[2], outbox, chat_id, command);
                }else if (menuTerakhir.equals("2") && arrExplode[0].equals("lihat semua mahasiswa")){
                    messageBotDAO.getSQL(arrExplode[0], null, null, outbox, chat_id, command);
                }else if(menuTerakhir.equals("4") && arrExplode[0].equals("lihat semua dosen")){
                    messageBotDAO.getSQL(arrExplode[0], null, null, outbox, chat_id, command);
                }else {
                    messageBotDAO.selectMenu(outbox);
                    System.out.println(outbox.getMessage());
                    message.setText(outbox.getMessage());
                }

                messageBotDAO.getData(inbox, outbox);
                System.out.println(outbox.getMessage());
                message.setText(outbox.getMessage());

                message.setChatId(update.getMessage().getChatId());
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                messageBotDAO.updateSQL(inbox);

            }else if (menuTerakhir.equals("5") && arrExplode[0].equals("lihat kegiatan")){
                photoBotDAO.getListKegiatan(arrExplode[1], outbox);
                lastMenuHelper.insertLastMenu(chat_id, arrExplode[0], lastMenu);
                message.setText(outbox.getMessage());

                message.setChatId(update.getMessage().getChatId());
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }else if (menuTerakhir.equals("6") && arrExplode[0].equals("lihat mahasiswa file")){
                fileHelper.getSQL(arrExplode[0], chat_id, outbox);
                String query = outbox.getMessage();
                System.out.println(query);
                mahasiswaList = fileHelper.getDataMahasiswa(query);
                for (Mahasiswa mahasiswa : mahasiswaList){
                    System.out.println(mahasiswa.getId());
                    System.out.println(mahasiswa.getNim());
                    System.out.println(mahasiswa.getNama());
                    System.out.println(mahasiswa.getProdi());
                    System.out.println(mahasiswa.getAlamat());
                }
                fileHelper.generateFile(mahasiswaList);
                fileHelper.convertToPDF("D:/File/mahasiswa.xlsx");
                fileHelper.uploadFile("D:/File/mahasiswa.pdf");
                messageBotDAO.insertOutbox(chat_id,"https://itcc-udayana.com/berkas/mahasiswa.pdf", outbox, 3);
                messageBotDAO.getData(inbox, outbox);
                System.out.println(outbox.getMessage());

                SendDocument doc = new SendDocument()
                        .setChatId(chat_id)
                        .setDocument(outbox.getMessage());

                try {
                    sendDocument(doc);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

                messageBotDAO.updateSQL(inbox);

            }else if (menuTerakhir.equals("7") && arrExplode[0].equals("tambah mahasiswa")){
                mahasiswaHelper.getSQL(arrExplode[0], chat_id, command, outbox);
                String query = outbox.getMessage();
                System.out.println(query);
                mahasiswaHelper.insertMhs(query, arrExplode[1], arrExplode[2], arrExplode[3], arrExplode[4], chat_id, outbox);
                messageBotDAO.getData(inbox, outbox);
                System.out.println(outbox.getMessage());
                message.setText(outbox.getMessage());

                message.setChatId(update.getMessage().getChatId());
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                messageBotDAO.updateSQL(inbox);

            }else if (menuTerakhir.equals("8") && arrExplode[0].equals("hapus mahasiswa")){
                mahasiswaHelper.getSQL(arrExplode[0], chat_id, command, outbox);
                String query = outbox.getMessage();
                System.out.println(query);
                mahasiswaHelper.deleteMhs(query, arrExplode[1], chat_id, outbox);
                messageBotDAO.getData(inbox, outbox);
                System.out.println(outbox.getMessage());
                message.setText(outbox.getMessage());

                message.setChatId(update.getMessage().getChatId());
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                messageBotDAO.updateSQL(inbox);

            }else if (menuTerakhir.equals("9") && arrExplode[0].equals("edit mahasiswa")){
                mahasiswaHelper.getSQL(arrExplode[0], chat_id, command, outbox);
                String query = outbox.getMessage();
                System.out.println(query);
                mahasiswaHelper.updateMhs(query, arrExplode[1], arrExplode[2], arrExplode[3], arrExplode[4], arrExplode[5],
                        chat_id, outbox);
                messageBotDAO.getData(inbox, outbox);
                System.out.println(outbox.getMessage());
                message.setText(outbox.getMessage());

                message.setChatId(update.getMessage().getChatId());
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                messageBotDAO.updateSQL(inbox);

            }else {
                messageBotDAO.selectMenu(outbox);
                System.out.println(outbox.getMessage());
                message.setText(outbox.getMessage());

                message.setChatId(update.getMessage().getChatId());
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

        }else if (menuTerakhir.equals("lihat kegiatan")){
            if (command.equals("a")||command.equals("b")||command.equals("c")){
                photoBotDAO.getSqlPhoto(command, chat_id, outbox);
                messageBotDAO.getData(inbox, outbox);
                SendPhoto msg = new SendPhoto()
                        .setChatId(chat_id)
                        .setPhoto(outbox.getMessage());
                try {
                    sendPhoto(msg);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
                messageBotDAO.updateSQL(inbox);

            }else {
                messageBotDAO.selectMenu(outbox);
                System.out.println(outbox.getMessage());
                message.setText(outbox.getMessage());

                message.setChatId(update.getMessage().getChatId());
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }

        }else {
            messageBotDAO.selectMenu(outbox);
            System.out.println(outbox.getMessage());
            message.setText(outbox.getMessage());

            message.setChatId(update.getMessage().getChatId());
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }

    }

    /*===============================================================================================
                                            FUNGSI TO EXPLODE PESAN
    =================================================================================================*/
    public static String[] explodeStringUsingCoreJava(String stringToExplode,String separator){
        return stringToExplode.split(separator);
    }

    public String getBotUsername() {
        return "guspram_bot";
    }

    public String getBotToken() {
        return "652559193:AAG4y9_nbJA_mVHlNpvalNTcJcdpjA61xbI";
    }
}
