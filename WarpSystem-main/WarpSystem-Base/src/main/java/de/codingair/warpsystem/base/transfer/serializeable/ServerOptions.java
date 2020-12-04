package de.codingair.warpsystem.base.transfer.serializeable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerOptions implements Serializable {
    private String version;
    private int updateFetching;

    private boolean fetched = false;
    private boolean sameVersion = false;

    public ServerOptions() {
    }

    public ServerOptions(String version, int updateFetching) {
        this.version = version;
        this.updateFetching = updateFetching;
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeUTF(this.version);
        out.writeByte(updateFetching);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        this.version = in.readUTF();
        updateFetching = in.readUnsignedByte();
    }

    public String getVersion() {
        return version;
    }

    public int getUpdateFetching() {
        return updateFetching;
    }

    public boolean isFetched() {
        return fetched;
    }

    public void setFetched(boolean fetched) {
        this.fetched = fetched;
    }

    public boolean sameVersion() {
        return sameVersion;
    }

    public void setSameVersion(boolean sameVersion) {
        this.sameVersion = sameVersion;
    }
}
