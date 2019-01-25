package helper;

import model.DatabaseConnection;
import model.Outbox;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhotoBotDAO {
    /*===============================================================================================
                                            INISIALISASI
    =================================================================================================*/
    private MessageBotDAO messageBotDAO = new MessageBotDAO();

    /*===============================================================================================
                                            CREATE CONSTSTRUCTOR
    =================================================================================================*/
    public PhotoBotDAO() {

    }

    /*===============================================================================================
                                          SELECT DATA KEGIATAN
    =================================================================================================*/
    public String getListKegiatan(String prodi, Outbox outbox){
        String sql = "SELECT part, nama FROM kegiatan_mahasiswa WHERE prodi = '"+prodi+"';";

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            List<String> data = new ArrayList<>();
            if (rs.next() == false){
                data.add("Data Tidak ditemukan");
            }else {
                do {
                    String part = rs.getString("part");
                    String nama = rs.getString("nama");

                    data.add(part+". "+nama);
                }while (rs.next());
            }

            String result = data.toString();
            result = result.replace("[", "");
            result = result.replace("]", "");
            result = result.replace(", ", "\n");

            System.out.println(result);
            if (result.equals("Data Tidak ditemukan")){
                outbox.setMessage("Data Tidak ditemukan");
            }else {
                outbox.setMessage("Kegiatan Mahasiswa dari Prodi "+prodi+" yang Tersedia adalah\n\n"+result
                        +"\n\nSilakan input nomor kegiatan");
            }

            rs.close();
            st.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            outbox.setMessage("Layanan Kami Belum Siap");
        }
        return outbox.getMessage();
    }

    /*===============================================================================================
                                          SELECT SQL KEGIATAN
    =================================================================================================*/
    public String getSqlPhoto(String part, Long chat_id, Outbox outbox){
        String keyword = "lihat kegiatan";
        String sql = "SELECT processing.`sql` FROM processing WHERE keyword = '"+keyword+"';";

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){
                String query = rs.getString("sql");

                System.out.println(query);
                messageBotDAO.insertInbox(chat_id, part);
                getLinkPhoto(query, part, chat_id, outbox);
            }

            rs.close();
            st.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            outbox.setMessage("Layanan Kami Belum Siap");
        }

        return outbox.getMessage();
    }

    /*===============================================================================================
                                     SELECT LINK FOTO KEGIATAN
    =================================================================================================*/
    public String getLinkPhoto(String query, String part, Long chat_id, Outbox outbox){
        String sql = query;
        sql = sql.replace("'?'", "'"+part+"'");

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            System.out.println(sql);
            String result;
            if (rs.next()==false){
                result = "Kosong";
            }else {
                do{
                    result = rs.getString("link_photo");

                }while (rs.next());
            }

            System.out.println(result);
            messageBotDAO.insertOutbox(chat_id, result, outbox, 2);

            rs.close();
            st.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            outbox.setMessage("Layanan Kami Belum Siap");
        }
        return outbox.getMessage();
    }
}
