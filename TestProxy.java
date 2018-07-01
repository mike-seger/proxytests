import java.net.HttpURLConnection;
import java.net.URL;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Scanner;
import java.io.InputStream;

public class TestProxy {
    public static void main(String [] args) throws Exception {
        initProxyProperties(System.getProperty("httpproxy"));
        initProxyProperties(System.getProperty("httpsproxy"));
        
        initAuthenticator();
        boolean silent=false;
        for(String urlString : args) {
            if("-".equals(urlString)) {
                silent=true;
                continue;
            }
            String content=getContent(urlString);
            if(!silent) {
                System.out.println(content);
            } else {
                System.out.println(content.length()+" bytes");
            }
        }
    }
    
    private static void initProxyProperties(String proxy) {
        if(proxy==null) {
            return;
        }
        String user=null;
        String password=null;
        String host;
        String port=null;
        String protocol;
        String [] proxyParts=proxy.split(":");
        
        try {
            if(proxyParts.length<3) {
                throw new RuntimeException("Proxy parts < 3");
            }
            if(!proxyParts[1].startsWith("//")) {
                throw new RuntimeException("Proxy prts 1 does not start with //");
            }
            protocol=proxyParts[0];
            proxyParts[1]=proxyParts[1].substring(2);
            int hostIndex=2;
            if(proxyParts.length==4) {
                String [] passHost=proxyParts[2].split("@");
                if(passHost.length!=2) {
                    throw new RuntimeException("No password in proxy part: "+proxyParts[1]);
                }
                proxyParts[2]=passHost[1];
                System.setProperty(protocol + ".proxyUser", proxyParts[1]);
                System.setProperty(protocol + ".proxyPassword", passHost[0]);
            } else {
                hostIndex=1;
            }
            System.setProperty(protocol + ".proxyHost", proxyParts[hostIndex]);
            System.setProperty(protocol + ".proxyPort", proxyParts[hostIndex+1]);            
            System.out.printf("Proxy: %s://%s:%s@%s:%s\n", protocol, 
                System.getProperty(protocol + ".proxyUser"), 
                System.getProperty(protocol + ".proxyPassword"), 
                proxyParts[hostIndex], proxyParts[hostIndex+1]);
            if("https".equals(protocol)) {
                System.out.println("Disabled Schemes: jdk.http.auth.tunneling.disabledSchemes jdk.http.auth.proxying.disabledSchemes");
                System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
                System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
            }
        } catch(Exception e) {
            throw new RuntimeException("Invalid proxy definition: ["+proxy+"] expected: http[s]://[<user>:<password>@]<host>:<port>", e);
        }
    }

    private static void initAuthenticator() {
        Authenticator.setDefault(
            new Authenticator() {
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    String prot = getRequestingProtocol().toLowerCase();
                    String user = System.getProperty(prot + ".proxyUser", "");
                    if("".equals(user)) {
                        System.out.println("\nNo Proxy Auth for "+prot);
                        return null;
                    }
                    String password = System.getProperty(prot + ".proxyPassword", "");
                    String host = System.getProperty(prot + ".proxyHost", "");
                    String port = System.getProperty(prot + ".proxyPort", "");
                    System.out.println("\n-----------------------"+prot);
                    System.out.printf("Proxy Auth: %s://%s:%s@%s:%s\n", prot, user, password, host, port);
                    return new PasswordAuthentication(
                        user, password.toCharArray());
                    }
                }
        );
    }
    
    private static String getContent(String urlString) throws Exception {
        String content="<none>";
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        int code = conn.getResponseCode();
        System.out.println("Status code: "+code);
        try (InputStream is = (InputStream) conn.getContent()) {
            content = new Scanner(is).useDelimiter("\\A").next();
        }
        conn.getContent();
        conn.disconnect();
        return content;
    }
}
