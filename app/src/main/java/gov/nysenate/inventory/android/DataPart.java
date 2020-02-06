package gov.nysenate.inventory.android;

/**
 * Simple data container use for passing byte file
 */
public class DataPart {
    private String fileName;
    private byte[] content;
    private String type;

    /**
     * Default data part
     */
    public DataPart() {
    }

    /**
     * Constructor with data.
     *
     * @param name label of data
     * @param data byte data
     */
    public DataPart(String name, byte[] data) {
        fileName = name;
        content = data;
    }

    /**
     * Constructor with mime data type.
     *
     * @param name     label of data
     * @param data     byte data
     * @param mimeType mime data like "image/jpg"
     */
    public DataPart(String name, byte[] data, String mimeType) {
        fileName = name;
        content = data;
        type = mimeType;
    }

    /**
     * Getter file name.
     *
     * @return file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Setter file name.
     *
     * @param fileName string file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Getter content.
     *
     * @return byte file data
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Setter content.
     *
     * @param content byte file data
     */
    public void setContent(byte[] content) {
        this.content = content;
    }

    /**
     * Getter mime type.
     *
     * @return mime type
     */
    public String getType() {
        return type;
    }

    /**
     * Setter mime type.
     *
     * @param type mime type
     */
    public void setType(String type) {
        this.type = type;
    }
}