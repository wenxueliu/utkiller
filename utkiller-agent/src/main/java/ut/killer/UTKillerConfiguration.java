package ut.killer;

import java.util.List;

public class UTKillerConfiguration {
    private String baseDir;

    private String namespace;

    private List<String> includePackages;

    private int port;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public List<String> getIncludePackages() {
        return includePackages;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public void setIncludePackages(List<String> includePackages) {
        this.includePackages = includePackages;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String toString() {
        return "UTKillerConfiguration{" + "baseDir='" + baseDir + '\'' +
                ", namespace='" + namespace + '\'' +
                ", includePackages=" + includePackages +
                '}';
    }
}
