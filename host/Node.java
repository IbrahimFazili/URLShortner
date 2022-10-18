public class Node {
    private String addr;
    private int id;
    private int state;

    public Node(String addr, int id, int state) {
        this.addr = addr;
        this.id = id;
        this.state = state;
    }

    public String getAddr() {
        return this.addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public int getId() {
        return this.id;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
