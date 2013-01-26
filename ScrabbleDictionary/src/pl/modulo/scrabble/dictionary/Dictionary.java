package pl.modulo.scrabble.dictionary;

import java.io.*;


public class Dictionary {
    public static final int DICT_LEN = 4523730;

    private static String POLISH_LETTERS = "aąbcćdeęfghijklłmnńoóprsśtuwyzźż";

    InputStreamReader dictReader;
    String dictWord;
    int bytesLeft;

    int prefixWords;
    int nextPrefixWords;
    int token;

    public Dictionary() {
    }

    public boolean reader(final String wordUTF16) {
        if (!isGoodWord(wordUTF16)) {
            return false;
        }
        try {
            final String word32 = this.convertString(wordUTF16.toLowerCase());
            this.dictReader = this.getDictReader();
            int prefixesOffset = getPrefixWords(word32);
            if (prefixWords == 0xffffff) {
                return false;
            }
            getNextPrefixWords(prefixesOffset);
            bytesLeft = nextPrefixWords - prefixWords;
            this.dictReader = getDictReader();
            dictReader.skip(prefixWords);
            readFirstWord(word32);
            int comparison = this.compareStringsPL(dictWord, word32);
            while (comparison < 0 && bytesLeft > 0) {
                readNextWord();
                comparison = this.compareStringsPL(dictWord, word32);
            }
            return (comparison == 0);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InvalidCharacterException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private InputStreamReader getDictReader() {
        InputStream dictStream = getClass().getResourceAsStream("/slownik.bin");
        return new InputStreamReader(dictStream);
    }

    private void getNextPrefixWords(int prefixesOffset) throws IOException {
        if (prefixesOffset >= 1020) {
            nextPrefixWords = DICT_LEN - 3 * 1024;
        } else {
            do {
                nextPrefixWords = this.readOffset();
            } while (nextPrefixWords == 0xffffff);
        }
    }

    private void readFirstWord(final String word32) throws IOException {
        dictWord = word32.substring(0, 2);
        token = this.readAndDecCounter();
        if (token != 0xff) {
            StringBuffer wordBuf = new StringBuffer(dictWord);
            token = token & 0x1f;
            while (token <= 0x1f) {
                wordBuf.append((char) token);
                token = this.readAndDecCounter();
            }
            dictWord = wordBuf.toString();
        } else {
            token = this.readAndDecCounter();
        }
    }

    private void readNextWord() throws IOException {
        StringBuffer wordBuf;
        if (token < 0xe0) {
            int charsBack = (token >> 5) - 1;
            wordBuf = new StringBuffer(dictWord.substring(0, dictWord.length() - charsBack));
        } else {
            wordBuf = new StringBuffer(dictWord.substring(0, token - 0xe0));
            token = this.readAndDecCounter();
        }
        token = token & 0x1f;
        while (token <= 0x1f) {
            wordBuf.append((char) token);
            token = this.readAndDecCounter();
        }
        dictWord = wordBuf.toString();
    }

    private int getPrefixWords(final String word32) throws IOException {
        int prefixesOffset = word32.charAt(0) * 32 + word32.charAt(1);
        dictReader.skip(DICT_LEN - 3 * 1024 + 3 * prefixesOffset);
        prefixWords = this.readOffset();
        return prefixesOffset;
    }

    private int readAndDecCounter() throws IOException {
        --this.bytesLeft;
        return this.dictReader.read();
    }

    private int convertChar(char c) {
        int pos = POLISH_LETTERS.indexOf(c);
        if (pos < 0 || pos > 31) {
            throw new InvalidCharacterException("forbidden char " + c);
        }
        return pos;
    }

    private String convertString(String s) {
        StringBuffer sb = new StringBuffer(s.length());
        for (int i = 0; i < s.length(); i++) {
            sb.append((char) convertChar(s.charAt(i)));
        }
        return sb.toString();
    }

    private int compareStringsPL(String s, String t) {
        int i = 2;
        while (i < s.length() && i < t.length()) {
            int s1 = s.charAt(i);
            int t1 = t.charAt(i);
            if (s1 != t1) {
                return s1 - t1;
            }
            i++;
        }
        return s.length() - t.length();
    }

    private boolean isGoodWord(String s) {
        return s.indexOf("x") < 0 && s.indexOf("q") < 0 && s.indexOf("v") < 0 && s.length() >= 2 && s.length() <= 15;
    }

    private int readOffset() throws IOException {
        int b1 = this.dictReader.read();
        int b2 = this.dictReader.read();
        int b3 = this.dictReader.read();
        return (b1 << 16) + (b2 << 8) + b3;
    }
}
class InvalidCharacterException extends RuntimeException {

    public InvalidCharacterException(String arg0) {
        super(arg0);
    }
}