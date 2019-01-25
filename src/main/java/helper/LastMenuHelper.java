package helper;

import model.DatabaseConnection;
import model.LastMenu;

import java.sql.*;

public class LastMenuHelper {

    public LastMenuHelper() {

    }

    public void insertLastMenu(Long chat_id, String last_menu, LastMenu lastMenu){
        String insSQL = "INSERT INTO log_last_chat_menu(chat_id, last_menu) VALUES(?,?);";

        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            PreparedStatement st = connection.prepareStatement(insSQL);
            st.setLong(1, chat_id);
            st.setString(2, last_menu);

            int rowInserted = st.executeUpdate();
            if (rowInserted > 0){
                System.out.println("Data berhasil ditambahkan");
            }

            selectLastMenu(chat_id, lastMenu);
            st.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String selectLastMenu(Long chat_id, LastMenu lastMenu){

        String sql = "SELECT * FROM log_last_chat_menu WHERE chat_id = "+chat_id+" ORDER BY id DESC LIMIT 1;";
        try {
            Connection connection = DriverManager.getConnection(DatabaseConnection.Entry.URL, DatabaseConnection.Entry.USERNAME,
                    DatabaseConnection.Entry.PASSWORD);

            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()!=false){
                do {
                    int id = rs.getInt("id");
                    Long id_chat = rs.getLong("chat_id");
                    String menu = rs.getString("last_menu");

                    lastMenu.setLast_menu(menu);
                }while (rs.next());
            }else {
                lastMenu.setLast_menu("Belum Ada Data");
            }

            rs.close();
            st.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
            lastMenu.setLast_menu("Error");
        }

        return lastMenu.getLast_menu();
    }
}
