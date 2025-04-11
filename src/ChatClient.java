import javax.swing.*; 

/**
 * Class to setup and run the Chat's Graphical User Interface
 */
public class ChatClient {
    public static void main(String[] args) throws Exception {

       // set look and feel to system default 
       try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // launch login GUI
        SwingUtilities.invokeLater(() -> {
            new LoginGUI();
        }); 
    }
}
