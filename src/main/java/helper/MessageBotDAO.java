package helper;

import model.DatabaseConnection;
import model.Inbox;
import model.Outbox;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageBotDAO {
    /*===============================================================================================
                                            CREATE CONSTSTRUCTOR
    =================================================================================================*/
    public MessageBotDAO() {

    }

    /*===============================================================================================
                                            SELECT MENU
    =================================================================================================*/
    public String selectMenu(Outbox outbox){
        String select = "SELECT processing.`id`, processing.`name` FROM processing;";

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(select);
            List<String> listKeyword = new ArrayList<>();

            while (rs.next()){
                String id = String.valueOf(rs.getInt("id"));
                String keyword = rs.getString("name");
                listKeyword.add(id+". "+keyword);
            }

            String result = listKeyword.toString();
            result = result.replace("[", "");
            result = result.replace("]", "");
            result = result.replace(", ", "\n");

            System.out.println(result);

            outbox.setMessage("Menu yang Tersedia\n\n"+result+"\n\nSilakan Input Nomor Menu");

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
                                            SELECT FORMAT
    =================================================================================================*/
    public String getFormat(Inbox inbox, Outbox outbox){
        String sql = "SELECT format FROM processing WHERE id = "+inbox.getPesan()+";";

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){
                String format = rs.getString("format");
                outbox.setMessage("Input Format Seperti Berikut\n"+format);
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
                                            SELECT SQL QUERY
    =================================================================================================*/
    public String getSQL(String keyword, String nama, String alamat, Outbox outbox, Long chat_id, String pesan){
        String sql = "SELECT processing.`sql` FROM processing WHERE processing.`format` LIKE '%"+keyword+"%';";

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){
                String quey = rs.getString("sql");

                /*===============================================================================================
                                                  CALL FUNCTION INSERT INTO INBOX
                =================================================================================================*/
                insertInbox(chat_id, pesan);

                /*===============================================================================================
                                                  CALL FUNCTION GET LIST DATA
                =================================================================================================*/
                if (keyword.equals("cari mahasiswa")||keyword.equals("cari dosen")){
                    getList(keyword, quey, nama, alamat, outbox, chat_id);
                }else {
                    getList(keyword, quey, null, null, outbox, chat_id);
                }

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
                                            SELECT LIST DATA
    =================================================================================================*/
    public String getList(String keyword, String query, String nama, String alamat,Outbox outbox, Long chat_id){
        String sql = query;
        if (keyword.equals("cari mahasiswa")||keyword.equals("cari dosen")){
            sql = sql.replace("'%a%'","'%"+nama+"%'");
            sql = sql.replace("'%l%'","'%"+alamat+"%'");
        }

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            System.out.println(keyword);
            System.out.println(sql);

            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            List<String> data = new ArrayList<>();
            if (rs.next() == false){
                data.add("Data Tidak Ditemukan");
            }else {
                do{
                    int id = rs.getInt("id");
                    if (keyword.equals("cari mahasiswa") || keyword.equals("lihat semua mahasiswa")){
                        String nim = rs.getString("nim");
                        String name = rs.getString("nama");
                        String prodi = rs.getString("prodi");
                        String address = rs.getString("alamat");
                        data.add("NIM\t\t: "+nim+"\n"+"Nama\t: "+name+"\n"+"Prodi\t: "+prodi+"\n"+"Alamat\t: "+address+"\n\n");
                    }else if (keyword.equals("cari dosen") || keyword.equals("lihat semua dosen")){
                        String nim = rs.getString("nidn");
                        String name = rs.getString("nama");
                        String prodi = rs.getString("prodi");
                        String address = rs.getString("alamat");
                        data.add("NIDN\t\t: "+nim+"\n"+"Nama\t: "+name+"\n"+"Prodi\t: "+prodi+"\n"+"Alamat\t: "+address+"\n\n");
                    }
                }while (rs.next());
            }

            String result = data.toString();
            result = result.replace("[", "");
            result = result.replace("]", "");
            result = result.replace(", ", "\n");

            System.out.println(result);

            /*===============================================================================================
                                             CALL FUNCTION INSERT INTO OUTBOX
            =================================================================================================*/
            insertOutbox(chat_id, result, outbox, 1);

            rs.close();
            st.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            outbox.setMessage("Data Tidak Ditemukan");
        }
        return outbox.getMessage();
    }

    /*===============================================================================================
                                            INSERT INTO INBOX
    =================================================================================================*/
    public void insertInbox(Long chat_id, String pesan){
        String insSQL = "INSERT INTO inbox(chat_id, pesan, created_at) VALUES(?,?,NOW());";

        try{
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            PreparedStatement st = connection.prepareStatement(insSQL);

            st.setLong(1,chat_id);
            st.setString(2, pesan);
            int rowInserted = st.executeUpdate();
            if (rowInserted > 0){
                System.out.println("Data berhasil ditambahkan");
            }

            st.close();
            connection.close();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    /*===============================================================================================
                                            INSERT INTO OUTBOX
    =================================================================================================*/
    public void insertOutbox(Long chat_id, String message, Outbox outbox, int tipe){
        String sql = "INSERT INTO outbox(chat_id, message, type, status) VALUES(?, ?, ?, 0);";

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            PreparedStatement st = connection.prepareStatement(sql);
            st.setLong(1, chat_id);
            st.setString(2, message);
            st.setInt(3, tipe);
            int rowInserted = st.executeUpdate();
            if (rowInserted > 0){
                System.out.println("Data Berhasil Ditambah");
                updateInbox(outbox);
            }

            st.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*===============================================================================================
                                   UPDATE INBOX AFTER PROCESSING
    =================================================================================================*/
    public void updateInbox(Outbox outbox){
        String sql = "UPDATE inbox SET inbox.status=1 WHERE inbox.chat_id=? AND inbox.status != 1;";

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            PreparedStatement st = connection.prepareStatement(sql);
            st.setLong(1, outbox.getChat_id());
            int rowsUpdated = st.executeUpdate();
            if (rowsUpdated > 0){
                System.out.println("Data Berhasil Di Update");
            }

            st.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*===============================================================================================
                                            GET DATA FROM OUTBOX
    =================================================================================================*/
    public String getData(Inbox inbox, Outbox outbox){
        String sql = "SELECT outbox.message FROM outbox WHERE chat_id = "+inbox.getChat_id()+" AND STATUS = 0;";

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);
            PreparedStatement st = connection.prepareStatement(sql);
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()){
                String data = rs.getString("message");

                outbox.setMessage(data);
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
                                   UPDATE OUTBOX AFTER SEND MESSAGE
    =================================================================================================*/
    public void updateSQL(Inbox inbox){
        String updateSQL = "UPDATE outbox SET outbox.status=1, outbox.send_at=NOW() WHERE outbox.chat_id = "+inbox.getChat_id()+
                " AND outbox.status != 1;";

        try {

            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            PreparedStatement st = connection.prepareStatement(updateSQL);
            int rowsUpdated = st.executeUpdate();
            if (rowsUpdated > 0){
                System.out.println("Data Berhasil Di Update");
            }

            st.close();
            connection.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
