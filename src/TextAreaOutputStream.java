import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream; 
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * class that provides the ability for the user to see terminal output from their actions
 */
public class TextAreaOutputStream extends OutputStream { 
    
    private final JTextArea textArea;
    private final ByteArrayOutputStream buffer;

    /**
     * Expected constructor 
     * @param textArea - allocated text area
     */
    public TextAreaOutputStream(JTextArea textArea) {
        this.textArea = textArea; 
        this.buffer = new ByteArrayOutputStream(); 
    }

    /**
     * fills in the instance's allocated text area with terminal output
     */
    @Override
    public void write(int b) throws IOException {
        buffer.write(b);
        if (b == '\n') {
            //when we encoutner a new line, update the text area
            final String text = buffer.toString();
            SwingUtilities.invokeLater(() -> {
                textArea.append(text);
                //auto-scroll to the bottom
                textArea.setCaretPosition(textArea.getDocument().getLength());
            }); 
            buffer.reset();
        }
    }

    /**
     * Allows the user to see the terminal's updates and be aware of login/registration issues
     * @param textArea
     */
    public static void redirectSystemStreams(JTextArea textArea) {
        OutputStream out = new TextAreaOutputStream(textArea);
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

}
