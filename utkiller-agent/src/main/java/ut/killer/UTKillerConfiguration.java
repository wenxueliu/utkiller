package ut.killer;

import java.util.List;

/**
 * UTKiller 配置类。
 * <p>
 * 此类封装了从 YAML 文件中读取的所有配置项。它提供了对配置项的访问方法，
 * 使得可以方便地在应用程序中使用这些配置。
 * </p>
 */
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
