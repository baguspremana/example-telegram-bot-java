package helper;

import model.DatabaseConnection;
import model.Outbox;

import java.sql.*;

public class MahasiswaHelper {

    MessageBotDAO messageBotDAO = new MessageBotDAO();

    public MahasiswaHelper() {

    }

    public String getSQL(String keyword, Long chat_id, String pesan, Outbox outbox){
        String sql = "SELECT processing.`sql` FROM processing WHERE processing.`format` LIKE '%"+keyword+"%';";

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){
                String query = rs.getString("sql");
                outbox.setMessage(query);

                messageBotDAO.insertInbox(chat_id, pesan);
            }

            rs.close();
            st.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            outbox.setMessage("Data Tidak Ditemukan");
        }

        return outbox.getMessage();
    }

    public void insertMhs(String query, String nim, String nama, String prodi, String alamat, Long chat_id, Outbox outbox){
        String sql = query;
        String pesan;
        System.out.println(sql);

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            PreparedStatement st = connection.prepareStatement(sql);

            st.setString(1, nim);
            st.setString(2, nama);
            st.setString(3, prodi);
            st.setString(4, alamat);
            int rowInserted = st.executeUpdate();
            if (rowInserted > 0){
                System.out.println("Data berhasil ditambahkan");

                pesan = "Data mahasiswa dengan nama : "+nama+" berhasil ditambahkan";
                messageBotDAO.insertOutbox(chat_id, pesan, outbox, 1);
            }

            st.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteMhs(String query, String nim, Long chat_id, Outbox outbox){
        String sql = query;
        sql = sql.replace("'?'", "'"+nim+"'");
        String pesan;
        System.out.println(sql);

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            PreparedStatement st = connection.prepareStatement(sql);
            st.executeUpdate();
            System.out.println("Data Berhasil Dihapus");
            pesan = "Data dengan NIM : "+nim+" berhasil dihapus";
            messageBotDAO.insertOutbox(chat_id, pesan, outbox, 1);

            st.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateMhs(String query, String niml, String nim, String nama, String prodi, String alamat, Long chat_id, Outbox outbox){
        String sql = query;
        sql = sql.replace("'?'","'"+niml+"'");
        String pesan;
        System.out.println(sql);

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            PreparedStatement st = connection.prepareStatement(sql);

            st.setString(1, nim);
            st.setString(2, nama);
            st.setString(3, prodi);
            st.setString(4, alamat);
            int rowInserted = st.executeUpdate();
            if (rowInserted > 0){
                System.out.println("Data berhasil ditambahkan");

                pesan = "Data mahasiswa dengan nim : "+niml+" berhasil diedit";
                messageBotDAO.insertOutbox(chat_id, pesan, outbox, 1);
            }

            st.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
