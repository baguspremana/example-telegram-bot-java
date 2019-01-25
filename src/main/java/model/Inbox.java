package model;

public class Inbox {
    private Long chat_id;
    private String pesan;

    public Inbox(Long chat_id, String pesan) {
        this.chat_id = chat_id;
        this.pesan = pesan;
    }

    public Long getChat_id() {
        return chat_id;
    }

    public void setChat_id(Long chat_id) {
        this.chat_id = chat_id;
    }

    public String getPesan() {
        return pesan;
    }

    public void setPesan(String pesan) {
        this.pesan = pesan;
    }
}
