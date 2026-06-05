package com.andbank.exporter.domain.model;

/**
 * Contrato de salida de todo {@code StatementExportStrategy}.
 *
 * <p><strong>Regla Módulo 5:</strong> Todo strategy devuelve {@code ExportResult}
 * con el contenido como {@code byte[]} y el {@code ContentType} correspondiente.
 * <em>Nunca</em> escribir a disco desde el strategy — eso es responsabilidad
 * del adapter de salida.</p>
 *
 * <pre>
 *  ExportResult resultado = strategy.export(request);
 *  response.setContentType(resultado.getContentType());
 *  response.getOutputStream().write(resultado.getContent());
 * </pre>
 */
public class ExportResult {

    /** Contenido binario del archivo generado. */
    private final byte[] content;

    /**
     * MIME type del contenido.
     *
     * <p>Ejemplos:
     * <ul>
     *   <li>CSV  → {@code "text/csv; charset=UTF-8"}</li>
     *   <li>PDF  → {@code "application/pdf"}</li>
     *   <li>OFX  → {@code "application/x-ofx"}</li>
     *   <li>JSON → {@code "application/json; charset=UTF-8"}</li>
     *   <li>XLSX → {@code "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"}</li>
     * </ul>
     */
    private final String contentType;

    /** Nombre sugerido para el archivo al descargarlo (Content-Disposition). */
    private final String filename;

    private ExportResult(byte[] content, String contentType, String filename) {
        if (content == null)      throw new IllegalArgumentException("content no puede ser null");
        if (contentType == null)  throw new IllegalArgumentException("contentType no puede ser null");
        if (filename == null)     throw new IllegalArgumentException("filename no puede ser null");
        this.content = content;
        this.contentType = contentType;
        this.filename = filename;
    }

    public static ExportResult of(byte[] content, String contentType, String filename) {
        return new ExportResult(content, contentType, filename);
    }

    public byte[] getContent()      { return content; }
    public String getContentType()  { return contentType; }
    public String getFilename()     { return filename; }

    /** Tamaño en bytes del contenido generado. Útil para tests de tamaño mínimo. */
    public int size() { return content.length; }

    @Override
    public String toString() {
        return "ExportResult{filename='" + filename + "', contentType='" + contentType
                + "', size=" + size() + " bytes}";
    }
}
