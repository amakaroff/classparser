package com.classparser.exception.file;

/**
 * Runtime exception which throws when process of file
 * reading was interrupted with any errors
 *
 * @author Aleksei Makarov
 */
public class FileReadingException extends RuntimeException {

    private final String filePath;

    /**
     * Constructor with parameters cause and message of exception
     *
     * @param message reason message of exception
     * @param cause   cause of this exception
     * @param filePath path of file which is can't be reading
     */
    public FileReadingException(String message, Throwable cause, String filePath) {
        super(message, cause);
        this.filePath = filePath;
    }

    /**
     * Obtain path of file which is can't be reading
     *
     * @return file path
     */
    public String getFilePath() {
        return filePath;
    }
}
