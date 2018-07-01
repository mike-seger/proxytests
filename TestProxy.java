import java.net.HttpURLConnection;
import java.net.URL;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class TestProxy {
  /*  private final static String authUser = "user";
    private final static String authPassword = "password";
    private Authenticator.setDefault(
        new Authenticator() {
            @Override
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                    authUser, authPassword.toCharArray());
                }
            }
    );
    */
    private static void initAuthenticator() {
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                if (getRequestorType() == RequestorType.PROXY) {
                    String prot = getRequestingProtocol().toLowerCase();
                    String host = System.getProperty(prot + ".proxyHost", "");
                    String port = System.getProperty(prot + ".proxyPort", "");
                    String user = System.getProperty(prot + ".proxyUser", "");
                    String password = System.getProperty(prot + ".proxyPassword", "");
 
                    if (getRequestingHost().toLowerCase().equals(host.toLowerCase())) {
                        if (Integer.parseInt(port) == getRequestingPort()) {
                            // Seems to be OK.
                            System.out.printf("User/Pass: %s/%s", user, password);
                            return new PasswordAuthentication(user, password.toCharArray());  
                        }
                    }
                }
                return null;
            }  
        });
    }

    public static void main(String [] args) throws Exception {
/*        System.getProperties().put("https.proxyHost", "localhost");
        System.getProperties().put("https.proxyPort", "17780");
        System.getProperties().put("http.proxyHost", "localhost");
        System.getProperties().put("http.proxyPort", "17780");
*/
        System.out.println("Hello");
        initAuthenticator();

//        System.setProperty("http.proxyUser", authUser);
//        System.setProperty("http.proxyPassword", authPassword);

        HttpURLConnection conn = (HttpURLConnection) new URL("http://www.google.com").openConnection();
        conn.getContent();
        conn.disconnect();
    }
}
