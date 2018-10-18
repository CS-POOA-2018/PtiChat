package fr.centralesupelec.ptichatapp.PODS;

public class Chat {
    private String id;
    private String name;

    /** Creates a new chat object from existing chat */
    public Chat(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
