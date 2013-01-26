package pl.modulo.scrabble.dictionary;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class DictionaryMIDlet extends MIDlet implements CommandListener {

    private TextBox txtBox1;

    private TextBox txtBoxOK;

    private Dictionary dict;

    public DictionaryMIDlet() {
        txtBox1 = new TextBox("Słowo?", "", 50, TextField.ANY);
        txtBox1.addCommand(new Command("EXIT", Command.EXIT, 2));
        txtBox1.addCommand(new Command("OK", Command.OK, 2));
        txtBox1.setCommandListener(this);

        txtBoxOK = new TextBox("Wynik", "Jest!", 20, TextField.UNEDITABLE);
        txtBoxOK.addCommand(new Command("EXIT", Command.EXIT, 2));
        txtBoxOK.addCommand(new Command("BACK", Command.BACK, 2));
        txtBoxOK.setCommandListener(this);

        dict = new Dictionary();
    }

    protected void startApp() throws MIDletStateChangeException {
        Display display = Display.getDisplay(this);
        display.setCurrent(txtBox1);
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean unconditional) {
    }

    private boolean exists(String word) {
        return dict.reader(word);
    }

    public void commandAction(Command c, Displayable d) {
        final String label = c.getLabel();
        final Display display = Display.getDisplay(this);
        if ("EXIT".equals(label)) {
            this.exitMIDlet();
        }
        else if ("OK".equals(label)) {            
            txtBoxOK.setString(this.exists(txtBox1.getString()) ? "Jest" : "Nie ma");
            display.setCurrent(txtBoxOK);
        }
        else if ("BACK".equals(label)) {
            display.setCurrent(txtBox1);
        }

    }

    /**
     * Exits MIDlet.
     */
    public void exitMIDlet() {
        Display.getDisplay(this).setCurrent(null, null);
        destroyApp(true);
        notifyDestroyed();
    }

}
